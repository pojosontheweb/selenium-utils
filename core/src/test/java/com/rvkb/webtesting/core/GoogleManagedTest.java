package com.rvkb.webtesting.core;

import org.junit.Test;

public class GoogleManagedTest extends ManagedDriverTestBase {

    @Test
    public void testWithInjectedWebDriver() {
        GoogleRawTest.performTest(getWebDriver());
    }


}
