package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.developer.EditImpl

@FacetKey(name="edit", profileId="standard", targetObjectType=Config.class)
class Edit extends EditImpl {

}