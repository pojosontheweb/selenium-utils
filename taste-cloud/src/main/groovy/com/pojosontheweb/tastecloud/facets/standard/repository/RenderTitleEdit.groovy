package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.RenderTitle
import woko.facets.builtin.all.RenderTitleEditImpl

@FacetKey(name="renderTitleEdit", profileId="standard", targetObjectType=Repository.class)
class RenderTitleEdit extends RenderTitleEditImpl implements RenderTitle {

    @Override
    String getTitle() {
        isNew ? 'New Repository' : super.getTitle()
    }

    boolean getIsNew() {
        facetContext.targetObject.id==null
    }

}