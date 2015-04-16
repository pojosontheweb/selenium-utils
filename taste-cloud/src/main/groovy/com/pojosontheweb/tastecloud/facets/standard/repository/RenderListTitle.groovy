package com.pojosontheweb.tastecloud.facets.standard.repository

import com.pojosontheweb.tastecloud.model.Repository
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.BaseFragmentFacet

@FacetKey(name='renderListTitle', profileId = 'standard', targetObjectType = Repository.class)
class RenderListTitle extends BaseFragmentFacet implements woko.facets.builtin.RenderListTitle {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/renderListTitleRepository.jsp'
    }

    @Override
    String getTitle() {
        return 'Repositories'
    }
}
