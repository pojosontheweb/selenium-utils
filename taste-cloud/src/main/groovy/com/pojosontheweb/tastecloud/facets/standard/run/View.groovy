package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.validation.SimpleError
import woko.facets.builtin.developer.ViewImpl

@FacetKey(name="view", profileId="standard", targetObjectType=Run.class)
class View extends ViewImpl {

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        Run run = (Run)facetContext.targetObject
        if (run.finishedOn && !run.result) {
            abc.validationErrors.addGlobalError(new SimpleError("Ouch ! run is finished but we have no report... it's a BUG !!!!"))
        }
        return super.getResolution(abc)
    }
}