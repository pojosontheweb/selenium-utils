package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksImpl
import woko.facets.builtin.RenderLinks
import woko.util.LinkUtil

@FacetKey(name="renderLinks", profileId="guest", targetObjectType=Taste.class)
class RenderLinksTasteGuest extends RenderLinksImpl implements RenderLinks {

    @Override
    List<Link> getLinks() {
        def all = new ArrayList(super.getLinks())
        all.add(0, new Link(LinkUtil.getUrl(woko, facetContext.targetObject, 'run'), 'Run'))
        return all
    }

}