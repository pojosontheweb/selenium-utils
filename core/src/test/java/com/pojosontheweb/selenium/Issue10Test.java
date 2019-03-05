package com.pojosontheweb.selenium;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

public class Issue10Test extends ManagedDriverJunit4TestBase {

    @Test
    @Ignore
    public void testElemListWhereElemCount() {
        getWebDriver().get("http://woko.pojosontheweb.com");

        findr()
            .elem(id("top-nav"))
            .elemList(cssSelector("li a"))
            .where(Findrs.textEquals("Download"))
            .whereElemCount(1)
            .eval();

        // make sure you canot construct a ListFindr
        // with where() after whereElemCount()
        boolean failed = false;
        try {
            findr()
                .elem(id("top-nav"))
                .elemList(cssSelector("li a"))
                .whereElemCount(1)
                .where(Findrs.textEquals("Download"));
        } catch(IllegalArgumentException e) {
            // all good
            failed = true;
        }
        Assert.assertTrue(failed);
    }

}
