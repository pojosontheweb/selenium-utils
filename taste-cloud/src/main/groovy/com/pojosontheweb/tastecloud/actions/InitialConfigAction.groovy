package com.pojosontheweb.tastecloud.actions

import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.woko.TasteStore
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.UrlBinding
import net.sourceforge.stripes.validation.Validate
import net.sourceforge.stripes.validation.ValidateNestedProperties
import woko.actions.BaseActionBean


@UrlBinding("/initial-config")
class InitialConfigAction extends BaseActionBean {

    @ValidateNestedProperties([
        @Validate(field = 'webappDir', required = true),
        @Validate(field = 'dockerDir', required = true),
        @Validate(field = 'dockerUrl', required = true)
    ])
    Config config

    @DontValidate
    @DefaultHandler
    Resolution display() {
        new ForwardResolution('/WEB-INF/jsp/initial-config.jsp')
    }

    Resolution configure() {
        TasteStore store = (TasteStore)woko.objectStore
        store.save(config)
        ConfigInterceptor.setConfigured(context.servletContext)
        new RedirectResolution("/")
    }

}
