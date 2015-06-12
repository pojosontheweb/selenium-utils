package com.pojosontheweb.tastecloud.facets.standard

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.ResultSummary
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="standard", targetObjectType=ResultSummary.class)
class RenderPropertyValueResultSummary extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderPropertyValueResultSummary.jsp"
    }

}