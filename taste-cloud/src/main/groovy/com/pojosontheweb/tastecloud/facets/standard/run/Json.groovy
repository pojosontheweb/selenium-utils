package com.pojosontheweb.tastecloud.facets.standard.run

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.developer.JsonImpl
import woko.facets.builtin.Json

@FacetKey(name="json", profileId="standard", targetObjectType=Run.class)
class Json extends JsonImpl implements woko.facets.builtin.Json {

}