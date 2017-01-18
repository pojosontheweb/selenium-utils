package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;

public class FirefoxBuildr {

    private File path;
    private FirefoxProfile profile;
    private String locales;
    private FirefoxBinary binary;

    public FirefoxBuildr setFirefoxPath(File path) {
        this.path = path;
        return this;
    }

    public FirefoxBuildr setFirefoxBinary(FirefoxBinary binary) {
        this.binary = binary;
        return this;
    }

    public FirefoxBuildr setProfile(FirefoxProfile profile) {
        this.profile = profile;
        return this;
    }

    public FirefoxBuildr setLocales(String locales) {
        this.locales = locales;
        return this;
    }

    public WebDriver build() {
        if (profile==null) {
            profile = createFirefoxProfile(locales);
        }
        if (binary == null) {
            return path == null
                    ? new FirefoxDriver(profile)
                    : new FirefoxDriver(new FirefoxBinary(path), profile);
        } else {
            return new FirefoxDriver(binary, profile);
        }
    }

    public static FirefoxProfile createFirefoxProfile(String locales) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "wt-ffprofile");
        tmpDir.mkdirs();
        FirefoxProfile profile = new FirefoxProfile(tmpDir);
        if (locales !=null) {
            profile.setPreference("intl.accept_languages", locales);
        }
        return profile;
    }
}
