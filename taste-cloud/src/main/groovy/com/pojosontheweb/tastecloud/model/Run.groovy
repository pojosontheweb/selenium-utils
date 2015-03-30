package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Run {

    @Id @GeneratedValue
    String id

    Date startedOn

    Date finishedOn

    @NotNull
    Browsr browsr

    @NotNull
    @Column(columnDefinition = "text")
    String taste

    @Column(columnDefinition = "text")
    String logs
}
