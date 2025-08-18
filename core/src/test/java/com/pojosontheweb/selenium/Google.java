package com.pojosontheweb.selenium;

import org.openqa.selenium.Keys;

import static com.pojosontheweb.selenium.Findrs.*;

public class Google extends AbstractPageObject {

    public Google(Findr findr) {
        super(findr);
    }

    public Google typeQuery(String query) {
        Findr input = $$("form")
                .where(attrEquals("role", "search"))
                .expectOne()
                .$$("textarea")
                .expectOne();
        input.click();
        input.sendKeys(query, Keys.ENTER);
        return this;
    }

    public Google assertHasResult(String title) {
        $$("#rso h3")
                .where(textEquals(title))
                .expectOne()
                .eval();
        return this;
    }
}
