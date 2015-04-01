package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.developer.JsonImpl
import woko.facets.builtin.Json

@FacetKey(name="json", profileId="guest", targetObjectType=Run.class)
class JsonRunGuest extends JsonImpl implements Json {

}