package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import org.json.JSONObject
import woko.facets.builtin.all.RenderObjectJsonImpl
import woko.facets.builtin.RenderObjectJson
import woko.util.JSON
import woko.facets.builtin.all.RenderPropertyValueJsonDate

import javax.servlet.http.HttpServletRequest

@FacetKey(name="renderObjectJson", profileId="guest", targetObjectType=Run.class)
class RenderObjectJsonRunGuest extends RenderObjectJsonImpl implements RenderObjectJson {

    @Override
    JSONObject objectToJson(HttpServletRequest request) {
        JSONObject j = super.objectToJson(request)
        Run run = (Run)facetContext.targetObject
        if (run.finishedOn) {
            j.put("finishedOn", RenderPropertyValueJsonDate.dateToJsonString(run.finishedOn))
        }
        return j
    }
}