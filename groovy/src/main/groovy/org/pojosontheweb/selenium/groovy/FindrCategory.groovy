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

    def elem(By by, @DelegatesTo(DslFindr) Closure c) {
        def df = new DslFindr(this.elem(by))
        DslFindr.rehydrateAndCall(c, df)
        return df
    }

    def byId(String id, @DelegatesTo(DslFindr) Closure c) {
        return elem(By.id(id), c)
    }

}
