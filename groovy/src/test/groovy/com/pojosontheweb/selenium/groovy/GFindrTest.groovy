package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findrs
import groovy.transform.CompileStatic
import org.junit.Ignore

import static org.openqa.selenium.By.*
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

import static org.pojosontheweb.selenium.groovy.GFindr.withDsl

import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test


/**
 * Created by vankeisb on 27/10/14.
 */
class GFindrTest extends ManagedDriverJunit4TestBase {

    @CompileStatic
    @Test
    @Ignore
    void google() {
        // open google home
        webDriver.get 'http://www.google.com'

        // dsl-ize !
        withDsl(webDriver) {

            // type in our query
            elem {
                id 'gbqfq'
                sendKeys 'pojos on the web'
            }

            // click search btn
            elem {
                selector 'button.gbqfb'
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

    @CompileStatic
    @Test
    @Ignore
    void testReuse() {
        webDriver.get 'http://www.google.com'
        withDsl(webDriver) {
            elem {
                id 'gbqfq'
                sendKeys 'pojos on the web'
            }
            elem {
                selector 'button.gbqfb'
                click()
            }

            // reuse
            Findr f1 = (Findr)elem {
                id 'search'
            }

            f1.eval()
            println f1

            Findr f2 = (Findr)withDsl(f1) {
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

    @Test
    @Ignore
    void dsl() {
        def d = webDriver
        d.get 'http://www.google.com'
        Findr.DEBUG = true
        use(WebDriverCategory, FindrCategory, ListFindrCategory) {
            def f = findr().setTimeout(1)
            def f2 = f.elem(id('gbqfq')) {
                where(Findrs.isDisplayed())
                where { WebElement e ->
                    e.displayed
                }
                elem(id('foo')) {
                    where { WebElement e ->
                        e.displayed
                    }
                     byId('bar') {
                        elemList(tagName('ul')) {
                            at(0) {
                                where(Findrs.isEnabled())
                            }
                        }
                    }
                }
            }

            println f2
       }
    }

}