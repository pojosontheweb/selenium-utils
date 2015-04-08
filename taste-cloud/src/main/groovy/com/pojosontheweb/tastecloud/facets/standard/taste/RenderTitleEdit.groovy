package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.all.RenderTitleEditImpl
import woko.facets.builtin.RenderTitle

@FacetKey(name="renderTitleEdit", profileId="standard", targetObjectType=Taste.class)
class RenderTitleEdit extends RenderTitleEditImpl implements RenderTitle {

    @Override
    String getTitle() {
        isNew ? 'New Taste' : super.getTitle()
    }

    boolean getIsNew() {
        facetContext.targetObject.id==null
    }

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/renderTitleTasteEdit.jsp'
    }
}