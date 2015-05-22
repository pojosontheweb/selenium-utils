package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.developer.SaveImpl

@FacetKey(name="save", profileId="standard", targetObjectType=Config.class)
class Save extends SaveImpl {

}