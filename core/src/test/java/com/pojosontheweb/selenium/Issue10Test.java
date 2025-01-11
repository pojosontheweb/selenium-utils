package com.pojosontheweb.selenium;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static com.pojosontheweb.selenium.Findrs.textEquals;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

public class Issue10Test extends ManagedDriverJunit4TestBase {

    @Test
    public void testElemListWhereElemCount() {
        getWebDriver().get("https://en.wikipedia.org");

        findr().
            $$("#ca-viewsource span")
                    .where(textEquals("View source"))
                    .whereElemCount(1)
                    .eval();

        // make sure you canot construct a ListFindr
        // with where() after whereElemCount()
        boolean failed = false;
        try {
            findr().
                $$("#ca-viewsource span")
                    .where(textEquals("View source"))
                    .whereElemCount(1)
                    .where(textEquals("Download"));
        } catch(IllegalArgumentException e) {
            // all good
            failed = true;
        }
        Assert.assertTrue(failed);
    }

}
