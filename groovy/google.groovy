#!/usr/bin/env groovy

// dep on sel utils
// ----------------

@Grab(group = 'com.pojosontheweb', module = 'selenium-utils-groovy', version = 'LATEST-SNAPSHOT')

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import static org.pojosontheweb.selenium.groovy.GFindr.dsl
import static org.pojosontheweb.selenium.groovy.GFindr.quitAtEnd

// init a chrome driver
// --------------------
WebDriver driver = DriverBuildr
    .chrome()
    .setDriverPath(new File('/Users/vankeisb/bin/chromedriver')) // TODO change for your config
    .build()

// test start
// ----------

Findr.DEBUG = true

// get google home
driver.get 'http://www.google.com'

quitAtEnd(driver) {

    // allow us to use the DSL and do our business !
    dsl(driver) {

        // type in our query
        println "Typing query..."
        elem {
            id 'gbqfq'
            sendKeys 'pojos on the web'
        }

        // click search btn
        println "Click search button"
        elem {
            selector 'button.gbqfb'
            click()
        }

        // assert results
        println "Assert search result"
        elem {
            id 'search'
            elemList {
                selector 'h3.r'
                where { WebElement e ->
                    e.displayed
                }
                at(0) {
                    tagName 'a'
                    where { WebElement e ->
                        e.text.startsWith 'POJOs on the Web'
                    }
                    eval() // Don't forget eval()...
                }
            }
        }

        println "All good !"
    }
}