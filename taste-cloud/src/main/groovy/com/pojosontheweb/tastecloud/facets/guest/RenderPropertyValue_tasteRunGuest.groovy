package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.tastecloud.facets.RenderPropertyValuePre
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.jfacets.annotations.FacetKey

@FacetKey(name="renderPropertyValue_taste", profileId="guest", targetObjectType=Run.class)
class RenderPropertyValue_tasteRunGuest extends RenderPropertyValuePre {

    @Override
    String getCssClass() {
        'prettyprint'
    }
}