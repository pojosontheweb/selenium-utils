package com.pojosontheweb.selenium;

import org.openqa.selenium.WebElement;

import java.util.function.Function;

/**
 * Central place for click/clear/sendKeys behavior. Can be extended/overriden
 * in order to change the default behavior.
 */
public class FindrActions {

	public Function<WebElement, Boolean> click() {
		return Findrs.click();
	}

	public Function<WebElement, Boolean> clear() {
		return Findrs.clear();
	}

	public Function<WebElement,Boolean> sendKeys(CharSequence... keys) {
		return Findrs.sendKeys(keys);
	}

}
