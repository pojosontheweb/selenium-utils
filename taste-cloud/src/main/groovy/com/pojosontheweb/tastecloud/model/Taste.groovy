package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Taste {

    @Id
    String id

    @NotNull
    Browsr browsr = Browsr.Firefox

    @NotNull
    @Column(columnDefinition = "text")
    String taste

}
