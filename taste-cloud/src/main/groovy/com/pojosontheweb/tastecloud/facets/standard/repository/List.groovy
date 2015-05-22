package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.ListTabularImpl

@FacetKey(name="list", profileId="standard", targetObjectType=Repository.class)
class List extends ListTabularImpl {

    @Override
    java.util.List<String> getPropertyNames() {
        ['name', 'branch', 'url']
    }
}