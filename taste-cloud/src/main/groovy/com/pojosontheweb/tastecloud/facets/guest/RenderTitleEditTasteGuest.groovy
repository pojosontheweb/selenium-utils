package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderTitleEditImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitleEdit", profileId="guest", targetObjectType=Taste.class)
class RenderTitleEditTasteGuest extends RenderTitleEditImpl implements RenderTitle {

    @Override
    String getTitle() {
        Taste t = (Taste)facetContext.targetObject
        t.id ?: 'New'
    }
}