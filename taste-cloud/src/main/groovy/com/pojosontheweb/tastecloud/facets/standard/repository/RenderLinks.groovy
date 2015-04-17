package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksImpl
import woko.util.LinkUtil

@FacetKey(name="renderLinks", profileId="standard", targetObjectType=Repository.class)
class RenderLinks extends RenderLinksImpl implements woko.facets.builtin.RenderLinks {

    @Override
    java.util.List<Link> getLinks() {
        def all = new ArrayList(super.getLinks())
        Repository t = (Repository)facetContext.targetObject
        all.add(0, new Link(LinkUtil.getUrl(woko, t, 'pullAndRun'), 'Pull and run'))
        all.add(1, new Link("list/RepositoryRun?facet.repo=$t.id", 'Run history'))
        return all
    }

}