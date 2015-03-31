package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksImpl
import woko.facets.builtin.RenderLinks
import woko.util.LinkUtil

@FacetKey(name="renderLinks", profileId="guest", targetObjectType=Run.class)
class RenderLinksRunGuest extends RenderLinksImpl implements RenderLinks {

    @Override
    List<Link> getLinks() {
        def all = new ArrayList(super.getLinks())
        Run r = (Run)facetContext.targetObject
        if (r.finishedOn) {
            all << new Link(LinkUtil.getUrl(woko, facetContext.targetObject, 'results'), 'Results')
        }
        return all
    }
}