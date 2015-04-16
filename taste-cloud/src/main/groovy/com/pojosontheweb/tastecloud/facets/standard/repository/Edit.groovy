package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.EditImpl

@FacetKey(name="edit", profileId="standard", targetObjectType=Repository.class)
class Edit extends EditImpl {

}