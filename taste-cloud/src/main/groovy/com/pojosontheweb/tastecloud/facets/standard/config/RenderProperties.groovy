package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.all.RenderPropertiesImpl

@FacetKey(name="renderProperties", profileId="standard", targetObjectType=Config.class)
class RenderProperties extends RenderPropertiesImpl {

    @Override
    List<String> getPropertyNames() {
        ['imageName', 'webappDir', 'dockerUrl', 'dockerDir', 'parallelJobs']
    }
}