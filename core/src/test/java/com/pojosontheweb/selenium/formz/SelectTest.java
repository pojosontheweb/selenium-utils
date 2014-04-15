package com.pojosontheweb.selenium.formz;

import com.pojosontheweb.selenium.Findr;
import com.pojosontheweb.selenium.ManagedDriverTestBase;
import org.junit.Test;
import org.openqa.selenium.By;

public class SelectTest extends ManagedDriverTestBase {

    @Test
    public void testSelect() {
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
