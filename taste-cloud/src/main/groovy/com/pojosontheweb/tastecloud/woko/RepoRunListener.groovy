package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.activities.ActivityType
import com.pojosontheweb.tastecloud.model.activities.RepoRunActivity
import woko.async.Job
import woko.async.JobListener
import woko.util.WLogger

class RepoRunListener implements JobListener {

    private static final WLogger logger = WLogger.getLogger(RepoRunListener.class)

    private final TasteStore store

    String runId

    RepoRunListener(TasteStore store) {
        this.store = store
    }

    def update() {
        logger.info("Updating runId=$runId")
        store.inTx {
            Run r = store.getRun(runId)
            RepositoryRun rr = r.repositoryRun
            if (rr && !rr.finishedOn) {
                if (!rr.startedOn && r.startedOn) {
                    rr.startedOn = r.startedOn
                    store.save(rr)
                    logger.info("repo run $rr started")
                    store.save(RepoRunActivity.make(rr, ActivityType.Start, r))
                }
                // all runs must finish...
                boolean atLeastOneRunPending = rr?.runs?.find { it.finishedOn==null }
                if (!atLeastOneRunPending) {
                    rr.finishedOn = r.finishedOn
                    store.save(rr)
                    logger.info("repo run $rr finished")
                    store.save(RepoRunActivity.make(rr, ActivityType.Finish))
                }
            }
        }
    }

    @Override
    void onStart(Job job) {
        update()
    }

    @Override
    void onProgress(Job job) {
        update()
    }

    @Override
    void onException(Job job, Exception e) {
        update()
    }

    @Override
    void onEnd(Job job, boolean killed) {
        update()
    }

}
