package com.pojosontheweb.tastecloud.facets.standard.config

import com.pojosontheweb.tastecloud.model.Config
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertiesEditImpl

@FacetKey(name="renderPropertiesEdit", profileId="standard", targetObjectType=Config.class)
class RenderPropertiesEdit extends RenderPropertiesEditImpl {

    @Override
    List<String> getPropertyNames() {
        ['imageName', 'webappDir', 'dockerUrl', 'dockerDir']
    }
}