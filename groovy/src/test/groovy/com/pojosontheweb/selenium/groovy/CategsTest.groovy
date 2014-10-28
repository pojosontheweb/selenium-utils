package com.pojosontheweb.selenium.groovy

import com.google.common.base.Function
import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findrs
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import com.pojosontheweb.selenium.formz.Select
import groovy.json.JsonBuilder
import org.junit.Assert
import org.junit.Test
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

import static com.pojosontheweb.selenium.Findrs.textEquals
import static org.openqa.selenium.By.className
import static org.openqa.selenium.By.tagName

class CategsTest extends ManagedDriverJunit4TestBase {

    @Test
    void categsLeBonCoin() {
        use(WebDriverCategory,FindrCategory,ListFindrCategory) {

            def d = webDriver

            // test start
            d.get 'http://www.leboncoin.fr'

            d.manage().window().setSize(new Dimension(1400, 1000))

            Findr f = d.findr

            f.byId('TableContentBottom')
                .elemList(tagName('a'))
                .where(Findrs.attrEquals('title', 'Alsace'))
                .at(0)
                .click()

            Select.selectByVisibleText(f.byId('search_category'), 'Motos')
            Select.selectByVisibleText(f.byId('searcharea'), 'Toute la France')

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
                .eval({ List<WebElement> elems ->
                    elems.each { WebElement e ->
                        res << [
                            text: e.text
                        ]
                        true
                    }
                } as Function)

                ((JavascriptExecutor)d).executeScript("window.scrollTo(0, document.body.scrollHeight);")

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


            println "Trouvé : ${res.size()} annonces"
            println new JsonBuilder(res).toPrettyString()

            Assert.assertEquals(70, res.size())
        }
    }

}
