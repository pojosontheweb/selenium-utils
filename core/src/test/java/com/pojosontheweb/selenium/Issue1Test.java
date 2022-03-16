package com.pojosontheweb.selenium;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Issue1Test extends ManagedDriverJunit4TestBase {

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        boolean thrown = false;
        try {
            new Findr(getWebDriver()).sendKeys("foo");
        } catch(Findr.EmptyFindrException e) {
            // that's what we want !
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }


}
