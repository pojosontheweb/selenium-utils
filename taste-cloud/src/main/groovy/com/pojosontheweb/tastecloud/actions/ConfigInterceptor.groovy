package com.pojosontheweb.tastecloud.actions

import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.controller.ExecutionContext
import net.sourceforge.stripes.controller.Interceptor
import net.sourceforge.stripes.controller.Intercepts
import net.sourceforge.stripes.controller.LifecycleStage

import javax.servlet.ServletContext

@Intercepts([LifecycleStage.BindingAndValidation])
class ConfigInterceptor implements Interceptor {

    static Boolean isConfigured(ServletContext ctx) {
        ctx.getAttribute('_taste_configured')
    }

    static void setConfigured(ServletContext ctx) {
        ctx.setAttribute('_taste_configured', true)
    }

    @Override
    Resolution intercept(ExecutionContext context) throws Exception {
        Boolean configured = isConfigured(context.actionBeanContext.servletContext)
        if (context.actionBean instanceof InitialConfigAction) {
            if (configured) {
                // We are already configured...
                throw new IllegalStateException("A configuration already exists. You should not need to configure here again.")
            } else {
                // no config yet, let it through
                return context.proceed()
            }
        } else {
            // check if the config exists
            if (configured) {
                // config already present, all good
                return context.proceed()
            } else {
                // not configured, redirect to config page
                return new RedirectResolution(InitialConfigAction.class)
            }
        }
    }
}
