package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderTitleImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitle", profileId="guest", targetObjectType=Config.class)
class RenderTitleConfigGuest extends RenderTitleImpl implements RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderTitleConfigGuest.jsp"
    }

}