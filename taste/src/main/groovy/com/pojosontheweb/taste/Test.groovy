package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.TestUtil
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

    TestResult execute(Cfg cfg) {
        Findr.logDebug("[Test][$name] Starting")
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            // create driver
            TestUtil testUtil = new TestUtil()
            try {
                testUtil.setUp()
                WebDriver webDriver = testUtil.webDriver
                webDriver.withQuit {
                    TestContext tc = new TestContext(webDriver, name)
                    def code = body.rehydrate(tc, this, this)
                    code.resolveStrategy = DELEGATE_ONLY
                    try {
                        def res = code()
                        Findr.logDebug("[Test][$name] SUCCESS")
                        def r = new ResultSuccess(name, tc.startTime, new Date(), res)
                        if (testUtil.failuresOnly) {
                            testUtil.removeVideoFiles()
                        } else {
                            testUtil.moveVideoFiles(name)
                        }
                        return r
                    } catch (Throwable err) {
                        Findr.logDebug("[Test][$name] FAILURE")
                        def r = new ResultFailure(name, tc.startTime, new Date(), err)
                        testUtil.moveVideoFiles(name)
                        return r
                    }
                }
            } finally {
                testUtil.tearDown()
            }
        }
    }
}
