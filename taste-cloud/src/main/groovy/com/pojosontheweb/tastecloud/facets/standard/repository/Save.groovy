package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.SaveImpl

@FacetKey(name="save", profileId="standard", targetObjectType=Repository.class)
class Save extends SaveImpl {

}