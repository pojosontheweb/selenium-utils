package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.facets.RenderPropertyValuePre
import com.pojosontheweb.tastecloud.model.Log
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.BaseForwardResolutionFacet
import woko.facets.builtin.all.RenderPropertyValueImpl

@FacetKey(name="renderPropertyValue_logs", profileId="guest", targetObjectType=Run.class)
class RenderPropertyValue_logsRunGuest extends RenderPropertyValueImpl {

    @Override
    String getPath() {
        Run r = (Run)facetContext.targetObject
        if (r.startedOn) {
            if (r.finishedOn) {
                '/WEB-INF/jsp/guest/renderPropertyValueRunLogs_finished.jsp'
            } else {
                '/WEB-INF/jsp/guest/renderPropertyValueRunLogs_running.jsp'
            }
        } else {
            '/WEB-INF/jsp/guest/renderPropertyValueRunLogs_queued.jsp'
        }
    }

    int getLimit() {
        25
    }

    @Override
    Object getPropertyValue() {
        Run run = (Run)facetContext.targetObject
        List<Log> logs = run.logs
        if (!logs) {
            return []
        }
        // only return the 50 last log entries by default...
        int nbLogs = logs.size()
        if (logs.size()>limit) {
            return logs[nbLogs-limit..nbLogs-1]
        }
        return logs
    }
}