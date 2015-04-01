package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_taste", profileId="guest", targetObjectType=Taste.class)
class RenderPropertyValue_tasteTasteGuest extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/guest/renderPropertyValueTasteTasteGuest.jsp'
    }
}