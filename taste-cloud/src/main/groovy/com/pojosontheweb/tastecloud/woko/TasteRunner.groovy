package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.RunJob
import woko.Woko
import woko.async.Job
import woko.async.JobListener
import woko.async.JobManager

class TasteRunner {

    static Run createAndSubmitRun(Woko woko, Browsr b, String taste) {
        createAndSubmitRun(woko, b, taste, null, null, null)
    }

    static Run createAndSubmitRun(
        Woko woko,
        Browsr browsr,
        String taste,
        String fileName,
        RepositoryRun repositoryRun,
        RepoRunListener listener) {

        Run run = new Run(
            id: UUID.randomUUID().toString(),
            browsr: browsr,
            taste: taste,
            queuedOn: new Date(),
            repositoryRun: repositoryRun,
            relativePath: fileName ?: 'tests.taste'
        )
        TasteStore store = (TasteStore)woko.objectStore
        store.save(run)
        store.session.flush()

        // start the job in bg...
        Config config = store.config
        JobManager jobManager = woko.ioc.getComponent(JobManager.KEY)

        List<JobListener> listeners = []
        if (listener) {
            listener.runId = run.id
            listeners << listener
        }

        jobManager.submit(
            new RunJob(
                woko,
                run.id,
                new File(config.webappDir),
                config.dockerUrl,
                new File(config.dockerDir),
                config.imageName,
            ), listeners)
        return run
    }
}

