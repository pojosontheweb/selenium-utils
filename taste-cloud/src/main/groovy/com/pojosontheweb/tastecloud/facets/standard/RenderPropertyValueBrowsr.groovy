package com.pojosontheweb.tastecloud.facets.standard

import com.pojosontheweb.selenium.Browsr
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertyValueImpl
import woko.facets.builtin.RenderPropertyValue

@FacetKey(name="renderPropertyValue", profileId="standard", targetObjectType=Browsr.class)
class RenderPropertyValueBrowsr extends RenderPropertyValueImpl implements RenderPropertyValue {

    @Override
    String getPath() {
        "/WEB-INF/jsp/standard/renderPropertyValueBrowsr.jsp"
    }

}