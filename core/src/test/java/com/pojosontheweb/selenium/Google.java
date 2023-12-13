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

    public static final String DISMISS_SL = "Sprejmi vse";
    public static final String DISMISS_DE = "Alle akzeptieren";
    public static final String DISMISS_EN = "Accept all";

    public final static Map<String, String> DISMISS_PER_LOCALE = new HashMap<>();

    static {
        DISMISS_PER_LOCALE.put("en", DISMISS_EN);
        DISMISS_PER_LOCALE.put("de", DISMISS_DE);
        DISMISS_PER_LOCALE.put("sl", DISMISS_SL);
    }

    public Google dismissCookies() {
        if (Boolean.getBoolean("dismiss.cookies")) {
            String dismissText = DISMISS_PER_LOCALE.get(locale);
            if (dismissText == null) {
                dismissText = DISMISS_EN;
            }

            $$("button div")
                    .where(textEquals(dismissText))
                    .expectOne()
                    .click();
        }
        return this;
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
