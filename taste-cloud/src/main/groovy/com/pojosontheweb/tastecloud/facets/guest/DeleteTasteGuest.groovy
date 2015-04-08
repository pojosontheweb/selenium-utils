package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.DeleteImpl
import woko.facets.builtin.Delete

@FacetKey(name="delete", profileId="guest", targetObjectType=Taste.class)
class DeleteTasteGuest extends DeleteImpl implements Delete {

}