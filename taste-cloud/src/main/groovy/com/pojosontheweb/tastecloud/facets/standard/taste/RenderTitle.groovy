package com.pojosontheweb.tastecloud.facets.standard.taste

import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.RenderTitle
import woko.facets.builtin.all.RenderTitleImpl

@FacetKey(name="renderTitle", profileId="standard", targetObjectType=Taste.class)
class RenderTitle extends RenderTitleImpl implements woko.facets.builtin.RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderTitleTaste.jsp"
    }

}