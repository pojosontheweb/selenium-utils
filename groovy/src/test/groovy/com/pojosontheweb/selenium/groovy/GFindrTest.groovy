package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import groovy.transform.CompileStatic
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import static org.pojosontheweb.selenium.groovy.GFindr.from

import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test


/**
 * Created by vankeisb on 27/10/14.
 */
@CompileStatic
class GFindrTest extends ManagedDriverJunit4TestBase {

    @Test
    void google() {
        // open google home
        webDriver.get("http://www.google.com")
        from(findr()) {

            // type in our query
            elem {
                id('gbqfq')
                sendKeys('pojos on the web')
            }

            // click search btn
            elem {
                selector('button.gbqfb')
                click()
            }

            // assert results
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
                        eval()
                    }
                }
            }
        }
    }

    @Test
    void testReuse() {
        webDriver.get("http://www.google.com")
        from(findr()) {
            elem {
                id('gbqfq')
                sendKeys('pojos on the web')
            }
            elem {
                selector('button.gbqfb')
                click()
            }

            // reuse
            Findr f1 = (Findr)elem {
                id 'search'
            }

            f1.eval()
            println f1

            Findr f2 = (Findr)from(f1) {
                elemList {
                    selector 'h3.r'
                    where isDisplayed()
                    at(0) {
                        tagName 'a'
                        where { WebElement e ->
                            e.text.startsWith 'POJOs on the Web'
                        }
                    }
                }
            }

            f2.eval()
            println f2
        }
    }



}