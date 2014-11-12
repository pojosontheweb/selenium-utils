Writing Selenium tests is not easy as it seems. Especially for today's AJAX-y apps, where the DOM is fully dynamic, with parts of the pages being rendered asynchronously, in unpredictable orders, elements being removed and added to the page, etc. 

# The problem

Bare bones Selenium has several issues :

- WebDriver is "synchronous" by default
- Implicit waits don't solve all problems
- Finders, explicit waits, and expected conditions aren't consistent
- APIs are verbose and don't allow for easy, DRY and fluent coding

## findElement not async by default

Selenium "finders" are not asynchronous-friendly by default. The `findElement(s)` methods are supposed to be used when the requested element is already present on the page. For AJAX and other asynchronous apps, this cannot work.

Here's a simple example :

	// click button by ID : this triggers an AJAX request 
	// and adds a <div id="result"> to the page
	WebElement btn = driver.findElement(By.id("myButton"));
	btn.click();

	// make sure text is now on page 
	// => fails with NoSuchElementException
	WebElement resultElem = driver.findElement(By.id("result"));
	...

As the button triggers an AJAX request, `driver.findElement(By.id("result"))` is called before the element has been added to the DOM, which leads to an exception. 

You cannot use `findElement` for elements that are not present in the page. 

## Implicit Waits : the poor man's solution

In order to solve (part of) the async problem, Selenium has implicit waits : you can tell the driver to "wait" for a given time until the element is there. Calling `findElement` will then block until the element is available, or timeout. This is a global setting, for the whole driver.

Looks pretty good, and this would actually solve our previous example, but it has other drawbacks. A major one is that it doesn't allow for reuse of WebElement instances. For example :

	// get an element and assert its value
	WebElement result = driver.findElement(By.id("result"));
	assertEquals("hi there", result.getText());
	
	// click a button that removes #result and adds it back again
	driver.findElement(By.id("myButton")).click();
	
	// attempt to reuse ref to the previous element to #result but this 
	// one has been removed/added : it ain't the same as before the
	// button click 	
	// => fails with StaleElementReferenceException
	String actualText = result.getText(); 
	...
		
The button triggers a remove/add of element `#result`, therefore you cannot reuse the first reference you obtained (`result`). The reference to the WebElement is not valid any more, so you'd need to call `findElement` again. As `findElement` are usually chained, this requires you to factor out the element lookup in helper methods, and remember to never reuse references to `WebElement`s, but instead try to find them everytime you need them.

## WebDriverWait and Expected Conditions

You can also have fine-grained control over the whole polling/waiting process, using explicit waits. You basically wrap pass your condition as a Function to `WebDriverWait`, and this one polls the DOM, and evaluates the condition against it. It blocks until the condition evaluates to true, or timeout. 

It's the only robust way to write async tests with Selenium, and is the foundation for solving all async-related problems. But explicit waits are pretty clumsy and verbose to use, and they're not very well integrated with the element lookup (`findElement`) APIs. 

Here's an example of how to use it :

	// store 'by' for later use...
	By byResult = By.id("result");
	
	// assert initial text
	WebElement result = driver.findElement(byResult);
	assertEquals("hi there", result.getText());
	
	// click a button that removes #result and adds it back again
	driver.findElement(By.id("myButton")).click();
	
	// wait for our result elem to contain expected text
    new WebDriverWait(driver)
        .until(
        	ExpectedConditions.textToBePresentInElementLocated(byResult, "expected")
        );

The last line is now async-proof : the test will actually block until the element `#result` contains text `"expected"`. But what about the other lines ? They are not : we still use `findElement` outside a `WebDriverWait`. We'd have to do is this way in order to make it really stable :

	// store 'by's for later use...
	By byResult = By.id("result");
	By byButton = By.id("myButton");
	
	// assert initial text
    new WebDriverWait(driver)
        .until(
        	ExpectedConditions.textToBePresentInElementLocated(byResult, "hi there")
        );
	
	// wait for the button to be clickable
    new WebDriverWait(driver)
        .until(
        	ExpectedConditions.elementToBeClickable(byButton)
        );
    
    // button clickable => click it
	driver.findElement(byButton).click();
	
	// wait for our result elem to contain expected text
    new WebDriverWait(driver)
        .until(
        	ExpectedConditions.textToBePresentInElementLocated(byResult, "expected")
        );

Ok, our test is now robust and will work with any kind of asynchronous behavior. But it's quite verbose, and it's a simple one. In real life, you often have much more complex finders and conditions, and you often want to factor them out nicely so that you can reuse them, and be DRY.

Explicit waits and Expected Conditions are too verbose, so you end up spending (too much) time refatoring your testing code and crafting dozens of helper methods, instead of writing actual test code.

# Findr

`Findr` encapsulates all the `WebDriverWait` plumbing into a clean, simple, and fluent API. It's a very small yet powerful utility that allows you to write reusable condition chains, and to evaluate them when needed. It brings all the power of `WebDriverWait` (and more), without the complexity.

Here's our previous test, Findr-ized :

	Findr f = new Findr(driver);
	
	// store a Findr to the result elem
	Findr result = f.elem(By.id("result"));
	
	// check initial result text
	result.where(Findrs.textEquals("hi there")).eval();
		
	// wait for btn and click it
	f.elem(By.id("myButton")).where(Findrs.isEnabled()).click();
	
	// wait for result elem
	result.where(Findrs.textEquals("expected")).eval();

A few things to notice here :

- We first create a `Findr` and store it into variable `f`. This allows us to reuse this variable later, whenever we need to create other `Findr`s from this one. We pass the `WebDriver` as a constructor arg to the `Findr`, that's all it needs for the moment.
- We call a method `elem(By)` on `f` : this creates and returns another `Findr` instance, with updated path.
- We check the initial result element text by adding another condition to 

	




