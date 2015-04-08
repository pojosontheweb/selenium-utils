package com.pojosontheweb.selenium;

import org.junit.Before;
import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverJunit4TestBase {

    @Before
    public void setLocale() {
        System.setProperty("webtests.locales", "en");
    }

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        GoogleRawTest.performTest(getWebDriver());
    }


}
