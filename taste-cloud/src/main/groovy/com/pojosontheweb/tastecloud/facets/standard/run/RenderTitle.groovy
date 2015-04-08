package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.RenderTitleImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitle", profileId="standard", targetObjectType=Run.class)
class RenderTitle extends RenderTitleImpl implements woko.facets.builtin.RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleRun.jsp"
    }

}