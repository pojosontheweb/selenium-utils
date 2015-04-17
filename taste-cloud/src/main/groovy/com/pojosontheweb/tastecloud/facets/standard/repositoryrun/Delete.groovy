package com.pojosontheweb.tastecloud.facets.standard.repositoryrun

import com.pojosontheweb.tastecloud.model.RepositoryRun
import net.sourceforge.jfacets.annotations.FacetKey
import woko.facets.builtin.developer.DeleteImpl

@FacetKey(name="delete", profileId="standard", targetObjectType=RepositoryRun.class)
class Delete extends DeleteImpl {

}