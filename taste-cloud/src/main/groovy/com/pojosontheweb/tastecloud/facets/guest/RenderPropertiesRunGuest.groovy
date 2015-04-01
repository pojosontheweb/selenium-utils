package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.all.RenderPropertiesImpl
import woko.facets.builtin.RenderProperties

@FacetKey(name="renderProperties", profileId="guest", targetObjectType=Run.class)
class RenderPropertiesRunGuest extends RenderPropertiesImpl implements RenderProperties {

    @Override
    List<String> getPropertyNames() {
        ['browsr', 'startedOn', 'finishedOn', 'result', 'logs', 'taste']
    }
}