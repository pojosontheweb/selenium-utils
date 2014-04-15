package com.rvkb.webtesting.core;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

public class ManagedDriverTestBase {

    private WebDriver webDriver;

    protected final WebDriver getWebDriver() {
        return webDriver;
    }

    @Before
    public void initWebDriver() {
        webDriver = DriverBuildr.fromSysProps().build();
    }

    @After
    public void closeWebDriver() {
        if (webDriver!=null) {
            webDriver.close();
        }
    }

    protected Findr findr() {
        return new Findr(webDriver);
    }

    protected Findr findr(int timeoutInSeconds) {
        return new Findr(webDriver, timeoutInSeconds);
    }

}
