package com.pojosontheweb.tastecloud.facets.standard.run

import com.pojosontheweb.tastecloud.model.RunJob
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.StreamingResolution
import net.sourceforge.stripes.action.StrictBinding
import net.sourceforge.stripes.validation.Validate
import woko.facets.BaseResolutionFacet
import woko.facets.builtin.all.Link

@StrictBinding(allow = ['facet.file'])
@FacetKey(name="results", profileId="standard", targetObjectType=Run.class)
class Results extends BaseResolutionFacet {

    @Validate(required = true)
    String file

    @DontValidate
    @Override
    Resolution getResolution(ActionBeanContext abc) {
        new ForwardResolution('/WEB-INF/jsp/results.jsp') // TODO RPC
    }

    Resolution download() {
        Run r = (Run)facetContext.targetObject
        TasteStore s = (TasteStore)woko.objectStore
        Config c = s.config
        File resultsDir = RunJob.resultsDir(new File(c.webappDir), r.id)
        File targetFile = new File(resultsDir, file)
        String contentType
        if (file.endsWith('.json')) {
            contentType = 'application/json'
        } else {
            contentType = 'application/bin'
        }
        return new StreamingResolution(contentType, new FileInputStream(targetFile))
            .setLength(targetFile.length())
            .setFilename(file)
    }

    java.util.List<Link> getLinks() {
        // create links for files/folders
        Run r = (Run)facetContext.targetObject
        TasteStore s = (TasteStore)woko.objectStore
        Config c = s.config
        File resultsDir = RunJob.resultsDir(new File(c.webappDir), r.id)
        def res = []
        if (resultsDir.exists()) {
            resultsDir.eachFile { File f ->
                String fileName = f.name
                String href = request.contextPath + woko.facetUrl('results', r)
                href += "?facet.file=${URLEncoder.encode(fileName, 'utf-8')}&download=true"
                res << new Link(href, f.name)
            }
        }
        return res
    }


}