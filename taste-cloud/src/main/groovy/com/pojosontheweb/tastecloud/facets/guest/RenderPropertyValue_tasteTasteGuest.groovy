package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.facets.RenderPropertyValuePre
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste

@FacetKey(name="renderPropertyValue_taste", profileId="guest", targetObjectType=Taste.class)
class RenderPropertyValue_tasteTasteGuest extends RenderPropertyValuePre{

}