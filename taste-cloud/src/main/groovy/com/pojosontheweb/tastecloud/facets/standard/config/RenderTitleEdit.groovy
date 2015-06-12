package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderTitleEditImpl

@FacetKey(name="renderTitleEdit", profileId="standard", targetObjectType=Config.class)
class RenderTitleEdit extends RenderTitleEditImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleConfig.jsp"
    }
}