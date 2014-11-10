# Selenium Goodies : meet Findr

Writing Selenium tests is not easy as it seems. Especially for today's AJAX-y apps, where the DOM is fully dynamic, with parts of the pages being rendered asynchrnously, in unpredictable orders, elements being removed and added to the page, etc. 

Of course, you have implicit/explicit waits, expected conditions etc, but they don't feel very much integrated, and don't allow for simple, clear and robust tests.

`Findr` is an API for writing rock-solid Selenium tests easily. It encapsulates all the plumbing in a consistent, simple and fluent library. It also provides some tools for booting up Selenium, recording your tests as videos, or debugging and undestanding test failures.

## The concept

The main idea behing Findr is that every assertion or action should be done inside a `WebDriverWait`. This means that instead of seeing a test as a sequence of instructions, it's more like a "chain of conditions" than needs to be all satisfied, or the test times-out.

So instead of :

```
container = findElement(By.id("container"))
button = container.findElement(By.tagName("button"))
assert button is enabled
click button
```

Should be more like :

```
when 
  By.id("container") -> By.tagName("button") -> enabled
then
  click button
```

	




