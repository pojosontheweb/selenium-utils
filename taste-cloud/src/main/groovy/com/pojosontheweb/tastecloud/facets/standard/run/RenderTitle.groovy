package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.model.RepositoryRun
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.RenderTitleImpl

@FacetKey(name="renderTitle", profileId="standard", targetObjectType=Run.class)
class RenderTitle extends RenderTitleImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleRun.jsp"
    }

}