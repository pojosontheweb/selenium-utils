package com.pojosontheweb.tastecloud.facets.guest

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Run
import net.sourceforge.jfacets.annotations.FacetKey
import com.pojosontheweb.tastecloud.model.Taste
import net.sourceforge.stripes.action.ActionBeanContext
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.SimpleMessage
import woko.facets.builtin.developer.SaveImpl
import woko.facets.builtin.Save

@FacetKey(name="save", profileId="guest", targetObjectType=Taste.class)
class SaveTasteGuest extends SaveImpl implements Save {

    Browsr browsr = Browsr.Firefox

    private boolean savenrun = false

    @Override
    protected void doSave(ActionBeanContext abc) {
        Taste t = (Taste)facetContext.targetObject
        if (!t.id) {
            t.id = UUID.randomUUID().toString()
        }
        woko.objectStore.save(t)
        if (!savenrun) {
            abc.messages << new SimpleMessage('Taste Script saved.')
        }
    }

    Resolution saveAndRun(ActionBeanContext abc) {
        savenrun = true
        Resolution res = getResolution(abc)
        if (abc.validationErrors.size()>0) {
            return res
        }
        Taste t = (Taste)facetContext.targetObject
        Run run = RunTasteGuest.createAndSubmitRun(woko, browsr, t.taste)
        String runUrl = "${request.contextPath}${woko.facetUrl('view', run)}"
        abc.messages.add(
            new SimpleMessage("Taste saved, run started - <a href='$runUrl' target='_blank'>${run.id}</a>")
        )
        return res
    }
}