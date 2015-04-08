package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import woko.facets.builtin.developer.EditImpl
import woko.facets.builtin.Edit

@FacetKey(name="edit", profileId="guest", targetObjectType=Config.class)
class EditConfigGuest extends EditImpl implements Edit {

}