package org.pojosontheweb.selenium.groovy

import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.By

@Category(Findr)
class FindrCategory {

    Findr byId(String id) {
        elem(By.id(id))
    }

    Findr where(Closure c) {
        where(c as Predicate)
    }

}
