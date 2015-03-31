package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.BaseRenderPropertyValueEdit

@FacetKey(name="renderPropertyValueEdit_taste", profileId="guest", targetObjectType=Taste.class)
class RenderPropertyValueEdit_tasteTasteGuest extends BaseRenderPropertyValueEdit {

    @Override
    String getPath() {
        '/WEB-INF/jsp/taste-editor.jsp'
    }
}