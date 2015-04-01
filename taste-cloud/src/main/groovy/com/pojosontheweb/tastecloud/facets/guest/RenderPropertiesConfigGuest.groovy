package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderPropertiesImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderProperties", profileId="guest", targetObjectType=Config.class)
class RenderPropertiesConfigGuest extends RenderPropertiesImpl implements RenderProperties {

    @Override
    List<String> getPropertyNames() {
        ['webappDir', 'dockerUrl', 'dockerDir']
    }
}