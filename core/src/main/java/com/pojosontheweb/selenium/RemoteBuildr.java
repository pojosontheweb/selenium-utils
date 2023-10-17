package com.pojosontheweb.selenium;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Remote (selenium grid) driver builder.
 */
public class RemoteBuildr {

    private String hubUrl;
    private Browsr browsr;
    private String locales;

    public RemoteBuildr setHubUrl(String hubUrl) {
        this.hubUrl = hubUrl;
        return this;
    }

    public RemoteBuildr setBrowsr(Browsr browsr) {
        this.browsr = browsr;
        return this;
    }

    public RemoteBuildr setLocales(String locales) {
        this.locales = locales;
        return this;
    }

    public RemoteWebDriver build() {
        try {
            if (browsr==null || Browsr.Firefox.equals(browsr)) {
                FirefoxOptions browserOptions = new FirefoxOptions();
                FirefoxProfile profile = FirefoxBuildr.createFirefoxProfile(locales);
                browserOptions.setProfile(profile);
                return new RemoteWebDriver(new URL(hubUrl), browserOptions);
            } else {
                ChromeOptions browserOptions = new ChromeOptions();
                browserOptions.setCapability(ChromeOptions.CAPABILITY, ChromeBuildr.createChromeOptions(locales));
                return new RemoteWebDriver(new URL(hubUrl), browserOptions);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
