package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderPropertiesEditImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderPropertiesEdit", profileId="guest", targetObjectType=Taste.class)
class RenderPropertiesEditTasteGuest extends RenderPropertiesEditImpl implements RenderProperties {

    @Override
    List<String> getPropertyNames() {
        ['name', 'browsr', 'taste']
    }
}