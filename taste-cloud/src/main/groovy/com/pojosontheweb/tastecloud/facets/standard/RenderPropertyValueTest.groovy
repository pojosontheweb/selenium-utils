package com.pojosontheweb.tastecloud.facets.standard

import com.pojosontheweb.tastecloud.model.Test
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue", profileId="standard", targetObjectType=Test.class)
class RenderPropertyValueTest extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderPropertyValueTest.jsp"
    }

}