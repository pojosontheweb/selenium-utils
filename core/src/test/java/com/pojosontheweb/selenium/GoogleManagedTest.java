package com.pojosontheweb.selenium;

import org.junit.Ignore;
import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverJunit4TestBase {

    @Test
    @Ignore
    public void testWithInjectedWebDriver() throws Exception {
        GoogleRawTest.performTest(getWebDriver());
    }


}
