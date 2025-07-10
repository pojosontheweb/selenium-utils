package com.pojosontheweb.selenium;

import org.junit.Test;
import org.junit.Ignore;
import org.openqa.selenium.*;

import java.util.function.Function;

import static com.pojosontheweb.selenium.Findrs.textContains;
import static com.pojosontheweb.selenium.Findrs.textMatches;
import static org.junit.Assert.assertTrue;

public class GoogleRawTest {

    @Test
    public void testChrome() {
        System.out.println("Testing with Chrome");
        WebDriver d = DriverBuildr.chrome().setLocales("en,fr").build();
        try {
            performTest(d);
        } finally {
            d.quit();
        }
    }

    @Test
    public void testFirefox() {
        System.out.println("Testing with Firefox");
        WebDriver d = DriverBuildr.firefox().setLocales("en,fr").build();
        try {
            performTest(d);
        } finally {
            d.quit();
        }
    }

    // just for the demo
    private static class MyActions extends FindrActions {

        int clickCount = 0;

        @Override
        public Function<WebElement, Boolean> click() {
            clickCount++;
            return super.click();
        }

    }

    static void performTest(final WebDriver driver) {

        // get google
        // driver.get("http://www.google.com");
        driver.get("http://localhost:8000");

        MyActions myActions = new MyActions();
        Findr f = new Findr(driver).setActions(myActions);
        Google g = new Google(f, "en");

        g
                .dismissCookies()
                .typeQuery("selenium-utils github pojosontheweb")
                .assertHasResult("pojosontheweb/selenium-utils");

        // assert we have clicked
        assertTrue("no clicks ???", myActions.clickCount > 0);
    }

}
