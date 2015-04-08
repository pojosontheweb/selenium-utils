package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.developer.SaveImpl
import woko.facets.builtin.Save

@FacetKey(name="save", profileId="guest", targetObjectType=Config.class)
class SaveConfigGuest extends SaveImpl implements Save {

}