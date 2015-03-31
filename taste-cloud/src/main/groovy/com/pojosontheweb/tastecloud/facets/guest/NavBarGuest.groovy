package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.Link
import woko.facets.builtin.all.NavBarAll
import woko.facets.builtin.NavBar

@FacetKey(name="navBar", profileId="guest")
class NavBarGuest extends NavBarAll implements NavBar {

    @Override
    List<Link> getLinks() {
        [
            new Link('/edit/Taste?createTransient=true', 'new'),
            new Link('/list/Taste', 'tastes'),
            new Link('/list/Run', 'runs')
        ]
    }
}