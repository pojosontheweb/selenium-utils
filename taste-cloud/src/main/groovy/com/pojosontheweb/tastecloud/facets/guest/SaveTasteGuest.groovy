package com.pojosontheweb.tastecloud.facets.guest

import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.SimpleMessage
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
        woko.objectStore.save(t)
        abc.messages << new SimpleMessage('Taste Script saved.')
    }
}