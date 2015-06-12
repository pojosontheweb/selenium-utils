package com.pojosontheweb.selenium;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.*;

public class GoogleRawTest {

    @Test
    public void testChrome() {
        System.out.println("Testing with Chrome");
        performTest(
            DriverBuildr
                .chrome()
                .build()
        );
    }

    @Test
    public void testFirefox() {
        System.out.println("Testing with Firefox");
        performTest(
            DriverBuildr
                .firefox()
                .build()
        );
    }

    public static void performTest(final WebDriver driver) {

        try {

            // get google
            driver.get("http://www.google.com");

            // should fail
            boolean fail;
            try {
                new Findr(driver)
                    .setTimeout(5)
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
            Assert.assertTrue(fail);

            // type in our query
            new Findr(driver)
                    .setTimeout(5)
                    .elem(By.id("lst-ib"))
                    .sendKeys("pojos on the web");
            new Findr(driver)
                    .elem(By.cssSelector("button.lsb"))
                    .where(Findrs.attrEquals("name", "btnG"))
                .click();

            // check the results
            new Findr(driver)
                    .elem(By.id("search"))
                    .elemList(By.cssSelector("h3.r"))
                    .at(0)
                    .elem(By.tagName("a"))
                    .where(Findrs.textContains("POJOs on the Web"))
                    .eval();

            System.out.println("OK !");

            // any() and all()
            new Findr(driver)
                    .elem(By.id("search"))
                    .elemList(By.cssSelector("h3.r"))
                    .whereAny(Findrs.textContains("POJOs on the Web"))
                    .whereAll(Findrs.hasClass("r"))
                    .eval();

            try {
                new Findr(driver)
                        .setTimeout(5)
                        .elem(By.id("search"))
                        .elemList(By.cssSelector("h3.r"))
                        .whereAll(Findrs.textContains("POJOs on the Web"))
                        .eval();
                fail = false;
            } catch(TimeoutException e) {
                fail = true;
            }
            Assert.assertTrue("All search hits on Woko ? I'm famous ! Oh but wait, something must be wrong ...", fail);

            try {
                new Findr(driver)
                        .setTimeout(5)
                        .elem(By.id("search"))
                        .elemList(By.cssSelector("h3.r"))
                        .whereAny(Findrs.textContains("Hot babes playing with each other in space"))
                        .eval();
                fail = false;
            } catch(TimeoutException e) {
                fail = true;
            }
            Assert.assertTrue("We're pulling some strange hits ...", fail);


            // regexp matching
            new Findr(driver)
                    .elem(By.id("search"))
                    .elemList(By.cssSelector("h3.r"))
                    .at(0)
                    .elem(By.tagName("a"))
                    .where(Findrs.textMatches(".*(POJOs).*"))
                    .eval();

            System.out.println("Regexp OK !");

        } finally {
            driver.quit();
        }
    }


}
