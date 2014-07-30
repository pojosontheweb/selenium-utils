package com.pojosontheweb.selenium;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LocaleTest {

    @Ignore
    @Test
    public void testChrome() {
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocale("sl")
                        .build(),
                "Iskanje Google"
        );
        performTest(
                DriverBuildr
                        .chrome()
                        .setLocale("de")
                        .build(),
                "Google-Suche"
        );
    }

    @Test
    public void testFirefox() {
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocale("sl")
                        .build(),
                "Iskanje Google"
        );
        performTest(
                DriverBuildr
                        .firefox()
                        .setLocale("de")
                        .build(),
                "Google-Suche"
        );
    }

    @Test
    public void testSysPropsFirefox() {
        System.setProperty("webtests.browser", "firefox");
        System.setProperty("webtests.locale", "de");
        performTest(DriverBuildr.fromSysProps().build(), "Google-Suche");
    }

    @Ignore
    @Test
    public void testSysPropsChrome() {
        System.setProperty("webtests.browser", "chrome");
        System.setProperty("webtests.locale", "sl");
        performTest(DriverBuildr.fromSysProps().build(), "Iskanje Google");
    }

    public static void performTest(final WebDriver driver, String expectedText) {

        try {

            // get google
            driver.get("http://www.google.com");

            // assert btn text
            new Findr(driver)
                    .elem(By.id("gbqfsa"))
                    .where(Findr.textEquals(expectedText))
                    .eval();

        } finally {
            driver.close();
        }
    }


}
