package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.DollrCategory

@Mixin(DollrCategory)
class TestContext {

    private final WebDriver webDriver
    private final String testName
    private final Date startTime

    TestContext(WebDriver webDriver, String testName) {
        this.webDriver = webDriver
        this.testName = testName
        startTime = new Date()
    }

    Findr findr() {
        new Findr(webDriver)
    }

    WebDriver getWebDriver() {
        return webDriver
    }

    String getTestName() {
        return testName
    }

    Date getStartTime() {
        return startTime
    }
}