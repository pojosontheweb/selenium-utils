package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.By

@Category(Object)
class DollrCategory {

    Findr.ListFindr $(String selector) {
        findr().elemList(By.cssSelector(selector))
    }

    Findr.ListFindr $(Findr f, String selector) {
        f.elemList(By.cssSelector(selector))
    }

    WhereElemCount whereElemCount(int i) {
        new WhereElemCount(i)
    }

    At at(int i) {
        new At(i)
    }

    Closure eval() {
        return { true }
    }
}
