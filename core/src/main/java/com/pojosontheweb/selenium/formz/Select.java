package com.pojosontheweb.selenium.formz;

import com.google.common.base.Function;
import org.openqa.selenium.WebElement;
import com.pojosontheweb.selenium.Findr;

public class Select {

    private final Findr findr;

    public Select(Findr findr) {
        this.findr = findr;
    }

    public void selectByVisibleText(final String text) {
        findr.eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                        new org.openqa.selenium.support.ui.Select(select);
                selSelect.selectByVisibleText(text);
                return true;
            }
        });
    }

    public void assertSelectedText(final String expectedText) {
        findr.eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                        new org.openqa.selenium.support.ui.Select(select);
                WebElement selOption = selSelect.getFirstSelectedOption();
                if (selOption == null) {
                    return null;
                }
                String text = selOption.getText();
                if (text == null) {
                    return null;
                }
                return text.equals(expectedText);
            }
        });
    }

}
