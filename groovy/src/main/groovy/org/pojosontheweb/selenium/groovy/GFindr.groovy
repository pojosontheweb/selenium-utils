package org.pojosontheweb.selenium.groovy

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * Created by vankeisb on 27/10/14.
 */
class GFindr {

    private final Findr findr

    GFindr(Findr findr) {
        this.findr = findr
    }

    static def withDriver(WebDriver d, @DelegatesTo(GFindr) Closure c) {
        def gf = new GFindr(new Findr(d))
        def code = c.rehydrate(gf, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

    def elem(@DelegatesTo(DlgElem) Closure c) {
        def w = new DlgElem(findr: findr)
        def code = c.rehydrate(w, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

}

class DlgElem {

    Findr findr

    Findr id(String id) {
        findr = findr.elem(By.id(id))
        return findr
    }

    Findr tagName(String tagName) {
        findr = findr.elem(By.tagName(tagName))
        return findr
    }

    Findr selector(String selector) {
        findr = findr.elem(By.cssSelector(selector))
        return findr
    }

    Findr where(Closure c) {
        findr = findr.where(c as Predicate)
        return findr
    }

    def elem(@DelegatesTo(DlgElem) Closure c) {
        def w = new DlgElem(findr: findr)
        def code = c.rehydrate(w, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }


    def elemList(@DelegatesTo(DlgListElem) Closure c) {
        def w = new DlgListElem(findr: findr)
        def code = c.rehydrate(w, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

    void eval() {
        findr.eval()
    }

    def eval(Closure c) {
        return findr.eval(c as Function)
    }

    void sendKeys(CharSequence keys) {
        findr.sendKeys(keys)
    }

    void click() {
        findr.click()
    }

}

class DlgListElem {

    Findr findr
    Findr.ListFindr listFindr

    Findr.ListFindr selector(String selector) {
        listFindr = findr.elemList(By.cssSelector(selector))
        return listFindr
    }

    Findr.ListFindr where(Closure c) {
        listFindr = listFindr.where(c as Predicate)
        return listFindr
    }

    Findr.ListFindr whereElemCount(int c) {
        listFindr = listFindr.whereElemCount(c)
        return listFindr
    }

    Findr elem(@DelegatesTo(DlgListElemAt) Closure c) {
        def w = new DlgListElemAt(listFindr: listFindr)
        def code = c.rehydrate(w, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }


}

class DlgListElemAt {

    Findr.ListFindr listFindr
    Findr findr

    Findr at(int i) {
        findr = listFindr.at(i)
        return findr
    }

    def elem(@DelegatesTo(DlgElem) Closure c) {
        def w = new DlgElem(findr: findr)
        def code = c.rehydrate(w, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        return code()
    }

}

