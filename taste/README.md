# Taste: Selenium With Style

`taste` is a toolkit for spicing up your Selenium Tests. It provides APIs (and DSLs) for
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

TODO describe the DSLs

## Test runner

There are many test runners already available on the market. JUnit, TestNG... you name it !
You can of course use Findr (as well as the Groovy enhancements like `$` methods) with these frameworks.
Findr was even designed before `taste`, and was initially used exclusively in JUnit test suites...

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

This creates a suite with 2 tests.

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

As the config is actually a Groovy script, it's typed, and you can do whatever you fancy in there.

### Command-line

TODO command line usage

