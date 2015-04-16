package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertiesImpl

@FacetKey(name="renderProperties", profileId="standard", targetObjectType=Repository.class)
class RenderProperties extends RenderPropertiesImpl {

    @Override
    java.util.List<String> getPropertyNames() {
        ['url', 'branch', 'repositoryRuns']
    }
}