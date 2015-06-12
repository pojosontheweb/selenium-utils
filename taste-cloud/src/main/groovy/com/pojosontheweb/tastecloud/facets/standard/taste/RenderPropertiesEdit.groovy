package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertiesEditImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderPropertiesEdit", profileId="standard", targetObjectType=Taste.class)
class RenderPropertiesEdit extends RenderPropertiesEditImpl implements RenderProperties {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/renderPropertiesEditTaste.jsp'
    }
}