package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

public class ChromeBuildr {

    private File driverPath;
    private String locale;

    public static final String CHROMEDRIVER_PATH_SYSPROP_NAME = "webdriver.chrome.driver";

    public static String getChromeDriverSysPropValue() {
        return System.getProperty(CHROMEDRIVER_PATH_SYSPROP_NAME);
    }

    public WebDriver build() {
        String propPath = getChromeDriverSysPropValue();
        if (propPath==null) {
            if (driverPath != null) {
                System.setProperty(CHROMEDRIVER_PATH_SYSPROP_NAME, driverPath.getAbsolutePath());
            } else {
                throw new RuntimeException("Path to ChromeDriver not specified ! Please set the path either " +
                        "by calling the builder or by setting the " + CHROMEDRIVER_PATH_SYSPROP_NAME + " System Property");
            }
        }
        ChromeOptions options = new ChromeOptions();
        if (locale!=null) {
            options.addArguments("--lang=" + locale);
        }
        return new ChromeDriver(options);
    }

    public ChromeBuildr setDriverPath(File driverPath) {
        this.driverPath = driverPath;
        return this;
    }

    /**
     * Set the driver locale
     * @param locale two letters to represent the locale, or two letters + country
     * @return this
     */
    public ChromeBuildr setLocale(String locale) {
        this.locale = locale;
        return this;
    }
}
