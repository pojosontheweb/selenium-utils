package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.selenium.Browsr
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="guest", targetObjectType=Browsr.class)
class RenderPropertyValueBrowsrGuest extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/guest/renderPropertyValueBrowsrGuest.jsp"
    }

}