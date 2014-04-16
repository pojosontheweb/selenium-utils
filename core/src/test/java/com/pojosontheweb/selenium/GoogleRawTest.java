package com.pojosontheweb.selenium;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.io.File;

public class GoogleRawTest {

    @Test
    public void testChrome() {
        System.out.println("Testing with Chrome");
        performTest(
                DriverBuildr
                        .chrome()
                        .build(),
                "testChrome"
        );
    }

    @Test
    public void testFirefox() {
        System.out.println("Testing with Firefox");
        performTest(
                DriverBuildr
                        .firefox()
                        .build(),
                "testFirefox"
        );
    }

    public static void performTest(WebDriver driver, String videoFileName) {

        ScreenRecordr r = new ScreenRecordr();
        r.start();

        try {

            // get google
            driver.get("http://www.google.com");

            // type in our query
            new Findr(driver)
                    .elem(By.id("gbqfq"))
                    .sendKeys("pojos on the web", Keys.ENTER);

            // check the results
            new Findr(driver)
                    .elem(By.id("ires"))
                    .elemList(By.cssSelector("h3.r"))
                    .at(0)
                    .elem(By.tagName("a"))
                    .where(Findr.textEquals("POJOs on the Web!: Woko"))
                    .eval();

            System.out.println("OK !");

        } finally {
            driver.close();
            r.moveVideoFilesTo(new File("/tmp"), videoFileName);
        }
    }


}
