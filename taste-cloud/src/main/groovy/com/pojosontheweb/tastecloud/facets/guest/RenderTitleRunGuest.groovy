package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.RenderTitleImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitle", profileId="guest", targetObjectType=Run.class)
class RenderTitleRunGuest extends RenderTitleImpl implements RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderTitleRunGuest.jsp"
    }

}