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
        Findr select = findr().elem(By.tagName("select"));

        // assert value
        select.where(Select.selectedText("Volvo")).eval();

        // change value
        Select.selectByVisibleText(select, "Audi");

        // assert value again
        select.where(Select.selectedText("Audi")).eval();
    }
}
