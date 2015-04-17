package com.pojosontheweb.tastecloud.facets.standard

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.NavBarAll
import woko.facets.builtin.NavBar

@FacetKey(name="navBar", profileId="standard")
class NavBar extends NavBarAll implements woko.facets.builtin.NavBar {

    @Override
    List<Link> getLinks() {
        TasteStore store = (TasteStore)woko.objectStore
        Config c = store.config
        def res = []
        if (c) {
            res << new Link('/edit/Taste?createTransient=true', 'new')
            res << new Link('/list/Taste', 'tastes')
            res << new Link('/list/Repository', 'repos')
            res << new Link('/list/Run', 'runs')
            res << new Link(woko.facetUrl('view', c), 'config')
        }
        return res
    }
}