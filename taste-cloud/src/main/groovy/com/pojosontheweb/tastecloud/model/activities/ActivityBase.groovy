package com.pojosontheweb.tastecloud.model.activities

import com.pojosontheweb.selenium.Browsr

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
abstract class ActivityBase {

    @Id @GeneratedValue
    Long id

    @NotNull
    Date tstamp = new Date()

    @NotNull
    ActivityType type

    Browsr browsr

    String runId

    String relativePath

}
