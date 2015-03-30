package com.pojosontheweb.tastecloud.woko

import com.pojosontheweb.tastecloud.model.Run
import woko.hbcompass.HibernateCompassStore

class TasteStore extends HibernateCompassStore {

    TasteStore(List<String> packageNames) {
        super(packageNames)
    }

    Run getRun(runId) {
        (Run)session.get(Run.class, (Serializable)runId)
    }
}
