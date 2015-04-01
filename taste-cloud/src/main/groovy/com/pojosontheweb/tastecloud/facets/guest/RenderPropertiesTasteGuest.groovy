package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertiesImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderProperties", profileId="guest", targetObjectType=Taste.class)
class RenderPropertiesTasteGuest extends RenderPropertiesImpl implements RenderProperties {

    RenderPropertiesTasteGuest() {
        useFlatLayout = true
    }

    @Override
    List<String> getPropertyNames() {
        ['taste']
    }
}