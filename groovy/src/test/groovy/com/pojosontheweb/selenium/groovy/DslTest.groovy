package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import groovy.json.JsonBuilder
import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.At
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WhereElemCount

import static org.pojosontheweb.selenium.groovy.ListFindrCategory.*

import static com.pojosontheweb.selenium.Findrs.*
import org.junit.Ignore
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test
import org.openqa.selenium.Dimension
import org.pojosontheweb.selenium.groovy.WebDriverCategory

class DslTest extends ManagedDriverJunit4TestBase {

    private Findr.ListFindr $(String selector) {
        findr().elemList(By.cssSelector(selector))
    }

    private Findr.ListFindr $(Findr f, String selector) {
        f.elemList(By.cssSelector(selector))
    }

    private static WhereElemCount whereElemCount(int i) {
        new WhereElemCount(i)
    }

    private static At at(int i) {
        new At(i)
    }

    private static Closure eval() {
        return { true }
    }

    @Test
    void dsl() {

        use(WebDriverCategory, FindrCategory, ListFindrCategory) {

            def d = webDriver
            d.get 'http://www.leboncoin.fr'
            d.windowSize = new Dimension(1400, 1000)

            assert $('#foo .bar') instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() + whereElemCount(5) instanceof Findr.ListFindr
            assert $('#foo') + isDisplayed() + whereElemCount(5) + at(0) instanceof Findr
            assert findr() + isDisplayed() instanceof Findr

            findr().elem(By.id('TableContentBottom')) + isDisplayed() >> eval()
            $('#TableContentBottom') + isDisplayed() >> eval()


            $('#TableContentBottom a') + attrEquals('title', 'Alsace') + at(0) >> click()

            $('#search_category') at 0 asSelect() selectByVisibleText('Motos')
            $('#searcharea')[0].asSelect().selectByVisibleText('Toute la France')

            $('#searchbutton')[0].click()

            def res = []

            def nbPages = 2

            (1..nbPages).each { page ->
                println "page $page"

                $('#ContainerMain .detail') + { WebElement e ->
                    e.text != null
                } >> { List<WebElement> elems ->
                    elems.each { WebElement e ->
                        res << [
                            text: e.text
                        ]
                        true
                    }
                }

                d.executeJavaScript("window.scrollTo(0, document.body.scrollHeight);")

                if (page>1) {
                    $('#paging a') +
                        textEquals("$page") +
                        isDisplayed() +
                        at(0) >> click()
                }
            }


            println "Found : ${res.size()} ads"
            println new JsonBuilder(res).toPrettyString()

            Assert.assertEquals(70, res.size())
        }
    }

}
