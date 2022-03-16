package com.pojosontheweb.selenium;

import org.openqa.selenium.Keys;

import java.util.HashMap;
import java.util.Map;

import static com.pojosontheweb.selenium.Findrs.*;

public class Google extends AbstractPageObject {

    private final String locale;

    public Google(Findr findr, String locale) {
        super(findr);
        this.locale = locale;
    }

    public static final String DISMISS_SL = "Strinjam se";
    public static final String DISMISS_DE = "Ich stimme zu";
    public static final String DISMISS_EN = "I agree";

    public final static Map<String, String> DISMISS_PER_LOCALE = new HashMap<>();

    static {
        DISMISS_PER_LOCALE.put("en", DISMISS_EN);
        DISMISS_PER_LOCALE.put("de", DISMISS_DE);
        DISMISS_PER_LOCALE.put("sl", DISMISS_SL);
    }

    public Google dismissCookies() {
        String dismissText = DISMISS_PER_LOCALE.get(locale);
        if (dismissText == null) {
            dismissText = DISMISS_EN;
        }

        Findr fDialog = $$("div")
                .where(attrEquals("role", "dialog"))
                .expectOne();
        fDialog
                .where(isDisplayed())
                .$$("button div")
                .where(textEquals(dismissText))
                .expectOne()
                .click();

        fDialog
                .where(not(isDisplayed()))
                .eval();

        return this;
    }

    public Google typeQuery(String query) {
        Findr input = $$("input")
                .where(attrEquals("name", "q"))
                .expectOne();
        input.click();
        input.sendKeys(query, Keys.ENTER);
        return this;
    }

    public Google assertHasResult(String title) {
        $$("div.g h3")
                .where(textEquals(title))
                .expectOne()
                .eval();
        return this;
    }
}
