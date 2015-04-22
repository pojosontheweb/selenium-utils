package com.pojosontheweb.tastecloud.model

import com.pojosontheweb.selenium.Browsr

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class RepositoryRun {

    @Id @GeneratedValue
    Long id

    @OneToMany(mappedBy = "repositoryRun", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    List<Run> runs

    @ManyToOne(fetch = FetchType.LAZY)
    Repository repository

    @NotNull
    Date queuedOn

    String revision

    @NotNull
    String branch

    Date startedOn

    Date finishedOn

    @NotNull
    Browsr browsr = Browsr.Chrome

    ResultSummary getResultSummary() {
        ResultSummary s = new ResultSummary(finished: finishedOn!=null)
        if (s.finished) {
            runs?.each { Run run ->
                s = s + run.resultSummary
            }
        }
        return s
    }

    @Override
    public String toString() {
        return "RepositoryRun{" +
            "id=" + id +
            ", repository=" + repository?.id +
            ", queuedOn=" + queuedOn +
            ", startedOn=" + startedOn +
            ", finishedOn=" + finishedOn +
            '}';
    }
}
