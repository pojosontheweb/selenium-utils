package com.pojosontheweb.tastecloud.actions

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.woko.DockerManager
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.UrlBinding
import net.sourceforge.stripes.validation.Validate
import org.apache.commons.io.FileUtils
import woko.Woko
import woko.actions.BaseActionBean
import woko.async.JobBase
import woko.async.JobListener
import woko.async.JobManager
import woko.persistence.TransactionCallbackWithResult

@UrlBinding('/run')
class RunAction extends BaseActionBean {

    @Validate(required = true)
    Browsr browsr

    @Validate(required = true)
    String taste

    @DefaultHandler
    @DontValidate
    Resolution display() {
        new ForwardResolution('/WEB-INF/jsp/run.jsp')
    }

    Resolution run() {
        // store the run
        Run run = new Run(
            browsr: browsr,
            taste: taste
        )
        woko.objectStore.save(run)

        // start the job in bg...
        JobManager jobManager = woko.ioc.getComponent(JobManager.KEY)
        jobManager.submit(new RunJob(woko, run.id), [])

        // redirect to run view
        woko.resolutions().redirect('view', run)
    }

}

class RunJob extends JobBase {

    private final Woko woko
    private final def runId

    RunJob(Woko woko, runId) {
        this.woko = woko
        this.runId = runId
    }

    private def withRun(Closure c) {
        TasteStore store = (TasteStore)woko.objectStore
        store.doInTransactionWithResult({
            Run run = store.getRun(runId)
            c(run)
        } as TransactionCallbackWithResult)
    }

    private def log(Run run, String msg) {
        run.logs = (run.logs ?: '') + msg + '\n'
    }

    private static def withTmpDir(Closure c) {
        File tmpDir = new File(System.getProperty('java.io.tmpdir') + File.separator +
            UUID.randomUUID().toString())
        tmpDir.mkdirs()
        try {
            return c(tmpDir)
        } finally {
            FileUtils.deleteDirectory(tmpDir)
        }
    }

    @Override
    protected void doExecute(List<JobListener> listeners) {

        try {
            String tasteTxt = withRun { Run run ->
                log run, 'Run started...'
                run.startedOn = new Date()
                return run.taste
            }

            // create a folder in tmp dir to store
            // the taste run data
            withTmpDir { File tmp ->

                // store taste script to file
                File tasteFile = new File(tmp, 'tests.taste')
                tasteFile.text = tasteTxt

                // run the taste file into a docker of its own !
                DockerManager dm = woko.ioc.getComponent(DockerManager.KEY)

                dm.startRun(tmp)

            }





        } finally {
            withRun { Run run ->
                log run, 'Run finished'
                run.finishedOn = new Date()
            }

        }



    }
}
