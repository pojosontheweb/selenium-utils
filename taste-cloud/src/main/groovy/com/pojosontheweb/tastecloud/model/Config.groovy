package com.pojosontheweb.tastecloud.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Max
import javax.validation.constraints.Min
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

    @NotNull
    @Min(1L)
    @Max(64L)
    Integer parallelJobs

    @Override
    public String toString() {
        return "Config{" +
            "imageName='" + imageName + '\'' +
            ", webappDir='" + webappDir + '\'' +
            ", dockerDir='" + dockerDir + '\'' +
            ", dockerUrl='" + dockerUrl + '\'' +
            ", parallelJobs=" + parallelJobs +
            '}';
    }
}
