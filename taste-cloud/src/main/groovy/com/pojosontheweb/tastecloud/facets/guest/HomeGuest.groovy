package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.HomeImpl
import woko.facets.builtin.Home

@FacetKey(name="home", profileId="guest")
class HomeGuest extends HomeImpl implements Home {

    @Override
    String getPath() {
        '/WEB-INF/jsp/guest/home.jsp'
    }
}