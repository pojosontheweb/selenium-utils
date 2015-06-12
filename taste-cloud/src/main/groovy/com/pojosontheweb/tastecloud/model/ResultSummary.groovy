package com.pojosontheweb.tastecloud.model

class ResultSummary {

    boolean finished
    int nbSuccess = 0
    int nbFailed = 0
    long elapsed = 0

    ResultSummary plus(ResultSummary other) {
        new ResultSummary(
            finished: finished && other.finished,
            nbSuccess: nbSuccess + other.nbSuccess,
            nbFailed: nbFailed + other.nbFailed,
            elapsed: elapsed + other.elapsed
        )
    }

    boolean isSuccess() {
        return nbSuccess>0 && nbFailed==0
    }

    Double getSuccessRatio() {
        if (nbSuccess==0 && nbFailed==0) {
            return null
        }
        return nbSuccess / (nbFailed+nbSuccess) * 100
    }
}
