package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * WebDriver builder for Safari.
 */
public class SafariBuildr {

    private String locales;

    public WebDriver build() {
        // did not found the way to set locales like for Firefox and Chrome for now.
        return new SafariDriver();
    }

    /**
     * Set locales to be used
     * @param locales a comma-separated list of locales
     * @return the builder, for chained calls
     */
    public SafariBuildr setLocales(String locales) {
        this.locales = locales;
        return this;
    }

}
