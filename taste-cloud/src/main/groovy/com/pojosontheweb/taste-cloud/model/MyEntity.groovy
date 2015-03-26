package com.pojosontheweb.taste-cloud.model

import org.compass.annotations.Searchable
import org.compass.annotations.SearchableId
import org.compass.annotations.SearchableProperty
import javax.validation.constraints.NotNull

import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Searchable
class MyEntity {

    @Id
    @SearchableId
    Long id

    @NotNull
    @SearchableProperty
    String myProp

    Date myOtherProp
}