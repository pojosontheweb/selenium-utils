package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.model.Config
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertiesEditImpl

@FacetKey(name="renderPropertiesEdit", profileId="guest", targetObjectType=Config.class)
class RenderPropertiesEditConfigGuest extends RenderPropertiesEditImpl {

    @Override
    List<String> getPropertyNames() {
        ['webappDir', 'dockerUrl', 'dockerDir']
    }
}