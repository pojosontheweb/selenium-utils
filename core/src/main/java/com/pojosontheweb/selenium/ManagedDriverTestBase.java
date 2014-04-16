package com.pojosontheweb.selenium;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import java.io.File;

public class ManagedDriverTestBase {

    private static final String SYS_PROP_VIDEO_ENABLED = "webtests.video.enabled";
    private static final String SYS_PROP_VIDEO_DIR = "webtests.video.dir";

    private WebDriver webDriver;
    private boolean videoEnabled = isVideoEnabledFromSysProps();
    private String videoDir = getVideoDirFromSysProps();

    protected static boolean isVideoEnabledFromSysProps() {
        String videoEnabledProp = System.getProperty(SYS_PROP_VIDEO_ENABLED, "false");
        return "true".equals(videoEnabledProp.toLowerCase());
    }

    protected static String getVideoDirFromSysProps() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return System.getProperty(SYS_PROP_VIDEO_DIR, tmpDir);
    }


    public boolean isVideoEnabled() {
        return videoEnabled;
    }

    public void setVideoEnabled(boolean videoEnabled) {
        this.videoEnabled = videoEnabled;
    }

    public String getVideoDir() {
        return videoDir;
    }

    public void setVideoDir(String videoDir) {
        this.videoDir = videoDir;
    }

    private static void log(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("ManagerDriverTestBase : ");
        for (String s : args) {
            sb.append(s);
        }
        System.out.println(sb.toString());
    }

    private static String toTestName(Description d) {
        return d.getClassName() + "." + d.getMethodName();
    }

    @Rule
    public TestRule watchman = new TestWatcher() {

        private ScreenRecordr recordr = videoEnabled ? new ScreenRecordr() : null;

        @Override
        public Statement apply(Statement base, Description description) {
            return super.apply(base, description);
        }

        @Override
        protected void succeeded(Description description) {
            // trash video
            if (recordr!=null) {
                log(toTestName(description), " : Test succeeded, removing video files");
                recordr.removeVideoFiles();
                recordr = null;
            }
        }

        @Override
        protected void failed(Throwable e, Description description) {
            // keep video for failed tests if video is on
            if (recordr!=null) {
                String testName = toTestName(description);
                log(testName + " : Test failed, moving video files to ", videoDir);
                recordr.moveVideoFilesTo(new File(videoDir), testName);
                recordr = null;
            }
        }

        @Override
        protected void starting(Description description) {
            log(toTestName(description), " : test starting");
            // init web driver before each test in ran
            webDriver = DriverBuildr.fromSysProps().build();
            // start video recorder if video is enabled
            if (recordr!=null) {
                log(toTestName(description), " : video is enabled, starting recorder");
                recordr.start();
            }
        }

        @Override
        protected void finished(Description description) {
            log(toTestName(description), " : test finished");
            // close webdriver
            if (webDriver!=null) {
                webDriver.close();
            }
            if (recordr!=null) {
                // ref should have been nulled-out unless test is skipped
                // or whatever : destroy the files in any case !!!
                recordr.removeVideoFiles();
            }
        }
    };

    protected final WebDriver getWebDriver() {
        return webDriver;
    }


    protected Findr findr() {
        return new Findr(webDriver);
    }

    protected Findr findr(int timeoutInSeconds) {
        return new Findr(webDriver, timeoutInSeconds);
    }

}
