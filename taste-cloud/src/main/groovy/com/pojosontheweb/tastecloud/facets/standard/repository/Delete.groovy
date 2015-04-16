package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.DeleteImpl

@FacetKey(name="delete", profileId="standard", targetObjectType=Repository.class)
class Delete extends DeleteImpl {

}