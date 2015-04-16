package com.pojosontheweb.tastecloud.facets.standard

import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.HomeImpl

@FacetKey(name="home", profileId="standard")
class Home extends HomeImpl {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/home.jsp'
    }

    Integer getNbRuns() {
        TasteStore s = (TasteStore)woko.objectStore
        s.list(s.getClassMapping(Run.class), 0, 1).totalSize
    }
}