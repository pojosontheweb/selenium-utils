package com.pojosontheweb.selenium;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static com.pojosontheweb.selenium.Findrs.textEquals;

public class ShadowDomTest {

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

    private void performTest(WebDriver d) {
        d.get("http://localhost:8000");
        Findr f = new Findr(d);
        f.$$("span").where(textEquals("I'm not in shadow")).expectOne().eval();
        f.$("my-elem")
                .shadowRoot()
                .$("span")
                .where(textEquals("I'm in shadow"))
                .eval();
    }


}
