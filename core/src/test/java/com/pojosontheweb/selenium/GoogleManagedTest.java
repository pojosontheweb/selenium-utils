package com.pojosontheweb.selenium;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class GoogleManagedTest extends ManagedDriverJunit4TestBase {

    @Override
    protected WebDriver createWebDriver() {
        System.setProperty("webtests.locales", "en,fr");
        System.setProperty("webtests.browser", "chrome");
        return super.createWebDriver();
    }

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        GoogleRawTest.performTest(getWebDriver());
    }


}
