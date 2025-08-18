package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

/**
 * WebDriver builder for Chrome.
 */
public class ChromeBuildr {

    private File driverPath;

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

        return new ChromeDriver(createChromeOptions());
    }

    /**
     * Create chrome options from passes locales
     * @param locales comma-separated locales
     * @return chrome options
     */
    public static ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
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

}
