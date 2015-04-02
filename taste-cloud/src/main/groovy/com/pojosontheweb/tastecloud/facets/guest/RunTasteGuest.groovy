package com.pojosontheweb.tastecloud.facets.guest

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
import woko.async.JobManager
import woko.facets.BaseResolutionFacet

@FacetKey(name="run", profileId="guest", targetObjectType=Taste.class)
class RunTasteGuest extends BaseResolutionFacet {

    @Validate(required = true)
    Browsr browsr

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        Taste t = (Taste)facetContext.targetObject

        // store the run
        TasteStore store = (TasteStore)woko.objectStore
        Run run = new Run(
            id: UUID.randomUUID().toString(),
            browsr: browsr,
            taste: t.taste,
            startedOn: new Date()
        )
        store.save(run)
        store.session.flush()

        // start the job in bg...
        Config config = store.config
        JobManager jobManager = woko.ioc.getComponent(JobManager.KEY)
        jobManager.submit(new RunJob(woko, run.id, new File(config.webappDir), config.dockerUrl, new File(config.dockerDir)), [])

        abc.messages.add(new SimpleMessage('Run started. Page will reload when run finishes.'))

        // redirect to run view
        woko.resolutions().redirect('view', run)
    }
}