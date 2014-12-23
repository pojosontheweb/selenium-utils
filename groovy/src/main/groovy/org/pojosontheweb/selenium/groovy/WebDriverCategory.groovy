package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import static com.pojosontheweb.selenium.Findr.logDebug
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

    def withQuit(Closure c) {
        try {
            c()
        } finally {
            logDebug("[WebDriverCategory] quit WebDriver $this")
            quit()
        }
    }
}
