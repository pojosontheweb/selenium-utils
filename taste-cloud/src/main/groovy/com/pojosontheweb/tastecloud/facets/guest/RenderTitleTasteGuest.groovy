package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.RenderTitle
import woko.facets.builtin.all.RenderTitleImpl

@FacetKey(name="renderTitle", profileId="guest", targetObjectType=Taste.class)
class RenderTitleTasteGuest extends RenderTitleImpl implements RenderTitle {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderTitleTasteGuest.jsp"
    }

}