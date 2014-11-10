package org.pojosontheweb.selenium.groovy

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement

@Category(Findr.ListFindr)
class ListFindrCategory {

    Findr.ListFindr where(Closure c) {
        where(c as Predicate)
    }

    public <T> T eval(Closure c) throws TimeoutException {
        eval(c as Function<List<WebElement>, T>)
    }

}
