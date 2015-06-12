package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksImpl
import woko.facets.builtin.RenderLinks
import woko.util.LinkUtil

@FacetKey(name="renderLinks", profileId="standard", targetObjectType=Run.class)
class RenderLinks extends RenderLinksImpl implements woko.facets.builtin.RenderLinks {

    @Override
    java.util.List<Link> getLinks() {
        def all = new ArrayList(super.getLinks())
        Run r = (Run)facetContext.targetObject
        if (r.finishedOn) {
            all.add(0, new Link(LinkUtil.getUrl(woko, facetContext.targetObject, 'results'), 'Results'))
        }
        return all
    }
}