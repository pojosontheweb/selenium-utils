package org.pojosontheweb.selenium.groovy

import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.formz.Select
import org.openqa.selenium.By

@Category(Findr)
class FindrCategory {

    Findr byId(String id) {
        elem(By.id(id))
    }

    Findr where(Closure c) {
        where(c as Predicate)
    }

    Select asSelect() {
        return new Select(this)
    }

    def plus(By by) {
        elem(by)
    }

    def plus(Closure c) {
        where(c as Predicate)
    }

    def plus(Predicate p) {
        where(p)
    }

}
