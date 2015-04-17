package com.pojosontheweb.tastecloud.facets.standard.repositoryrun

import com.pojosontheweb.tastecloud.model.RepositoryRun
import net.sourceforge.jfacets.annotations.FacetKey
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import woko.facets.BaseFragmentFacet
import woko.facets.ResolutionFacet

@FacetKey(name='runsFragment',profileId = 'standard', targetObjectType = RepositoryRun.class)
class RunsFragment extends BaseFragmentFacet implements ResolutionFacet {

    @Override
    String getPath() {
        '/WEB-INF/jsp/standard/repoRunsFragment.jsp'
    }

    @Override
    Resolution getResolution(ActionBeanContext abc) {
        new ForwardResolution(path)
    }
}
