package com.pojosontheweb.tastecloud.facets.standard.taste

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import woko.facets.builtin.developer.EditImpl
import woko.facets.builtin.Edit

@FacetKey(name="edit", profileId="standard", targetObjectType=Taste.class)
class Edit extends EditImpl implements woko.facets.builtin.Edit {

}