package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.ViewImpl

@FacetKey(name="view", profileId="standard", targetObjectType=Repository.class)
class View extends ViewImpl {

}