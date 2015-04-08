package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.RunSummary
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="guest", targetObjectType=RunSummary.class)
class RenderPropertyValueRunSummaryGuest extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderPropertyValueRunSummaryGuest.jsp"
    }

}