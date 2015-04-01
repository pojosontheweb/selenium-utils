package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderTitleEditImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitleEdit", profileId="guest", targetObjectType=Config.class)
class RenderTitleEditConfigGuest extends RenderTitleEditImpl implements RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderTitleConfigGuest.jsp"
    }
}