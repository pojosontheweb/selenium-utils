package com.pojosontheweb.taste

import com.pojosontheweb.selenium.DriverBuildr
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

/**
 * Created by vankeisb on 14/12/14.
 */
class Test {

    String name
    Closure body

    def execute() {
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            // create driver
            WebDriver webDriver = DriverBuildr.fromSysProps().build()
            webDriver.withQuit {
                TestContext tc = new TestContext(webDriver, name)
                def code = body.rehydrate(tc, this, this)
                code.resolveStrategy = DELEGATE_ONLY
                try {
                    def res = code()
                    new ResultSuccess(name, tc.startTime, new Date(), res)
                } catch (Throwable err) {
                    new ResultFailure(name, tc.startTime, new Date(), err)
                }
            }
        }

    }
}
