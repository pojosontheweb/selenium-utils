package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderTitleImpl

@FacetKey(name="renderTitle", profileId="standard", targetObjectType=Config.class)
class RenderTitle extends RenderTitleImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleConfig.jsp"
    }

}