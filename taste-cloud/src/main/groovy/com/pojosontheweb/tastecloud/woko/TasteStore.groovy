package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.Run
import woko.hbcompass.HibernateCompassStore
import woko.persistence.TransactionCallbackWithResult

class TasteStore extends HibernateCompassStore {

    TasteStore(List<String> packageNames) {
        super(packageNames)
    }

    def <T> T inTx(Closure<T> c) {
        doInTransactionWithResult(c as TransactionCallbackWithResult<T>)
    }

    Run getRun(runId) {
        (Run)session.get(Run.class, (Serializable)runId)
    }

    Config getConfig() {
        (Config)session.createCriteria(Config.class).uniqueResult()
    }
}
