package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;

public class DriverBuildr {

    public static SysPropsBuildr fromSysProps() {
        return new SysPropsBuildr();
    }

    public static class SysPropsBuildr {

        public static final String PROP_WEBTESTS_BROWSER = "webtests.browser";

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
            if (browsr==null) {
                throw new RuntimeException("Could not find browser ! " + PROP_WEBTESTS_BROWSER + "=" + browserName);
            }
            // create WebDriver using this prop
            switch (browsr) {
                case Chrome:
                    return  DriverBuildr.chrome().build();
                default:
                    return DriverBuildr.firefox().build();
                }
            }
    }

    public static ChromeBuildr chrome() {
        return new ChromeBuildr();
    }


    public static FirefoxBuildr firefox() {
        return new FirefoxBuildr();
    }


}
