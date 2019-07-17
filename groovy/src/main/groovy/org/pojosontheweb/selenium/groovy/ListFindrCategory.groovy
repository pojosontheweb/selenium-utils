package org.pojosontheweb.selenium.groovy

import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement

import java.util.function.Function
import java.util.function.Predicate

@Category(Findr.ListFindr)
class ListFindrCategory {

    Findr.ListFindr where(Closure c) {
        where(c as Predicate)
    }

    public <T> T eval(Closure c) throws TimeoutException {
        eval(c as Function<List<WebElement>, T>)
    }

    public Findr getAt(int i) {
        this.at(i)
    }

    public Findr.ListFindr plus(Predicate p) {
        this.where(p)
    }

    public Findr.ListFindr plus(Closure c) {
        this.where(c as Predicate)
    }

    public Findr.ListFindr plus(WhereElemCount wec) {
        this.whereElemCount(wec.index)
    }

    public Findr plus(At at) {
        this.at(at.index)
    }

    def rightShift(Function f) {
        this.eval(f)
    }

    def rightShift(Closure c) {
        this.eval(c as Function)
    }

}

class WhereElemCount {

    private final int index

    WhereElemCount(int index) {
        this.index = index
    }

    int getIndex() {
        return index
    }
}

class At {

    private final int index

    At(int index) {
        this.index = index
    }

    int getIndex() {
        return index
    }

}
