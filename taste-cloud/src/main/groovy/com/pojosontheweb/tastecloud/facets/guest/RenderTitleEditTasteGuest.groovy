package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderTitleEditImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitleEdit", profileId="guest", targetObjectType=Taste.class)
class RenderTitleEditTasteGuest extends RenderTitleEditImpl implements RenderTitle {

    @Override
    String getTitle() {
        isNew ? 'New Taste' : super.getTitle()
    }

    boolean getIsNew() {
        facetContext.targetObject.id==null
    }

    @Override
    String getPath() {
        '/WEB-INF/jsp/guest/renderTitleTasteEditGuest.jsp'
    }
}