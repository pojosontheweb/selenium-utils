package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.By

/**
 * Created by vankeisb on 28/10/14.
 */
class DslFindr {

    private Findr findr

    DslFindr(Findr findr) {
        this.findr = findr
    }

    @Override
    def methodMissing(String name, args) {
        def res = findr.invokeMethod(name, args)
        if (res instanceof Findr) {
            findr = (Findr)res
            return this
        } else if (res instanceof Findr.ListFindr) {
            return new DslListFindr((Findr.ListFindr)res)
        }
        return res
    }

    def elem(By by, @DelegatesTo(DslFindr) Closure c) {
        findr = findr.elem(by)
        rehydrateAndCall(c, this)
    }

    def elemList(By by, @DelegatesTo(DslFindr) Closure c) {
        def dlf = new DslListFindr(findr.elemList(by))
        rehydrateAndCall(c, dlf)
    }


    static void rehydrateAndCall(Closure c, Object delegate) {
        def code = c.rehydrate(delegate, null, null)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

}
