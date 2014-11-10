package com.pojosontheweb.selenium.formz;

import com.pojosontheweb.selenium.Findr;
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class SelectTest extends ManagedDriverJunit4TestBase {

    @Ignore("shaky : the page uses iframes and it doesn't seem to be ok")
    @Test
    public void testSelect() {
        Findr.DEBUG = true;

        getWebDriver().get("http://www.w3schools.com/tags/tryit.asp?filename=tryhtml_select");
        getWebDriver().switchTo().frame("view");
        Findr fSelect = findr().elem(By.tagName("select"));
        Select select = new Select(fSelect);

        // assert value
        fSelect.where(Select.selectedText("Volvo")).eval();
        // change value
        fSelect.eval(Select.makeSelectByVisibleText("Audi"));
        // assert value again
        fSelect.where(Select.selectedText("Audi")).eval();

        // same with instance methods
        select.assertSelectedText("Audi")
            .selectByVisibleText("Volvo")
            .assertSelectedText("Volvo");
    }
}
