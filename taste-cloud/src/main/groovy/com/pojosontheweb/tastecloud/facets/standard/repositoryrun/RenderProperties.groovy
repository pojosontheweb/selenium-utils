package com.pojosontheweb.tastecloud.facets.standard.repositoryrun

import com.pojosontheweb.tastecloud.model.RepositoryRun
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.all.RenderPropertiesImpl

@FacetKey(name="renderProperties", profileId="standard", targetObjectType=RepositoryRun.class)
class RenderProperties extends RenderPropertiesImpl {

    @Override
    List<String> getPropertyNames() {
        ['repository', 'branch', 'revision', 'queuedOn', 'startedOn', 'finishedOn', 'runs']
    }
}