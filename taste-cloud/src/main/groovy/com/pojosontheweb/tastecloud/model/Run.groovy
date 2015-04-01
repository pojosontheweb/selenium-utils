package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = 'taste_run')
class Run {

    @Id
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

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    Suite suite

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    Test test

    def getResult() {
        return suite ?: test
    }

}
