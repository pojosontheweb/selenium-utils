package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.stripes.action.ActionBeanContext
import woko.facets.builtin.developer.SaveImpl
import woko.facets.builtin.Save

@FacetKey(name="save", profileId="guest", targetObjectType=Taste.class)
class SaveTasteGuest extends SaveImpl implements Save {

    @Override
    protected void doSave(ActionBeanContext abc) {
        Taste t = (Taste)facetContext.targetObject
        if (!t.id) {
            t.id = UUID.randomUUID().toString()
        }
        super.doSave(abc)
    }
}