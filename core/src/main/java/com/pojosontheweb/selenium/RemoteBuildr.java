package com.pojosontheweb.selenium;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

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
        DesiredCapabilities capabilities;

        if (browsr==null || Browsr.Firefox.equals(browsr)) {
            capabilities = DesiredCapabilities.firefox();
            FirefoxProfile profile = FirefoxBuildr.createFirefoxProfile(locales);
            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        } else {
            capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, ChromeBuildr.createChromeOptions(locales));
        }
        try {
            return new RemoteWebDriver(new URL(hubUrl), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
