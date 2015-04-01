package com.pojosontheweb.tastecloud.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Test {

    @Id @GeneratedValue
    Long id

    String name

    Boolean success

    Date startedOn

    Date finishedOn

    @Column(columnDefinition = "text")
    String retVal

    @Column(columnDefinition = "text")
    String err

    @Column(columnDefinition = "text")
    String stack

    Integer getElapsed() {
        if (!startedOn || !finishedOn) {
            return null
        }
        return (finishedOn.time - startedOn.time) / 1000
    }

}
