package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.model.RunJob
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.IInstanceFacet
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.stripes.action.ActionBeanContext
import org.apache.commons.io.FileUtils
import woko.facets.builtin.developer.DeleteImpl
import woko.facets.builtin.Delete

@FacetKey(name="delete", profileId="standard", targetObjectType=Run.class)
class Delete extends DeleteImpl implements woko.facets.builtin.Delete, IInstanceFacet {

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

    @Override
    boolean matchesTargetObject(Object targetObject) {
        Run r = (Run)facetContext.targetObject
        r.startedOn && r.finishedOn
    }
}