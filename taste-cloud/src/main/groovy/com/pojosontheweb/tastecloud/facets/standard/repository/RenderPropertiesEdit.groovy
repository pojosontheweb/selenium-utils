package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertiesEditImpl

@FacetKey(name="renderPropertiesEdit", profileId="standard", targetObjectType=Repository.class)
class RenderPropertiesEdit extends RenderPropertiesEditImpl {

    @Override
    java.util.List<String> getPropertyNames() {
        ['name', 'url', 'branch']
    }
}