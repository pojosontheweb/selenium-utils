package com.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import static com.pojosontheweb.selenium.Findrs.*
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import groovy.json.JsonBuilder
import org.junit.Assert
import org.junit.Test
import org.openqa.selenium.Dimension
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

import static com.pojosontheweb.selenium.Findrs.textEquals
import static org.openqa.selenium.By.className
import static org.openqa.selenium.By.tagName

class CategsManagedTest extends ManagedDriverJunit4TestBase {

    @Test
    void categsLeBonCoin() {

        use(WebDriverCategory,FindrCategory,ListFindrCategory) {

            def d = webDriver

            // no paren is more stylish...
            d.get 'http://www.leboncoin.fr'

            // categ shortcut method
            d.windowSize = new Dimension(1400, 1000)

            // creates a default Findr for this driver
            Findr f = d.findr

            // click on the 1st link with title "Alsace"
            f.byId('TableContentBottom')
                .elemList(tagName('a'))
                .where(attrEquals('title', 'Alsace'))
                .at(0)
                .click()

            // use the Select helper
            f.byId('search_category').asSelect().selectByVisibleText('Motos')
            f.byId('searcharea').asSelect().selectByVisibleText('Toute la France')

            f.byId('searchbutton').click()

            def res = []

            def nbPages = 2

            (1..nbPages).each { page ->
                println "page $page"
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
                    f.byId('paging')
                        .where { WebElement e ->
                            e.displayed
                        }
                        .elemList(tagName('a'))
                            .where(textEquals("$page"))
                            .at(0)
                            .click()
                }
            }


            println "Found : ${res.size()} ads"
            println new JsonBuilder(res).toPrettyString()

            Assert.assertEquals(70, res.size())
        }
    }

}
