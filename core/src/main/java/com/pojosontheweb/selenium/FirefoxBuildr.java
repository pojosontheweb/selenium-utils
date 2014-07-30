package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;

public class FirefoxBuildr {

    private File path;
    private FirefoxProfile profile;
    private String locale;

    public FirefoxBuildr setFirefoxPath(File path) {
        this.path = path;
        return this;
    }

    public FirefoxBuildr setProfile(FirefoxProfile profile) {
        this.profile = profile;
        return this;
    }

    public FirefoxBuildr setLocale(String locale) {
        this.locale = locale;
        return this;
    }


    public WebDriver build() {
        if (profile==null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "wt-ffprofile");
            tmpDir.mkdir();
            profile = new FirefoxProfile(tmpDir);
            if (locale!=null) {
                profile.setPreference("intl.accept_languages", locale);
            }
        }
        if (path!=null) {
            return new FirefoxDriver(new FirefoxBinary(path), profile);
        } else {
            return new FirefoxDriver(profile);
        }
    }
}
