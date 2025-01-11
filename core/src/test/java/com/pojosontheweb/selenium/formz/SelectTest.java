package com.pojosontheweb.selenium.formz;

import com.pojosontheweb.selenium.Findr;
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class SelectTest extends ManagedDriverJunit4TestBase {

    @Test
    @Ignore
    public void testSelect() {
        getWebDriver().get("http://www.w3schools.com/tags/tryit.asp?filename=tryhtml_select");

        // dismiss cookies
//        findr().elem(By.id("accept-choices")).click();

        getWebDriver().switchTo().frame("iframeResult");

        // select helper test
        Findr fSelect = findr().elem(By.id("cars"));
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
