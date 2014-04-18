package com.pojosontheweb.selenium;

/**
 * Represents a supported browser.
 */
public enum Browsr {

    Chrome("chrome"),
    Firefox("firefox");

    /**
     * The value of the "webtests.browser" system property
     */
    private final String sysProp;

    Browsr(String sysProp) {
        this.sysProp = sysProp;
    }

    /**
     * Return the value of the "webtests.browser" system property
     */
    public String getSysProp() {
        return sysProp;
    }
}
