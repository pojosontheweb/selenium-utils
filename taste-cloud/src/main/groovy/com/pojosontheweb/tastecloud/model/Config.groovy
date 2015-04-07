package com.pojosontheweb.tastecloud.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Config {

    @Id @GeneratedValue
    Long id

    @NotNull
    String imageName

    @NotNull
    String webappDir

    @NotNull
    String dockerDir

    @NotNull
    String dockerUrl

}
