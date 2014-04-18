package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;

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
            } else {
                throw new RuntimeException("Path to ChromeDriver not specified ! Please set the path either " +
                        "by calling the builder or by setting the " + CHROMEDRIVER_PATH_SYSPROP_NAME + " System Property");
            }
        }
        return new ChromeDriver();
    }

    public ChromeBuildr setDriverPath(File driverPath) {
        this.driverPath = driverPath;
        return this;
    }
}
