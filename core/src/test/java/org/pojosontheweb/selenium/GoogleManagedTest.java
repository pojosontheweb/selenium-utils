package org.pojosontheweb.selenium;

import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverTestBase {

    @Test
    public void testWithInjectedWebDriver() {
        GoogleRawTest.performTest(getWebDriver());
    }


}
