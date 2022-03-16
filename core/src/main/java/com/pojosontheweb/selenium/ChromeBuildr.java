package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.HashMap;

/**
 * WebDriver builder for Chrome.
 */
public class ChromeBuildr {

    private File driverPath;
    private String locales;

    public static final String CHROMEDRIVER_PATH_SYSPROP_NAME = "webdriver.chrome.driver";

    public static String getChromeDriverSysPropValue() {
        return System.getProperty(CHROMEDRIVER_PATH_SYSPROP_NAME);
    }

    public WebDriver build() {
        String propPath = getChromeDriverSysPropValue();
        if (propPath==null) {
            if (driverPath != null) {
                System.setProperty(CHROMEDRIVER_PATH_SYSPROP_NAME, driverPath.getAbsolutePath());
//            } else {
//                throw new RuntimeException("Path to ChromeDriver not specified ! Please set the path either " +
//                        "by calling the builder or by setting the " + CHROMEDRIVER_PATH_SYSPROP_NAME + " System Property");
            }
        }

        return new ChromeDriver(createChromeOptions(locales));
    }

    /**
     * Create chrome options from passes locales
     * @param locales comma-separated locales
     * @return chrome options
     */
    public static ChromeOptions createChromeOptions(String locales) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        if (locales !=null) {
            HashMap<String, String> prefs = new HashMap<String, String>();
            prefs.put("intl.accept_languages", locales);
            options.setExperimentalOption("prefs", prefs);
        }
        return options;
    }

    /**
     * Set the driver path (webdriver.chrome.driver sys property).
     * @param driverPath the path to the driver
     * @return the builder, for chained calls
     */
    public ChromeBuildr setDriverPath(File driverPath) {
        this.driverPath = driverPath;
        return this;
    }

    /**
     * Set locales to be used
     * @param locales a comma-separated list of locales
     * @return the builder, for chained calls
     */
    public ChromeBuildr setLocales(String locales) {
        this.locales = locales;
        return this;
    }
}
