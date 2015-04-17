package com.pojosontheweb.tastecloud.model.activities

import com.pojosontheweb.tastecloud.model.Repository
import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.model.Run

import javax.persistence.Entity

@Entity
class RepoRunActivity extends ActivityBase {

    Long repoRunId
    String repoName
    String branch
    Long repoId

    static RepoRunActivity make(RepositoryRun rr, ActivityType type) {
        make(rr, type, null)
    }

    static RepoRunActivity make(RepositoryRun rr, ActivityType type, Run run) {
        Repository repo = rr.repository
        new RepoRunActivity(
            repoId: repo.id,
            repoRunId: rr.id,
            repoName: repo.name,
            branch: repo.branch,
            browsr: rr.browsr,
            type: type,
            runId: run?.id,
            relativePath: run?.relativePath
        )
    }

}
