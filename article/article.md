# Selenium Goodies : meet Findr

Writing Selenium tests is not easy as it seems. Especially for today's AJAX-y apps, where the DOM is fully 
dynamic, with parts of the pages being rendered in unpredictable orders, elements being removed and added to the 
page, etc. 

## The problem(s)

- Cannot use findElement(s) without an implicit wait
- Implicit wait doesn't solve the reference reuse problem (stale)
- Expected Conditions and findElement(s) not tightly integrated : hard to be DRY and clean
- No standard tooling (recording and debugging tests etc) 



