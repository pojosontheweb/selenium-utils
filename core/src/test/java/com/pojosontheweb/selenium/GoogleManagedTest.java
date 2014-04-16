package com.pojosontheweb.selenium;

import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverTestBase {

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        GoogleRawTest.performTest(getWebDriver(), "testWithInjectedWebDriver");
    }


}
