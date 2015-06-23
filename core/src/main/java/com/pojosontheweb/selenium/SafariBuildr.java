package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * WebDriver builder for Safari.
 */
public class SafariBuildr {

    public WebDriver build() {
        // did not found the way to set locales like for Firefox and Chrome for now.
        return new SafariDriver();
    }

}
