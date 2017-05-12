package com.pojosontheweb.selenium;

import org.openqa.selenium.WebDriver;

/**
 * Utility for creating "page object" helper classes.
 * Page objects are used to model pages (or page fragments)
 * and encapsulate the underlying findrs, exposing a clean,
 * explicit API for writing the tests.
 */
public abstract class AbstractPageObject {

	/**
	 * The "root" Findr for the page
	 */
	private final Findr findr;

	/**
	 * Create a page object with passed Findr as "root"
	 * @param findr the "root" findr
	 */
	public AbstractPageObject(Findr findr) {
		this.findr = findr;
	}

	/**
	 * Return the "root" findr
	 * @return the root findr
	 */
	public Findr getFindr() {
		return findr;
	}

	/**
	 * Return the WebDriver
	 * @return the WebDriver
	 */
	public WebDriver getDriver() {
		return findr.getDriver();
	}

	/**
	 * Finds from root findr : alias for <code>this.findr.$</code>
	 * @param selector the selector
	 * @return a findr for passed selector
	 */
	public Findr $(String selector) {
		return findr.$(selector);
	}

	/**
	 * Finds from root findr : alias for <code>this.findr.$</code>
	 * @param selector the selector
	 * @return a list findr for passed selector
	 */
	public Findr.ListFindr $$(String selector) {
		return findr.$$(selector);
	}

}
