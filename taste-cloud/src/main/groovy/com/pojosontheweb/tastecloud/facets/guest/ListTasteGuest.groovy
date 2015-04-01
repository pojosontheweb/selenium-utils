package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.ListTabularImpl

@FacetKey(name="list", profileId="guest", targetObjectType=Taste.class)
class ListTasteGuest extends ListTabularImpl {

    @Override
    List<String> getPropertyNames() {
        ['id', 'name']
    }
}