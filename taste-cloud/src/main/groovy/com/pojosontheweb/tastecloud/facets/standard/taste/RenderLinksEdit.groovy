package com.pojosontheweb.tastecloud.facets.standard.taste

import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.RenderLinksEditImpl

@FacetKey(name='renderLinksEdit',profileId='standard',targetObjectType= Taste.class)
class RenderLinksEdit extends RenderLinksEditImpl {

    @Override
    java.util.List<Link> getLinks() {
        Taste t = facetContext.targetObject
        def all = super.getLinks()
        all.add(0, new Link('#', 'Run')
            .addAttribute('data-toggle', 'modal')
            .addAttribute('data-target', '#runModal')
            .addAttribute('data-taste-id', woko.objectStore.getKey(t))
            .addAttribute('data-taste-name', t.name))
        return all
    }
}
