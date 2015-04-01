package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.tastecloud.woko.DockerManager
import com.pojosontheweb.tastecloud.woko.TasteStore
import com.spotify.docker.client.LogMessage
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import woko.Woko
import woko.async.JobBase
import woko.async.JobListener
import woko.util.WLogger

import java.nio.ByteBuffer
import java.text.SimpleDateFormat

/**
 * Created by vankeisb on 31/03/15.
 */
class RunJob extends JobBase {

    private static final WLogger logger = WLogger.getLogger(RunJob.class)

    private final Woko woko
    private final String runId
    private final File webappDir
    private final File dockerDir
    private final String dockerUrl

    RunJob(Woko woko, String runId, File webappDir, String dockerUrl, File dockerDir) {
        this.woko = woko
        this.runId = runId
        this.webappDir = webappDir
        this.dockerUrl = dockerUrl
        this.dockerDir = dockerDir
    }

    private def withRun(Closure c) {
        TasteStore store = (TasteStore) woko.objectStore
        store.inTx {
            Run run = store.getRun(runId)
            c(store, run)
        }
    }

    private static def log(Run run, String msg) {
        run.logs = (run.logs ?: '') + msg + '\n'
    }

    private static String logToString(LogMessage lm) {
        ByteBuffer bb = lm.content()
        byte[] b = new byte[bb.remaining()]
        bb.get(b)
        new String(b, 'utf-8')
    }

    @Override
    protected void doExecute(List<JobListener> listeners) {

        logger.info("$runId : starting, webappDir=$webappDir, dockerDir=$dockerDir")

        try {
            String tasteTxt = withRun { TasteStore store, Run run ->
                log run, 'Run started...'
                run.startedOn = new Date()
                store.save run
                store.session.flush()
                run.taste
            }

            // create a folder in tmp dir to store
            // the taste run data
            File webappFullDir = new File(webappDir, runId)
            webappFullDir.mkdirs()
            try {

                // store taste script to file
                File tasteFile = new File(webappFullDir, 'tests.taste')
                tasteFile.text = tasteTxt

                logger.info("$runId : tests file written in $tasteFile.absolutePath")

                // create a config file
                File cfgFile = new File(webappFullDir, 'cfg.taste')
                cfgFile.text = '''import static com.pojosontheweb.taste.Cfg.*

config {

    output {
        json()
    }

    firefox()

//    sysProps['webdriver.chrome.driver'] = "${System.getProperty('user.home')}/chromedriver"

//    locales "en", "fr"                      // locale(s) to be used

    findr {
        timeout 10
        verbose true
    }

    video {
        enabled true
        dir "/mnt/target"
        failuresOnly false
    }
}'''

                // run the taste file into a docker of its own !
                DockerManager dm = woko.ioc.getComponent(DockerManager.KEY)

                // store logs
                // TODO buffer : for now it's heavy db stress for nothing !
                File dockerFullDir = new File(dockerDir, runId)
                // new File('/media/psf/projects/selenium-utils/taste/docker/sample')
                dm.startRun(dockerUrl, dockerFullDir) { LogMessage lm ->
                    withRun { TasteStore s, Run run ->
                        String msg = logToString lm
                        run.logs = run.logs + msg
                        s.save run
                    }
                }

                // retrieve the results from target dir and
                // copy to webapp disk
                File resultsDir = resultsDir(webappDir, runId)
                if (!resultsDir.exists()) {
                    resultsDir.mkdirs()
                }
                File webappTargetDir = new File(webappFullDir, 'target')
                FileUtils.copyDirectory(webappTargetDir, resultsDir)

                // parse report and create db objects
                def jsonFiles = webappTargetDir.listFiles({File dir, String name ->
                    name.endsWith('.json')
                } as FilenameFilter)
                if (jsonFiles.length==1) {
                    File jsonReportFile = jsonFiles[0]
                    def report = new JsonSlurper().parse(new FileReader(jsonReportFile))
                    if (report.testName) {
                        // it's a test
                        logger.info("Result is a test, saving...")
                        withRun { TasteStore store, Run run ->
                            Test test = reportToTest(report)
                            store.save(test)
                            run.test = test
                            store.save(run)
                        }
                    } else {
                        // it's a suite
                        logger.info("Result is a suite, saving...")
                        withRun { TasteStore store, Run run ->
                            Suite s = reportToSuite(report)
                            store.save(s)
                            run.suite = s
                            store.save(run)
                        }
                    }
                }

            } finally {
                logger.info("Cleaning up webapp dir : $webappFullDir.absolutePath")
                FileUtils.deleteDirectory(webappFullDir)
            }

        } finally {
            withRun { TasteStore store, Run run ->
                log run, 'Run finished'
                run.finishedOn = new Date()
                store.save(run)
            }
            logger.info("$runId : done")
        }

    }

