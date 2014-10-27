package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import groovy.transform.CompileStatic
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.GFindr

import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test


/**
 * Created by vankeisb on 27/10/14.
 */
@CompileStatic
class GFindrTest extends ManagedDriverJunit4TestBase {

    @Test
    void googleTest() {
        // open google home
        webDriver.get("http://www.google.com")

        GFindr g = new GFindr(findr())

        // type in our query
        g.elem {
            id('gbqfq')
            sendKeys('pojos on the web')
        }

        // click search btn
        g.elem {
            selector('button.gbqfb')
            click()
        }

        // assert results
        g.elem {
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
                    eval()
                }
            }
        }

    }

}