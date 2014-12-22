# Taste: Selenium With Style

`taste` is a toolkit for spicing up your Selenium Tests. It provides APIs for
writing robust, concise and clear tests. It also includes a simple yet fully functional runner for executing tests/suites, and getting back test reports.

## Fluent, stylish APIs

You need good APIs in order to write good tests. `taste` relies on Findr
in order to express readable DOM selectors and predicates, and make async stuff fully
transparent.

`taste` also provides additional constructs, like "dollar" functions
(`$` and `$$`) and overloaded operators (`+` and `>>`), in order
to make test writing as easy as it should be.

Here's a simple example using dollar functions, operators, and other niceties :

	webDriver.get 'http://www.google.com'

	$('#gbqfq') >> sendKeys('pojos on the web')

	$('button.gbqfb') >> click()

	$$('#search h3.r') +
		textContains('POJOs on the Web') +
		at(0) +
		$('a') +
		textContains('POJOs on the Web') >> eval()

The `$` functions are inspired from JQuery : they are factories for Findr objects. You can add Predicates
(or Closures) to the Findrs and compose them using `+`. Findr evaluation (or click or sendKeys) is done using
the right shift `>>` operator.
Additional instructions/constructs are also provided, so that writing `Findr` chains is 
even easier.

All this is strongly typed : if you use a Groovy-capable IDE, you should have editor support (errors, completion etc.).

### Reference

#### $ 

Function that creates a Findr instance with passed selector. Shortcut for `findr().elem(By.cssSelector(...))`. 

Examples :

    $('#foo').click()
    $('#foo').eval { WebElement e -> ... }
    $('#foo').where { WebElement e -> e.text == 'Hello' }
    
#### $$

Counterpart of `$`, but returns a ListFindr, for list of elements. Shortcut for `findr().elemList(By.cssSelector(...))`.

Examples :

    $$('.bar).whereElemCount(5).eval()
    $$('.bar).at(3).where(Findrs.isDisplayed()).eval()
    $$('#foo .bar)[0].click()
    
The static predicates in Findrs can also be `static import`s for more compact style :

    $$('.bar).at(3).where(isDisplayed()).eval()

#### +

Overloaded "plus" operator for adding ListFindrs and Predicates to Findrs. Shortcut for `Findr.elem`, `Findr.where`, or `ListFindr.where`, depending on what you add to what...

Examples : 

    Findr f = $('#foo') + Findrs.isDisplayed()
    Findr.ListFindr lf = $('#foo') + $$('.bar') + isDisplayed() 

#### >>

"Right shift" operator : allows for more fluent `eval()`, `click()` and `sendKeys()`. Allows to bypass parenthesis and gives the whole chain and evaluation process a more natural look.

Examples :

    $('#foo').click() 
    $('#foo') >> click()
    $$('#foo .bar')[5] + Findrs.textEquals("clickme") >> click()
    $$('#foo .bar')[5] + { e -> e.text=='clickme' } >> click()

#### whereElemCount, at

The `whereElemCount` and `at` static funtions can also be used, along with `$$` and `+` :

    // wait until we have 3 elems with class 'bar', 
    // that the 1st one has text 'clickme', and click it
    $$('.bar') + 
        whereElemCount(3) + 
        at(0) + 
        textEquals('clickme') >> click()

## Test runner

There are many test runners already available on the market. JUnit, TestNG... you name it !
You can of course use Findr (as well as the Groovy enhancements like `$` methods) with these frameworks.
Findr was even designed before `taste`, and was initially used in JUnit test suites...

Nonetheless, if you start from scratch, or if you simply don't want to carry the burden of a heavyweight test framework, `taste` has its own test runner. It makes it easy to organize tests, run them, and get test reports.

### Tests and Suites

There's a DSL for organizing your code into tests and suites. Here's a simple example :

	import static com.pojosontheweb.taste.Taste.*

	suite('My Test Suite') {

		add test('My first test') {
			...
		}

		add test('My other test') {
			...
		}
	}

This creates a suite with 2 tests. You only need to static import all functions in `Taste` in 
order to have the testing "DSL".

### Configuration

`taste` runs can be parameterized via a config script. This allows to leave all environment
details out of the tests, and to run the tests on different configurations (e.g. different browsers).
The various `Findr` options
(browser, video, verbosity, etc.) can be fine-tuned, depending on the context.

Here's a config example :

	import static com.pojosontheweb.taste.Cfg.*

	config {

		json true                               // we output json

		chrome {                                // we use chrome
		
			// GString inside !
			driverPath  "${System.getProperty('user.home')}/chromedriver"
		}

		sysProps['foo.bar'] = 'baz'             // this is how you define/override System Properties

		locales "en", "fr"                      // locale(s) to be used

		findr {
			timeout 10                          // 10s findr timeout
			verbose true                        // tell me everything
		}

		video {
			enabled false                       // enable/disable video recording
			dir "/tmp/taste-videos"             // where to store videos
			failuresOnly true                   // record everything/failures only
		}

	}

Config is evaluated at startup time (when you launch the `taste` executable) from command-line options.
You can also place a default `~/.taste/cfg.taste` in your user dir, it will then be used as the default
one.

This file is actually a Groovy script, so you can do whatever you fancy in there in order to create your Taste config.

### Command-line

See the instructions in INSTALL.md.