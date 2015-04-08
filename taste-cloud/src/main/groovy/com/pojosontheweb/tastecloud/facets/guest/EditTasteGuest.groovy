package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.EditImpl
import woko.facets.builtin.Edit

@FacetKey(name="edit", profileId="guest", targetObjectType=Taste.class)
class EditTasteGuest extends EditImpl implements Edit {

}