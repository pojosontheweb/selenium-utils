package com.pojosontheweb.tastecloud.model.activities

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Repository
import com.pojosontheweb.tastecloud.model.RepositoryRun

import javax.persistence.Entity

@Entity
class RepoRunActivity extends ActivityBase {

    Long repoRunId
    String repoName
    String branch

    static RepoRunActivity make(RepositoryRun rr, ActivityType type) {
        Repository repo = rr.repository
        new RepoRunActivity(
            repoRunId: rr.id,
            repoName: repo.name,
            branch: repo.branch,
            browsr: rr.browsr,
            type: type
        )
    }


}
