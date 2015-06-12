package com.pojosontheweb.tastecloud.model

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class Repository {

    @Id @GeneratedValue
    Long id

    @NotNull
    String url

    @NotNull
    String name

    @NotNull
    String branch = 'master'

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repository", cascade = [CascadeType.REMOVE])
    List<RepositoryRun> repositoryRuns

    @Override
    public String toString() {
        return "Repository{" +
            "id=" + id +
            ", url='" + url + '\'' +
            ", branch='" + branch + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
