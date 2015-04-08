package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.ViewImpl
import woko.facets.builtin.View

@FacetKey(name="view", profileId="guest", targetObjectType=Taste.class)
class ViewTasteGuest extends ViewImpl implements View {

}