package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.actions.RunJob
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.stripes.action.ActionBeanContext
import org.apache.commons.io.FileUtils
import woko.facets.builtin.developer.DeleteImpl
import woko.facets.builtin.Delete

@FacetKey(name="delete", profileId="guest", targetObjectType=Run.class)
class DeleteRunGuest extends DeleteImpl implements Delete {

    @Override
    protected void doDelete(ActionBeanContext abc) {
        // remove the files associated to this run if any...
        Run r = (Run)facetContext.targetObject
        TasteStore s = (TasteStore)woko.objectStore
        Config c = s.config
        File resultsDir = RunJob.resultsDir(new File(c.webappDir), r.id)
        FileUtils.deleteDirectory(resultsDir)
        super.doDelete(abc)
    }
}