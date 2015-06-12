package com.pojosontheweb.tastecloud.facets.standard

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Suite
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue", profileId="standard", targetObjectType=Suite.class)
class RenderPropertyValueSuite extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderPropertyValueSuite.jsp"
    }

}