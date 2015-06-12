package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.Util
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.model.RunJob
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import woko.facets.BaseFragmentFacet
import woko.facets.ResolutionFacet

@FacetKey(name="logsFragment", profileId = "standard", targetObjectType = Run.class)
class LogsFragment extends BaseFragmentFacet implements ResolutionFacet {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/runLogsFragment.jsp'
    }

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        new ForwardResolution(path)
    }

    java.util.List<String> getLogs() {
        Run run = (Run)facetContext.targetObject
        run.relativePath
        TasteStore store = woko.objectStore
        File webappDir = new File(store.config.webappDir)
        File resDir = RunJob.resultsDir(webappDir, run.id)
        String fullPath
        if (run.finishedOn) {
            fullPath = resDir.absolutePath + File.separator + run.relativePath + ".output.txt"
        } else {
            fullPath = webappDir.absolutePath + File.separator + run.id + File.separator +
                'target' + File.separator + run.relativePath + ".output.txt"
        }
        File f = new File(fullPath)
        if (!f.exists()) {
            return []
        }
        return Util.tail(f, 25)
    }
}
