package com.pojosontheweb.selenium;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class LocaleManagedTest extends ManagedDriverJunit4TestBase {

    @BeforeClass
    public static void setSysProps() {
        System.setProperty("webtests.locales", "sl");
        System.setProperty("webtests.browser", "chrome");
    }

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        LocaleTest.performTest(getWebDriver(), "Iskanje Google", false);
    }

}
