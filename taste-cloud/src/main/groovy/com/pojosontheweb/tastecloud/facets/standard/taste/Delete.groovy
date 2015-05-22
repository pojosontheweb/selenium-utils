package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.DeleteImpl
import woko.facets.builtin.Delete

@FacetKey(name="delete", profileId="standard", targetObjectType=Taste.class)
class Delete extends DeleteImpl implements woko.facets.builtin.Delete {

}