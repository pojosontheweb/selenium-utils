package com.pojosontheweb.selenium.formz;

import com.pojosontheweb.selenium.Findr;
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SelectTest extends ManagedDriverJunit4TestBase {

    @Test
    public void testSelect() {
        WebDriver d = getWebDriver();
        getWebDriver().get("http://localhost:8000");
        Findr f = new Findr(d);

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
