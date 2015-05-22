package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_taste", profileId="standard", targetObjectType=Run.class)
class RenderPropertyValue_taste extends RenderPropertyValueImpl  {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/renderPropertyValueRunTaste.jsp'
    }
}