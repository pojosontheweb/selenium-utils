package com.pojosontheweb.tastecloud.model.activities

import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.Taste

import javax.persistence.Entity

@Entity
class TasteRunActivity extends ActivityBase {

    String tasteName
    String tasteId

    static TasteRunActivity make(Run run, ActivityType type) {
        Taste taste = run.fromTaste
        new TasteRunActivity(
            tasteName: taste.name,
            tasteId: taste.id,
            browsr: run.browsr,
            type: type,
            runId: run.id,
            relativePath: run.relativePath
        )
    }
}
