package com.pojosontheweb.tastecloud.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Stats {

    @Id @GeneratedValue
    Long id

    Integer totalRuns = 0

    Long totalTime = 0

    Integer nbRunning = 0

    Integer nbTastesSubmitted = 0

    Integer nbReposQueued = 0

    Integer nbSuccess = 0

    Integer nbFailure = 0

    Stats runSubmitted() {
        nbTastesSubmitted = nbTastesSubmitted + 1
        this
    }

    Stats runStarted() {
        totalRuns = totalRuns + 1
        nbRunning = nbRunning + 1
        this
    }

    Stats runFinished(Run run) {
        nbRunning = nbRunning - 1
        nbTastesSubmitted = nbTastesSubmitted - 1
        totalTime = totalTime + run.elapsed
        if (run.test) {
            if (run.test.success) {
                nbSuccess = nbSuccess + 1
            } else {
                nbFailure = nbFailure + 1
            }
        } else if (run.suite) {
            def counts = run.suite.counts
            nbSuccess = nbSuccess + counts.nbSuccess
            nbFailure = nbFailure + counts.nbFailed
        }
        this
    }

    Stats repoRunSubmitted() {
        nbReposQueued++
        this
    }

    Stats repoRunStarted() {
        nbReposQueued--
        this
    }

    Double getSuccessRate() {
        def success = nbSuccess ?: 0
        def failures = nbFailure ?: 0
        if (success==0 && failures==0) {
            return null
        }
        return success / (failures+success) * 100
    }

}
