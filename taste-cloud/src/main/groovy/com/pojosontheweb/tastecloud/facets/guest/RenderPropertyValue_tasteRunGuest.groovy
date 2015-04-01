package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_taste", profileId="guest", targetObjectType=Run.class)
class RenderPropertyValue_tasteRunGuest extends RenderPropertyValueImpl  {

    @Override
    String getPath() {
        '/WEB-INF/jsp/guest/renderPropertyValueRunTasteGuest.jsp'
    }
}