package com.pojosontheweb.selenium.groovy

import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.GFindr

import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test


/**
 * Created by vankeisb on 27/10/14.
 */
class GFindrTest extends ManagedDriverJunit4TestBase {

    @Test
    void googleTest() {
        // open google home
        webDriver.get("http://www.google.com")

        GFindr.withDriver(webDriver) {

            // to test scopes
            println "test scope 1 : $webDriver"

            // type in our query
            elem {
                id('gbqfq')
                sendKeys('pojos on the web')

                // to test scopes
                println "test scope 2 : $webDriver"

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
                    elem {
                        at 0
                        elem {
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
    }

//    @Test
//    void doIt() {
//
//        webDriver.get("http://www.google.com");
//
//        GFindr.withDriver(webDriver) {
//
//            println "With eval"
//
//            println elem {
//                id 'gbqfq'
//                eval { WebElement e ->
//                    assert e.getAttribute('id') == 'gbqfq'
//                    return "element text = ${e.text.trim()}"
//                }
//            }
//
//            println "Findr retrieval"
//
//            println "#1"
//            println elem {
//                id 'gbqfq'
//                elemList {
//                    selector '.foo'
//                }
//            }
//
//            println "#2"
//            println elem {
//                id 'gbqfq'
//                elemList {
//                    selector '.foo'
//                    whereElemCount 5
//                    elem {
//                        at 3
//                    }
//                }
//            }
//
//            println "done"
//        }
//
//
////        def f = gf.elem {
////            id 'gbqfq'
////            elemList {
////                selector '.foo'
////                whereElemCount 5
////                elem {
////                    at 3
////                }
////            }
////        }
////        println f
//
////        def res = gf
////            .elem {
////                id "foo"
////                where { e ->
////                    e.text != 'foo'
////                }
////                elemList {
////                    selector ".foo .bar"
////                    where { e ->
////
////                    }
////                    whereElemCount 5
////                    at 3
////                }
////            }
////
////        println res
////
////
////        // type in our query
////        new Findr(driver)
////            .elem(By.id("gbqfq"))
////            .sendKeys("pojos on the web");
////        new Findr(driver)
////            .elem(By.cssSelector("button.gbqfb"))
////            .click();
////
////        // check the results
////        new Findr(driver)
////            .elem(By.id("res"))
////            .elemList(By.cssSelector("h3.r"))
////            .at(0)
////            .elem(By.tagName("a"))
////            .elem(By.tagName("em"))
////            .where(Findrs.textEquals("POJOs on the Web"))
////            .eval();
//
//        System.out.println("OK !");
//
//
//
////        elem cssSelector('foo')
////        where { WebElement e ->
////
////        }
////        elemList cssSelector('bar')
////        filter { WebElement e ->
////
////        }
//    }

}