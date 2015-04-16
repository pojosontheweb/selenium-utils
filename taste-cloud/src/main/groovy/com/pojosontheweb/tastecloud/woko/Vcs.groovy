package com.pojosontheweb.tastecloud.woko

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import woko.util.WLogger

class Vcs {

    private static final WLogger logger = WLogger.getLogger(Vcs.class)

    static final String KEY = 'Vcs'

    String pull(RepoDef repository, File toDir) {
        logger.info("Cloning from $repository.url in $toDir...")
        Git git = Git.cloneRepository()
            .setURI(repository.url)
            .setDirectory(toDir)
            .setBranch(repository.branch)
            .call()
        logger.info("...$repository.url cloned in $toDir : getting last rev info")

        RevCommit rc = git
            .log()
            .setMaxCount(1)
            .call()
            .iterator()
            .next()

        return rc.id.name()
    }


}

class RepoDef {
    String url
    String branch = 'master'
}
