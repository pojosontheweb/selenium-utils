package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Run
import woko.facets.builtin.developer.ViewImpl
import woko.facets.builtin.View

@FacetKey(name="view", profileId="guest", targetObjectType=Run.class)
class ViewRunGuest extends ViewImpl implements View {

}