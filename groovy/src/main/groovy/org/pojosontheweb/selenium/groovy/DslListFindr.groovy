package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr

/**
 * Created by vankeisb on 28/10/14.
 */
class DslListFindr {

    private Findr.ListFindr listFindr

    DslListFindr(Findr.ListFindr listFindr) {
        this.listFindr = listFindr
    }

    def methodMissing(String name, args) {
        def res = listFindr.invokeMethod(name, args)
        if (res instanceof Findr) {
            return new DslFindr((Findr)res)
        } else if (res instanceof Findr.ListFindr) {
            listFindr = res
            return this
        }
        return res
    }

}
