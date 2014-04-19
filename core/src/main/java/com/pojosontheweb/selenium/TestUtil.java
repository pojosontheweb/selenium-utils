package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;

import java.io.File;

public class TestUtil {

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

    public void log(String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("ManagerDriverTestBase : ");
        for (String s : args) {
            sb.append(s);
        }
        System.out.println(sb.toString());
    }

    private ScreenRecordr recordr = videoEnabled ? new ScreenRecordr() : null;

    public void removeVideoFiles() {
        if (recordr!=null) {
            log("removing video files");
            recordr.removeVideoFiles();
            recordr = null;
        }

    }

    public void moveVideoFiles(String testName) {
        if (recordr!=null) {
            log("moving video files to ", videoDir, " with prefix ", testName);
            recordr.moveVideoFilesTo(new File(videoDir), testName);
            recordr = null;
        }
    }

    public void setUp() {
        // init web driver before each test in ran
        webDriver = DriverBuildr.fromSysProps().build();
        // start video recorder if video is enabled
        if (recordr!=null) {
            log("video is enabled, starting recorder");
            recordr.start();
        }
    }

    public void tearDown() {
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

    public WebDriver getWebDriver() {
        return webDriver;
    }
}


