package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Config
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.StrictBinding
import woko.facets.builtin.developer.ViewImpl
import woko.facets.builtin.View

@StrictBinding
@FacetKey(name="view", profileId="guest", targetObjectType=Config.class)
class ViewConfigGuest extends ViewImpl implements View {

}