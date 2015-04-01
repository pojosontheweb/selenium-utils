package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertiesEditButtonsImpl

@FacetKey(name="renderPropertiesEditButtons", profileId="guest", targetObjectType=Taste.class)
class RenderPropertiesEditButtonsTasteGuest extends RenderPropertiesEditButtonsImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/renderPropertiesEditButtonsTaste.jsp'
    }
}