package com.pojosontheweb.selenium.formz;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.pojosontheweb.selenium.Findr;
import org.openqa.selenium.WebElement;

public class Select {

    public static void selectByVisibleText(Findr selectFindr, final String text) {
        selectFindr.eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                        new org.openqa.selenium.support.ui.Select(select);
                selSelect.selectByVisibleText(text);
                return true;
            }

            @Override
            public String toString() {
                return "selectByVisibleText:" + text;
            }
        });
    }

    public static Predicate<WebElement> selectedText(final String expectedText) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement select) {
                org.openqa.selenium.support.ui.Select selSelect =
                        new org.openqa.selenium.support.ui.Select(select);
                WebElement selOption = selSelect.getFirstSelectedOption();
                if (selOption == null) {
                    return false;
                }
                String text = selOption.getText();
                if (text == null) {
                    return false;
                }
                return text.equals(expectedText);
            }

            @Override
            public String toString() {
                return "selectedText:" + expectedText;
            }

        };
    }

}
