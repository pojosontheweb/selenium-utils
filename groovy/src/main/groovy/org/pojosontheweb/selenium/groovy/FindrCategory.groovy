package org.pojosontheweb.selenium.groovy

import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.WebElement

@Category(Findr)
class FindrCategory {

    Findr where(Closure c) {
        return this.where(c as Predicate<WebElement>);
    }

}
