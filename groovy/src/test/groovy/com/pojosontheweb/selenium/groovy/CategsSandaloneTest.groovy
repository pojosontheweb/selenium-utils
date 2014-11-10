package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.TestUtil
import org.junit.Ignore
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

class CategsSandaloneTest {

    @Test
    @Ignore()
    void testCategs() {
        use(WebDriverCategory,FindrCategory,ListFindrCategory) {

            // build the driver
            WebDriver d = DriverBuildr.chrome().build()

            // use our test utils lifecycle
            TestUtil tu = d.withTestUtil 'mytest', {

                d.get('http://www.google.com')

                d.findr
                    .elem(By.id("gbqfq"))
                    .sendKeys("pojos on the web")

            }

            println "Videos dumped in ${tu.videoDir}"
        }
    }

}
