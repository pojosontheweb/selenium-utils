package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;

/**
 * Builds WebDrivers !
 * Uses system properties and/or API in order to create the appropriate driver.
 */
public class DriverBuildr {

    /**
     * Create and return a builder from System properties.
     */
    public static SysPropsBuildr fromSysProps() {
        return new SysPropsBuildr();
    }

    /**
     * Builds driver from system properties.
     */
    public static class SysPropsBuildr {

        static final String PROP_WEBTESTS_BROWSER = "webtests.browser";
        static final String PROP_WEBTESTS_LOCALES = "webtests.locales";
        static final String PROP_WEBTESTS_HUB_URL = "webtests.hub.url";

        public WebDriver build() {
            // find requested browser in sys properties
            String browserName = System.getProperty(PROP_WEBTESTS_BROWSER, "firefox");
            Browsr browsr = null;
            for (Browsr b : Browsr.values()) {
                if (b.getSysProp().equals(browserName)) {
                    browsr = b;
                    break;
                }
            }
            if (browsr == null) {
                throw new RuntimeException("Could not find browser ! " + PROP_WEBTESTS_BROWSER + "=" + browserName);
            }
            String locale = System.getProperty(PROP_WEBTESTS_LOCALES);
            if (browsr.equals(Browsr.Chrome)) {
                ChromeBuildr b = chrome();
                if (locale != null) {
                    b.setLocales(locale);
                }
                return b.build();
            } else if (browsr.equals(Browsr.Safari)) {
                SafariBuildr b = safari();
                return b.build();
            } else {
                FirefoxBuildr b = firefox();
                if (locale != null) {
                    b.setLocales(locale);
                }
                return b.build();
            }
        }
    }

    /**
     * Create and return a ChromeBuildr instance.
     */
    public static ChromeBuildr chrome() {
        return new ChromeBuildr();
    }

    /**
     * Create and return a FirefoxBuildr instance.
     */
    public static FirefoxBuildr firefox() {
        return new FirefoxBuildr();
    }

    /**
     * Create and return a SafariBuildr instance.
     */
    public static SafariBuildr safari() {
        return new SafariBuildr();
    }

}
