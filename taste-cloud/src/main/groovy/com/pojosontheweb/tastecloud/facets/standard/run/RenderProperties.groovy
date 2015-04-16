package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.RenderPropertiesImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderProperties", profileId="standard", targetObjectType=Run.class)
class RenderProperties extends RenderPropertiesImpl implements woko.facets.builtin.RenderProperties {

    RenderProperties() {
        useFlatLayout = true
    }

    @Override
    java.util.List<String> getPropertyNames() {
        ['summary', 'result', 'logs']
    }
}