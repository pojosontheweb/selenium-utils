package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_taste", profileId="standard", targetObjectType=Taste.class)
class RenderPropertyValue_taste extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/renderPropertyValueTasteTaste.jsp'
    }
}