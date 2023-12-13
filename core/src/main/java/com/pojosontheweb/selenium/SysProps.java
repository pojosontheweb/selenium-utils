package com.pojosontheweb.selenium;

/**
 * All system properties, "strong-typed".
 */
public class SysProps {

    public static class SPWebTests {
        public final String browser = DriverBuildr.SysPropsBuildr.PROP_WEBTESTS_BROWSER;
        public final String locales = DriverBuildr.SysPropsBuildr.PROP_WEBTESTS_LOCALES;
        public final SPFindr findr = new SPFindr();
        public final SPVideo video = new SPVideo();
    }

    public static class SPFindr {
        public final String timeout = ManagedDriverJunit4TestBase.PROP_WEBTESTS_FINDR_TIMEOUT;
        public final String verbose = Findr.SYSPROP_VERBOSE;
    }

    public static class SPVideo {
        public final String enabled = TestUtil.SYS_PROP_VIDEO_ENABLED;
        public final String dir = TestUtil.SYS_PROP_VIDEO_DIR;
        public final SPFailures failures = new SPFailures();
    }

    public static class SPFailures {
        public final String only = TestUtil.SYS_PROP_VIDEO_FAILED_ONLY;
    }

    public final static SPWebTests webtests = new SPWebTests();

    public static final SPWebDriver webdriver = new SPWebDriver();

    public static class SPWebDriver {
        public static final SPChrome chrome = new SPChrome();
    }

    public static final class SPChrome {
        public final String driver = ChromeBuildr.CHROMEDRIVER_PATH_SYSPROP_NAME;
    }

}

