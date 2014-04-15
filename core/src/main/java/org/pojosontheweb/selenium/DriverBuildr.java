package org.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;

public class DriverBuildr {

    public static SysPropsBuildr fromSysProps() {
        return new SysPropsBuildr();
    }

    public static class SysPropsBuildr {

        public static final String PROP_WEBTESTS_BROWSER = "webtests.browser";

        public WebDriver build() {
            // find requested browser in sys properties
            String browserName = System.getProperty(PROP_WEBTESTS_BROWSER, "firefox");
            Browsr browsr = null;
            for (Browsr b : Browsr.values()) {
                if (b.getSysProp().equals(browserName)) {
                    browsr = b;
                    break;
                }
            }
            if (browsr==null) {
                throw new RuntimeException("Could not find browser ! " + PROP_WEBTESTS_BROWSER + "=" + browserName);
            }
            // create WebDriver using this prop
            switch (browsr) {
                case Chrome:
                    return  DriverBuildr.chrome().build();
                default:
                    return DriverBuildr.firefox().build();
                }
            }
    }

    public static ChromeBuildr chrome() {
        return new ChromeBuildr();
    }

    public static class ChromeBuildr {

        private File driverPath;

        public static final String CHROMEDRIVER_PATH_SYSPROP_NAME = "webdriver.chrome.driver";

        public static final String getChromeDriverSysPropValue() {
            return System.getProperty(CHROMEDRIVER_PATH_SYSPROP_NAME);
        }

        public WebDriver build() {
            String propPath = getChromeDriverSysPropValue();
            if (propPath==null) {
                if (driverPath != null) {
                    System.setProperty(CHROMEDRIVER_PATH_SYSPROP_NAME, driverPath.getAbsolutePath());
                } else {
                    throw new RuntimeException("Path to ChromeDriver not specified ! Please set the path either " +
                            "by calling the builder or by setting the " + CHROMEDRIVER_PATH_SYSPROP_NAME + " System Property");
                }
            }
            return new ChromeDriver();
        }

        public ChromeBuildr setDriverPath(File driverPath) {
            this.driverPath = driverPath;
            return this;
        }
    }


    public static FirefoxBuildr firefox() {
        return new FirefoxBuildr();
    }

    public static class FirefoxBuildr {

        private File path;
        private FirefoxProfile profile;

        public FirefoxBuildr setFirefoxPath(File path) {
            this.path = path;
            return this;
        }

        public FirefoxBuildr setProfile(FirefoxProfile profile) {
            this.profile = profile;
            return this;
        }

        public WebDriver build() {
            if (profile==null) {
                File tmpDir = new File(System.getProperty("java.io.tmpdir"), "wt-ffprofile");
                tmpDir.mkdir();
                profile = new FirefoxProfile(tmpDir);
            }
            if (path!=null) {
                return new FirefoxDriver(new FirefoxBinary(path), profile);
            } else {
                return new FirefoxDriver(profile);
            }
        }
    }


}
