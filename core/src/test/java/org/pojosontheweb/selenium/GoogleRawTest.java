package org.pojosontheweb.selenium;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.pojosontheweb.selenium.DriverBuildr;
import org.pojosontheweb.selenium.Findr;

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

    public static void performTest(WebDriver driver) {
        try {

            // get google
            driver.get("http://www.google.com");

            // type in our query
            new Findr(driver, 2)
                    .elem(By.id("gbqfq"))
                    .sendKeys("pojos on the web", Keys.ENTER);

            // check the results
            new Findr(driver, 2)
                    .elem(By.id("ires"))
                    .elemList(By.cssSelector("h3.r"))
                    .at(0)
                    .elem(By.tagName("a"))
                    .where(Findr.textEquals("POJOs on the Web!: Woko"))
                    .eval();

            System.out.println("OK !");

        } finally {
            driver.close();
        }
    }


}
