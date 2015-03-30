package com.pojosontheweb.tastecloud.model

import net.sourceforge.stripes.validation.Validate

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Config {

    @Id @GeneratedValue
    Long id

    @Validate(required = true)
    @NotNull
    String webappDir

    @Validate(required = true)
    @NotNull
    String dockerDir

    @Validate(required = true)
    @NotNull
    String dockerUrl

}
