package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import org.junit.Ignore

import static com.pojosontheweb.selenium.Findrs.*
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import groovy.json.JsonBuilder
import org.junit.Assert
import org.junit.Test
import static org.openqa.selenium.By.*
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory


class DslTest extends ManagedDriverJunit4TestBase {

    @Ignore
    @Test
    void dsl() {

        use(WebDriverCategory,FindrCategory,ListFindrCategory) {

            def d = webDriver

            // no paren is more stylish...
            d.get 'http://www.leboncoin.fr'

            // categ shortcut method
            d.windowSize = new Dimension(1400, 1000)

            // creates a default Findr for this driver
            Findr f = d.findr

            (f + id('TableContentBottom') +
                tagName('a') +
                attrEquals('title', 'Alsace')
            ).click()

            // use the Select helper
            (f+id('search_category')).asSelect().selectByVisibleText('Motos')
            f.byId('searcharea').asSelect().selectByVisibleText('Toute la France')

            f.byId('searchbutton').click()

            def res = []

            def nbPages = 2

            (1..nbPages).each { page ->
                println "page $page"
                (((f + id('ContainerMain'))*className('detail')) + { e -> e.text!=null }).e


                f.byId('ContainerMain')
                    .elemList(className('detail'))
                    .where { WebElement e ->
                        e.text!=null
                    }
                    .eval { List<WebElement> elems ->
                        elems.each { WebElement e ->
                            res << [
                                text: e.text
                            ]
                            true
                        }
                    }

                d.executeJavaScript("window.scrollTo(0, document.body.scrollHeight);")

                if (page>1) {
                    (f + id('paging') +
                        isDisplayed() +
                        tagName('a') +
                        textEquals("$page")
                    ).click()
                }
            }


            println "Found : ${res.size()} ads"
            println new JsonBuilder(res).toPrettyString()

            Assert.assertEquals(70, res.size())
        }
    }

}
