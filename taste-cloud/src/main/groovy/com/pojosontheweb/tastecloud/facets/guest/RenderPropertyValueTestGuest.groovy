package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.model.Test
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue", profileId="guest", targetObjectType=Test.class)
class RenderPropertyValueTestGuest extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderPropertyValueTestGuest.jsp"
    }

}