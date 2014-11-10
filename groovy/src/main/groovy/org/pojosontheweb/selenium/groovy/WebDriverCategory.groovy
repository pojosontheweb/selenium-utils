package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.ScreenRecordr
import com.pojosontheweb.selenium.TestUtil
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver

@Category(WebDriver)
class WebDriverCategory {

    Findr getFindr() {
        return new Findr(this)
    }

    void setWindowSize(Dimension d) {
        manage().window().setSize(d)
    }

    def executeJavaScript(String javascript, Object[] args) {
        ((JavascriptExecutor)this).executeScript(javascript, args)
    }

    def executeJavaScript(String javascript) {
        ((JavascriptExecutor)this).executeScript(javascript, new Object[0])
    }

    TestUtil withTestUtil(String testName, Closure c) {
        TestUtil tu = new TestUtil(this)
        tu.setUp()
        try {
            c()
            if (tu.isFailuresOnly()) {
                tu.removeVideoFiles()
            } else {
                tu.moveVideoFiles(testName);
            }
        } catch(Exception e) {
            // keep video for failed tests if video is on
            tu.moveVideoFiles(testName);
        } finally {
            tu.tearDown()
        }
        return tu
    }
}
