package com.pojosontheweb.taste

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import groovy.transform.Immutable
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

/**
 * Created by vankeisb on 14/12/14.
 */
@Immutable
class Test {

    String name
    Closure body

    TestResult execute() {
        Findr.logDebug("[Test][$name] Starting")
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            // create driver
            WebDriver webDriver = DriverBuildr.fromSysProps().build()
            webDriver.withQuit {
                TestContext tc = new TestContext(webDriver, name)
                def code = body.rehydrate(tc, this, this)
                code.resolveStrategy = DELEGATE_ONLY
                try {
                    def res = code()
                    Findr.logDebug("[Test][$name] SUCCESS")
                    new ResultSuccess(name, tc.startTime, new Date(), res)
                } catch (Throwable err) {
                    Findr.logDebug("[Test][$name] FAILURE")
                    new ResultFailure(name, tc.startTime, new Date(), err)
                }
            }
        }
    }
}
