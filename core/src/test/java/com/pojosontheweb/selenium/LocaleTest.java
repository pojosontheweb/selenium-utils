package com.pojosontheweb.selenium;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LocaleTest {

    @Test
    public void testChrome() {
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocales("sl,fr")
                        .build(),
                "Iskanje Google",
                true
        );
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocales("de,en")
                        .build(),
                "Google-Suche",
                true
        );
    }

    @Test
    @Ignore
    public void testFirefox() {
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocales("sl")
                        .build(),
                "Iskanje Google",
                true
        );
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocales("de")
                        .build(),
                "Google-Suche",
                true
        );
    }

    @Test
    @Ignore
    public void testSysPropsFirefox() {
        System.setProperty("webtests.browser", "firefox");
        System.setProperty("webtests.locales", "de");
        performTest(DriverBuildr.fromSysProps().build(), "Google-Suche", true);
    }

    @Test
    @Ignore
    public void testSysPropsChrome() {
        System.setProperty("webtests.browser", "chrome");
        System.setProperty("webtests.locales", "sl");
        performTest(DriverBuildr.fromSysProps().build(), "Iskanje Google", true);
    }

    public static void performTest(final WebDriver driver, String expectedText, boolean quit) {

        try {

            // get google
            driver.get("http://www.google.com");

            // assert btn text
            new Findr(driver)
                    .elemList(By.tagName("input"))
                    .where(Findrs.attrEquals("name", "btnK"))
                    .where(Findrs.attrEquals("type", "submit"))
                    .where(Findrs.attrEquals("value", expectedText))
                    .at(0)
                    .eval();

        } finally {
            if (quit) {
                driver.quit();
            }
        }
    }


}
