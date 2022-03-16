package com.pojosontheweb.selenium;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;

import java.io.File;

import static com.pojosontheweb.selenium.Findrs.*;

public class LocaleTest {

    public static final String BTN_TEXT_SL = "Iskanje Google";
    public static final String BTN_TEXT_DE = "Google Suche";

    @Test
    public void testChrome() {
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocales("sl,fr")
                        .build(),
                "sl",
                BTN_TEXT_SL,
                true
        );
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocales("de,en")
                        .build(),
                "de",
                BTN_TEXT_DE,
                true
        );
    }

    @Test
    public void testFirefox() {
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocales("sl")
                        .build(),
                "sl",
                BTN_TEXT_SL,
                true
        );
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocales("de")
                        .build(),
                "de",
                BTN_TEXT_DE,
                true
        );
    }

    @Test
    @Ignore
    public void testSysPropsFirefox() {
        System.setProperty("webtests.browser", "firefox");
        System.setProperty("webtests.locales", "de");
        performTest(DriverBuildr.fromSysProps().build(), "de", BTN_TEXT_DE, true);
    }

    @Test
    @Ignore
    public void testSysPropsChrome() {
        System.setProperty("webtests.browser", "chrome");
        System.setProperty("webtests.locales", "sl");
        performTest(DriverBuildr.fromSysProps().build(), "sl", BTN_TEXT_SL, true);
    }

    public static void performTest(final WebDriver driver, String locale, String expectedText, boolean quit) {

        try {

            // create "root" findr
            Findr f = new Findr(driver);

            // get google
            driver.get("http://www.google.com");

            Google g = new Google(f, locale);
            g.dismissCookies();

            // assert btn text
            f
                    .$$("input")
                    .where(attrEquals("name", "btnK"))
                    .where(attrEquals("type", "submit"))
                    .where(attrEquals("value", expectedText))
                    .at(0)
                    .eval();

        } finally {
            if (quit) {
                driver.quit();
            }
        }
    }


}
