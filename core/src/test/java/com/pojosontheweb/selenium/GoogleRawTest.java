package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.openqa.selenium.*;

import static com.pojosontheweb.selenium.Findrs.textContains;
import static com.pojosontheweb.selenium.Findrs.textMatches;
import static org.junit.Assert.assertTrue;

public class GoogleRawTest {

    @Test
    @Ignore
    public void testChrome() {
        System.out.println("Testing with Chrome");
        WebDriver d = DriverBuildr.chrome().build();
        try {
            performTest(d);
        } finally {
            d.quit();
        }
    }

    @Test
    @Ignore
    public void testFirefox() {
        System.out.println("Testing with Firefox");
        WebDriver d = DriverBuildr.firefox().build();
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


    // very simple page object, just for the example...
    private static class GooglePage extends AbstractPageObject {

        GooglePage(Findr f) {
            super(f);
        }

        // example of factoring out Findrs, can be
        // final fields, or methods...
        private final Findr fSearch = $("#search");

        private Findr findResultLinkAt(int index) {
            return fSearch
                    .$$("h3.r")
                    .at(index)
                    .$("a");
        }

        // public pageobject api

        GooglePage typeQuery(String query) {
            Findr input = $("#lst-ib");
            input.click();
            input.sendKeys(query, Keys.ENTER);
            return this;
        }

        GooglePage assertResultAtContains(int index, String expectedText) {
            findResultLinkAt(index)
                    .where(textContains(expectedText))
                    .eval();
            return this;
        }

        GooglePage assertResultRegexp(int index, String regexp) {
            findResultLinkAt(index)
                    .where(textMatches(regexp))
                    .eval();
            return this;
        }

        GooglePage assertAllResultsAreNotWoko() {
            boolean failed = false;
            try {
                fSearch
                        .setTimeout(5)
                        .$$("h3.r")
                        .whereAll(textContains("POJOs on the Web"))
                        .eval();
            } catch(TimeoutException e) {
                failed = true;
            }
            assertTrue("All search hits on Woko ? I'm famous ! Oh but wait, something must be wrong ...", failed);
            return this;
        }

        GooglePage assertNoStrangeResults() {
            boolean failed = false;
            try {
                fSearch
                    .setTimeout(5)
                    .$$("h3.r")
                    .whereAny(textContains("Hot babes playing with each other in space"))
                    .eval();
            } catch(TimeoutException e) {
                failed = true;
            }
            assertTrue("We're pulling some strange hits ...", failed);
            return this;
        }

        GooglePage assertAnyWokoAndAllHaveClass() {
            fSearch
                    .setTimeout(5)
                    .$$("h3")
                    .whereAny(textContains("POJOs on the Web"))
                    .whereAll(Findrs.hasClass("r"))
                    .eval();
            return this;
        }
    }

    static void performTest(final WebDriver driver) {

        // get google
        driver.get("http://www.google.com");

        // should fail, verbose (no $, no static imports)
        boolean fail;
        try {
            new Findr(driver)
                .setTimeout(3)
                .elem(By.id("gbqfqwb"))
                .elem(By.id("gs_lc0"))
                .elem(By.id("idont"))
                .where(Findrs.isDisplayed())
                .elem(By.id("exist"))
                .eval();
            fail = false;
        } catch(TimeoutException e) {
            fail = true;
        }
        assertTrue(fail);

        final MyActions myActions = new MyActions();
        final Findr findr = new Findr(driver).setActions(myActions);
        final GooglePage google = new GooglePage(findr);

        // query, assert result...
        google
                .typeQuery("pojos on the web")
                .assertResultAtContains(0, "POJOs on the Web")
                .assertResultRegexp(0, ".*(POJOs).*");


        // any() and all()
        google
                .assertAnyWokoAndAllHaveClass()
                .assertAllResultsAreNotWoko()
                .assertNoStrangeResults();

        // assert we have clicked
        assertTrue("no clicks ???", myActions.clickCount > 0);
    }


}
