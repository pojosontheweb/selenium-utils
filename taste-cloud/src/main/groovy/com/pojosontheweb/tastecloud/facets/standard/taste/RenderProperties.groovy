package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertiesImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderProperties", profileId="standard", targetObjectType=Taste.class)
class RenderProperties extends RenderPropertiesImpl implements woko.facets.builtin.RenderProperties {

    RenderProperties() {
        useFlatLayout = true
    }

    @Override
    java.util.List<String> getPropertyNames() {
        ['taste']
    }
}