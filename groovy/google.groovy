#!/usr/bin/env groovy

// dep on sel utils
// ----------------

@Grab(group = 'com.pojosontheweb', module = 'selenium-utils-groovy', version = 'LATEST-SNAPSHOT')

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import static org.pojosontheweb.selenium.groovy.GFindr.withDsl
import static org.pojosontheweb.selenium.groovy.GFindr.withQuit

// init a chrome driver
// --------------------
WebDriver d = DriverBuildr
    .chrome()
    .setDriverPath(new File('/Users/vankeisb/bin/chromedriver')) // TODO change for your config
    .build()

// test start
// ----------

Findr.DEBUG = true

// get google home
d.get 'http://www.google.com'

withQuit(d) {       // quit driver at end
    withDsl(d) {    // use the DSL

        // type in our query
        println 'Typing query...'
        elem {
            id 'gbqfq'
            sendKeys 'pojos on the web'
        }

        // click search btn
        println 'Click search button'
        elem {
            selector 'button.gbqfb'
            click()
        }

        // assert results
        println 'Assert search result'
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