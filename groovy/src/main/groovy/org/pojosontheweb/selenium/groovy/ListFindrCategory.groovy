package org.pojosontheweb.selenium.groovy

import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr

@Category(Findr.ListFindr)
class ListFindrCategory {

    Findr.ListFindr where(Closure c) {
        where(c as Predicate)
    }

}
