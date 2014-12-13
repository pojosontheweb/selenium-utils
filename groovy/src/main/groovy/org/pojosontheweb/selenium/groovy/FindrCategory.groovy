package org.pojosontheweb.selenium.groovy

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findr.ListFindr
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

    Findr plus(Predicate p) {
        where(p)
    }

    Findr plus(Closure c) {
        where(c as Predicate)
    }

    Findr plus(By by) {
        elem(by)
    }

    ListFindr plus(ListFindr lf) {
        append(lf)
    }

    def rightShift(Function f) {
        eval(f)
    }

    def rightShift(Closure c) {
        eval(c as Function)
    }

}
