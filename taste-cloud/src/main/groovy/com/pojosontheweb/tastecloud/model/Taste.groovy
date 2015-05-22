package com.pojosontheweb.tastecloud.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
class Taste {

    @Id
    String id

    @NotNull
    String name

    @NotNull
    Date createdOn = new Date()

    Date lastUpdated

    @NotNull
    @Column(columnDefinition = "text")
    String taste

}
