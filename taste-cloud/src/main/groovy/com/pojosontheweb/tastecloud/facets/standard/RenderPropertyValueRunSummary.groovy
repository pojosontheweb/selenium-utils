package com.pojosontheweb.tastecloud.facets.standard

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.RunSummary
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="standard", targetObjectType=RunSummary.class)
class RenderPropertyValueRunSummary extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderPropertyValueRunSummary.jsp"
    }

}