package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.RunSummary
import woko.facets.builtin.all.RenderPropertyValueJsonObject
import woko.facets.builtin.RenderPropertyValueJson
import woko.util.JsonResolution

import javax.servlet.http.HttpServletRequest

@FacetKey(name="renderPropertyValueJson", profileId="guest", targetObjectType=RunSummary.class)
class RenderPropertyValueJsonRunSummaryGuest extends RenderPropertyValueJsonObject implements RenderPropertyValueJson {

    @Override
    Object propertyToJson(HttpServletRequest request, Object propertyValue) {
        JsonResolution.toJson(propertyValue, request)
    }
}