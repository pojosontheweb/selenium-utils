package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import org.openqa.selenium.WebElement;

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
