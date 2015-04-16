package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.Util
import com.pojosontheweb.tastecloud.model.Repository
import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.woko.RepoDef
import com.pojosontheweb.tastecloud.woko.RepoRunListener
import com.pojosontheweb.tastecloud.woko.TasteRunner
import com.pojosontheweb.tastecloud.woko.TasteStore
import com.pojosontheweb.tastecloud.woko.Vcs
import net.sourceforge.jfacets.annotations.FacetKey
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.Resolution
import woko.Woko
import woko.async.JobBase
import woko.async.JobListener
import woko.async.JobManager
import woko.facets.BaseResolutionFacet
import woko.ioc.WokoInject

@FacetKey(name='pullAndRun', profileId='standard', targetObjectType=Repository.class)
class PullAndRun extends BaseResolutionFacet {

    @WokoInject
    Vcs vcs

    @WokoInject
    JobManager jobManager

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        Repository repo = (Repository)facetContext.targetObject
        RepositoryRun repositoryRun = new RepositoryRun(
            runs: [],
            repository: repo,
            queuedOn: new Date(),
            branch: repo.branch
        )
        TasteStore store = (TasteStore)woko.objectStore
        store.save(repositoryRun)
        store.session.flush()
        jobManager.submit(new PullAndRunJob(woko, vcs, repositoryRun.id), [])
        woko.resolutions().redirect('view', repositoryRun)
    }
}

class PullAndRunJob extends JobBase {

    private final Woko woko
    private final Vcs vcs
    private final Long repositoryRunId

    PullAndRunJob(Woko woko, Vcs vcs, Long repositoryRunId) {
        this.woko = woko
        this.vcs = vcs
        this.repositoryRunId = repositoryRunId
    }

    @Override
    protected void doExecute(java.util.List<JobListener> listeners) {
        TasteStore store = (TasteStore)woko.objectStore
        // checkout the repository in tmp dir
        Util.withTmpDir { File tmpDir ->

            RepoDef repository = store.inTx {
                RepositoryRun rr = store.getRepositoryRun(repositoryRunId)
                return new RepoDef(url:rr.repository.url, branch: rr.repository.branch)
            }

            String revision = vcs.pull(repository, tmpDir)
            store.inTx {
                RepositoryRun rr = store.getRepositoryRun(repositoryRunId)
                rr.revision = revision
                store.save(rr)
            }

            // find all .taste files, create Taste instances
            // and add them to the queue
            tmpDir.eachFileRecurse { File f ->
                String relPath = f.absolutePath.substring(tmpDir.absolutePath.length())
                if (relPath.endsWith('.taste')) {
                    // it's a taste, run it !
                    store.inTx {
                        RepositoryRun repositoryRun = store.getRepositoryRun(repositoryRunId)
                        TasteRunner.createAndSubmitRun(
                            woko,
                            Browsr.Firefox,
                            f.text,
                            f.name,
                            repositoryRun,
                            new RepoRunListener(store)
                        )
                    }
                }
            }
        }
    }
}
