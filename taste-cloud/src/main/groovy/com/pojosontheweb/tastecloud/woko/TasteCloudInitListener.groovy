package com.pojosontheweb.tastecloud.woko

import woko.Woko

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class TasteCloudInitListener implements ServletContextListener {

    /**
     * Close Woko
     */
    public final void contextDestroyed(ServletContextEvent e) {
        Woko woko = Woko.getWoko(e.getServletContext());
        if (woko != null) {
            woko.close();
        }
    }

    @Override
    void contextInitialized(ServletContextEvent servletContextEvent) {
        // nothing to do
    }
}
