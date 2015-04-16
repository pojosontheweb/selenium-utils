package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.ListTabularImpl

@FacetKey(name="list", profileId="standard", targetObjectType=Taste.class)
class List extends ListTabularImpl {

    @Override
    java.util.List<String> getPropertyNames() {
        ['name', 'createdOn', 'lastUpdated']
    }


}