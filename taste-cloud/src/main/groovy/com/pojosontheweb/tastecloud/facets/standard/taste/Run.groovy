package com.pojosontheweb.tastecloud.facets.standard.taste

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.RunJob
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.SimpleMessage
import net.sourceforge.stripes.validation.Validate
import woko.Woko
import woko.async.JobManager
import woko.facets.BaseResolutionFacet

@FacetKey(name="run", profileId="standard", targetObjectType=Taste.class)
class Run extends BaseResolutionFacet {

    @Validate(required = true)
    Browsr browsr

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        Taste t = (Taste)facetContext.targetObject

        // store the run
        com.pojosontheweb.tastecloud.model.Run run = createAndSubmitRun(woko, browsr, t.taste)

        // redirect to run view
        woko.resolutions().redirect('view', run)
    }

    static com.pojosontheweb.tastecloud.model.Run createAndSubmitRun(Woko woko, Browsr b, String taste) {
        com.pojosontheweb.tastecloud.model.Run run = new com.pojosontheweb.tastecloud.model.Run(
            id: UUID.randomUUID().toString(),
            browsr: b,
            taste: taste,
            queuedOn: new Date()
        )
        TasteStore store = (TasteStore)woko.objectStore
        store.save(run)
        store.session.flush()

        // start the job in bg...
        Config config = store.config
        JobManager jobManager = woko.ioc.getComponent(JobManager.KEY)
        jobManager.submit(
            new RunJob(
                woko,
                run.id,
                new File(config.webappDir),
                config.dockerUrl, new File(config.dockerDir),
                config.imageName
            ), [])

        return run
    }
}