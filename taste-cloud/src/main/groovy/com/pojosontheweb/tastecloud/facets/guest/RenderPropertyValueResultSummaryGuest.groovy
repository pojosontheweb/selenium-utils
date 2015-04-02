package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.ResultSummary
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="guest", targetObjectType=ResultSummary.class)
class RenderPropertyValueResultSummaryGuest extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderPropertyValueResultSummaryGuest.jsp"
    }

}