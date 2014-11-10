# Selenium Goodies : meet Findr

Writing Selenium tests is not easy as it seems. Especially for today's AJAX-y apps, where the DOM is fully dynamic, with parts of the pages being rendered in unpredictable orders etc. 

## The problem


Using the "standard way" in Selenium doesn't really work now :

```
WebElement e = driver.findElement(...);
// do something with 'e'
e.click();
WebElement e2 = e.findElement(...);
// do somthing with e2
e.getText()...
```

Problem is that when you intend to click the first element,  

