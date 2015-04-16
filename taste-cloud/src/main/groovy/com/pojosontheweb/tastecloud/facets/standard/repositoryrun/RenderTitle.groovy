package com.pojosontheweb.tastecloud.facets.standard.repositoryrun

import com.pojosontheweb.tastecloud.model.RepositoryRun
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderTitleImpl

@FacetKey(name="renderTitle", profileId="standard", targetObjectType=RepositoryRun.class)
class RenderTitle extends RenderTitleImpl {

    @Override
    String getTitle() {
        RepositoryRun rr = (RepositoryRun)facetContext.targetObject
        rr.revision
    }

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleRepositoryRun.jsp"
    }

}