package com.pojosontheweb.tastecloud.facets.standard

import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.HomeImpl

@FacetKey(name="home", profileId="standard")
class Home extends HomeImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/home.jsp'
    }
}