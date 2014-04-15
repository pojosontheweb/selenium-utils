package com.pojosontheweb.selenium;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;

public class GoogleManagedTest extends ManagedDriverTestBase {

    @Test
    public void testWithInjectedWebDriver() throws Exception {
        try {
            GoogleRawTest.performTest(getWebDriver());
        } catch(Exception e) {
            File scrFile = ((TakesScreenshot)getWebDriver()).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("/tmp/testWithInjectedWebDriver.png"));
            throw e;
        }
    }


}
