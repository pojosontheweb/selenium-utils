package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.activities.ActivityType
import com.pojosontheweb.tastecloud.model.activities.RepoRunActivity
import com.pojosontheweb.tastecloud.model.activities.TasteRunActivity
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
    private final String imageName

    RunJob(Woko woko,
           String runId,
           File webappDir,
           String dockerUrl,
           File dockerDir,
           String imageName) {
        this.woko = woko
        this.runId = runId
        this.webappDir = webappDir
        this.dockerUrl = dockerUrl
        this.dockerDir = dockerDir
        this.imageName = imageName
    }

    private def withRun(Closure c) {
        TasteStore store = (TasteStore) woko.objectStore
        store.inTx {
            Run run = store.getRun(runId)
            c(store, run)
        }
    }

    @Override
    protected void doExecute(List<JobListener> listeners) {

        logger.info("$runId : starting, webappDir=$webappDir, dockerDir=$dockerDir")

        try {
            def runData = withRun { TasteStore store, Run run ->
                // prepare run
                run.startedOn = new Date()
                store.save run

                // post new activity to the stream
                if (run.fromTaste) {
                    store.save(TasteRunActivity.make(run, ActivityType.Start))
                }

                // update stats
                store.save(store.stats.runStarted())
                [
                    taste: run.taste,
                    relativePath: run.relativePath,
                    browsr: run.browsr
                ]
            }
            String tasteTxt = runData.taste
            notifyListenersStart(listeners)

            // create a folder in tmp dir to store
            // the taste run data
            File webappFullDir = new File(webappDir, runId)
            webappFullDir.mkdirs()
            try {

                // store taste script to file
                String tasteFileRelativePath = runData.relativePath
                String tasteFullPath = webappFullDir.absolutePath + File.separator + tasteFileRelativePath
                File tasteFile = new File(tasteFullPath)
                tasteFile.text = tasteTxt

                logger.info("$runId : tests file written in $tasteFile.absolutePath")

                // create a config file
                File cfgFile = new File(webappFullDir, 'cfg.taste')
                String browserMeth = runData.browsr.name().toLowerCase()
                cfgFile.text = """import static com.pojosontheweb.taste.Cfg.*

config {

    output {
        json()
    }

    $browserMeth()

    sysProps['webdriver.chrome.driver'] = '/chromedriver'

    locales 'en', 'fr'

    findr {
        timeout 30
        verbose true
    }

    video {
        enabled true
        dir '/mnt/target'
        failuresOnly false
    }
}"""

                // run the taste file into a docker of its own !
                DockerManager dm = woko.ioc.getComponent(DockerManager.KEY)
                dm.onStart = { id ->
                    withRun { store, Run r ->
                        r.dockerId = id
                        store.save(r)
                    }
                }

                // store logs
                // TODO buffer : for now it's heavy db stress for nothing !
                File dockerFullDir = new File(dockerDir, runId)
                // new File('/media/psf/projects/selenium-utils/taste/docker/sample')
                dm.startRun(imageName, dockerUrl, dockerFullDir, tasteFileRelativePath)
                // TODO handle docker exception(s)
                // e.g. com.spotify.docker.client.ImageNotFoundException

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
                run.finishedOn = new Date()
                store.save(run)

                if (run.fromTaste) {
                    store.save(TasteRunActivity.make(run, ActivityType.Finish))
                } else if (run.repositoryRun) {
                    store.save(RepoRunActivity.make(run.repositoryRun, ActivityType.Finish, run))
                }

                store.save(store.stats.runFinished(run))
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
        try {
            dateStr ? DATE_FORMAT.parse((String)dateStr) : null
        } catch(NumberFormatException e) {
            logger.error("Unable to format date $dateStr", e)
            return null
        }
    }

    static File resultsDir(File webappDir, String runId) {
        new File(webappDir.absolutePath + File.separator + 'results' + File.separator + runId)
    }
}

