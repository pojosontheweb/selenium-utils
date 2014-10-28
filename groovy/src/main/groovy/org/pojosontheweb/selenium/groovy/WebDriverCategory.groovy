package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebDriver

@Category(WebDriver)
class WebDriverCategory {

    Findr getFindr() {
        return new Findr(this)
    }

    def withQuit(Closure c) {
        try {
            c()
        } finally {
            quit()
        }
    }

    static def foo()   {

    }


}
