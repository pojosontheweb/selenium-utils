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

    private def log(Run run, String msg) {
        def l = run.addLog(msg)
        woko.objectStore.save(l)
        woko.objectStore.save(run)
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
                        String trimmed = msg?.trim()
                        if (trimmed) {
                            def log = run.addLog(trimmed)
                            s.save log
                            s.save run
                        }
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

    private static Test reportToTest(report) {
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

    private static Suite reportToSuite(report) {
        new Suite(
            name: report.name,
            startedOn: parseDate(report.startedOn),
            finishedOn: parseDate(report.finishedOn),
            testResults: report?.testResults?.collect { test ->
                reportToTest(test)
            }
        )
    }

    private static def DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    private static Date parseDate(dateStr) {
        dateStr ? DATE_FORMAT.parse((String)dateStr) : null
    }

    static File resultsDir(File webappDir, String runId) {
        new File(webappDir.absolutePath + File.separator + 'results' + File.separator + runId)
    }
}

