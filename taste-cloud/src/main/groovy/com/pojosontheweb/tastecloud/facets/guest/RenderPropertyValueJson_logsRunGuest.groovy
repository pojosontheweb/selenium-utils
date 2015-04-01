package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.model.Log
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import org.json.JSONArray
import woko.facets.builtin.all.RenderPropertyValueJsonCollection
import woko.util.JsonResolution

import javax.servlet.http.HttpServletRequest

@FacetKey(name="renderPropertyValueJson_logs", profileId="guest", targetObjectType=Run.class)
class RenderPropertyValueJson_logsRunGuest extends RenderPropertyValueJsonCollection {

    @Override
    Object propertyToJson(HttpServletRequest request, Object propertyValue) {
        List<Log> logs = (List<Log>)propertyValue ?: []
        int nbLogs = logs.size()
        int limit = 50
        if (logs.size()>limit) {
            logs = logs[nbLogs-limit..nbLogs-1]
        }
        JSONArray res = new JSONArray()
        logs.each { Log log ->
            res.put(JsonResolution.toJson(log, request))
        }
        return res
    }
}