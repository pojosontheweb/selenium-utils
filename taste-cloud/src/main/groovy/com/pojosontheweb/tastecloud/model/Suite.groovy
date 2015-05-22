package com.pojosontheweb.tastecloud.model

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Suite {

    @Id @GeneratedValue
    Long id

    String name

    Date startedOn

    Date finishedOn

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    List<Test> testResults

    Integer getElapsed() {
        if (!startedOn || !finishedOn) {
            return null
        }
        return (finishedOn.time - startedOn.time) / 1000
    }

    SuiteCounts getCounts() {
        new SuiteCounts(this)
    }

}

class SuiteCounts {

    private int nbSuccess = 0
    private int nbFailed = 0

    SuiteCounts(Suite suite) {
        suite.testResults?.each { Test t ->
            if (t.success) {
                nbSuccess++
            } else {
                nbFailed++
            }
        }
    }

    int getNbSuccess() {
        nbSuccess
    }

    int getNbFailed() {
        nbFailed
    }

    int getTotal() {
        nbSuccess + nbFailed
    }

    Double getRatio() {
        nbSuccess * 100 / total
    }

}
