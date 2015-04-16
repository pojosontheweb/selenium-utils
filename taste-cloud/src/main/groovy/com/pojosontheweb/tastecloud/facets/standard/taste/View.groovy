package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.ViewImpl

@FacetKey(name="view", profileId="standard", targetObjectType=Taste.class)
class View extends ViewImpl {

}