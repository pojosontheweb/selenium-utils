package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Suite
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue", profileId="guest", targetObjectType=Suite.class)
class RenderPropertyValueSuiteGuest extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderPropertyValueSuiteGuest.jsp"
    }

}