    private Test reportToTest(report) {
        new Test(
            success: report.success,
            retVal: report.retVal ? new JsonBuilder(report.retVal).toString() : null,
            name: report.testName,
            startedOn: parseDate(report.startedOn),
            finishedOn: parseDate(report.finishedOn),
            err: report.err,
            stack: report.stack
        )
    }

    private Suite reportToSuite(report) {
        new Suite(
            name: report.name,
            startedOn: parseDate(report.startedOn),
            finishedOn: parseDate(report.finishedOn),
            testResults: report?.testResults?.collect { test ->
                reportToTest(test)
            }
        )
    }

    private Date parseDate(dateStr) {
        dateStr ? ISO8601DateParser.parse((String)dateStr) : null
    }

    static File resultsDir(File webappDir, String runId) {
        new File(webappDir.absolutePath + File.separator + 'results' + File.separator + runId)
    }
}



/**
 * ISO 8601 date parsing utility.  Designed for parsing the ISO subset used in
 * Dublin Core, RSS 1.0, and Atom.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton (burtonator)</a>
 * @version $Id: ISO8601DateParser.java,v 1.2 2005/06/03 20:25:29 snoopdave Exp $
 */
public class ISO8601DateParser {

    // 2004-06-14T19:GMT20:30Z
    // 2004-06-20T06:GMT22:01Z

    // http://www.cl.cam.ac.uk/~mgk25/iso-time.html
    //
    // http://www.intertwingly.net/wiki/pie/DateTime
    //
    // http://www.w3.org/TR/NOTE-datetime
    //
    // Different standards may need different levels of granularity in the date and
    // time, so this profile defines six levels. Standards that reference this
    // profile should specify one or more of these granularities. If a given
    // standard allows more than one granularity, it should specify the meaning of
    // the dates and times with reduced precision, for example, the result of
    // comparing two dates with different precisions.

    // The formats are as follows. Exactly the components shown here must be
    // present, with exactly this punctuation. Note that the "T" appears literally
    // in the string, to indicate the beginning of the time element, as specified in
    // ISO 8601.

    //    Year:
    //       YYYY (eg 1997)
    //    Year and month:
    //       YYYY-MM (eg 1997-07)
    //    Complete date:
    //       YYYY-MM-DD (eg 1997-07-16)
    //    Complete date plus hours and minutes:
    //       YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
    //    Complete date plus hours, minutes and seconds:
    //       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
    //    Complete date plus hours, minutes, seconds and a decimal fraction of a
    // second
    //       YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)

    // where:

    //      YYYY = four-digit year
    //      MM   = two-digit month (01=January, etc.)
    //      DD   = two-digit day of month (01 through 31)
    //      hh   = two digits of hour (00 through 23) (am/pm NOT allowed)
    //      mm   = two digits of minute (00 through 59)
    //      ss   = two digits of second (00 through 59)
    //      s    = one or more digits representing a decimal fraction of a second
    //      TZD  = time zone designator (Z or +hh:mm or -hh:mm)
    public static Date parse( String input ) throws java.text.ParseException {

        //NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        //things a bit.  Before we go on we have to repair this.
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );

        //this is zero time so we need to add that TZ indicator for
        if ( input.endsWith( "Z" ) ) {
            input = input.substring( 0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring( 0, input.length() - inset );
            String s1 = input.substring( input.length() - inset, input.length() );

            input = s0 + "GMT" + s1;
        }

        return df.parse( input );

    }

    public static String toString( Date date ) {

        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );

        TimeZone tz = TimeZone.getTimeZone( "UTC" );

        df.setTimeZone( tz );

        String output = df.format( date );

        int inset0 = 9;
        int inset1 = 6;

        String s0 = output.substring( 0, output.length() - inset0 );
        String s1 = output.substring( output.length() - inset1, output.length() );

        String result = s0 + s1;

        result = result.replaceAll( "UTC", "+00:00" );

        return result;

    }

}
