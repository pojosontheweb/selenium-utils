package com.pojosontheweb.tastecloud.facets.standard.config

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import net.sourceforge.stripes.action.StrictBinding
import woko.facets.builtin.developer.ViewImpl

@StrictBinding
@FacetKey(name="view", profileId="standard", targetObjectType=Config.class)
class View extends ViewImpl {

}