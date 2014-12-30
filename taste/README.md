# Taste: Selenium With Style

Taste is a toolkit for spicing up your Selenium. It provides Groovy sugar over Findr for more  robust, concise and clear tests. It also includes a simple yet fully functional runner for executing tests/suites, and getting back test reports.

## Fluent, stylish APIs

You need good APIs in order to write good tests. Taste relies on Findr
in order to express readable DOM selectors and predicates, and make async stuff fully
transparent.

Taste also provides additional constructs, like "dollar" functions
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

The `$` functions are inspired from JQuery : they are factories for Findr objects. You can add Predicates (or Closures) to the Findrs and compose them using `+`. Findr evaluation (or click or sendKeys) is done using the right shift `>>` operator.
Additional instructions/constructs are also provided, so that writing Findr selectors and chains is even easier.

Here below is an explanation of the main Taste functions. The full API docs are available there :

* [core Findr API](http://jdp.rvkb.com/api/selenium-utils-core/index.html) 
* [Groovy extensions](http://jdp.rvkb.com/api/selenium-utils-groovy/index.html).


### $ 

Creates a Findr instance with passed selector. Shortcut for `findr().elem(By.cssSelector(...))`. Use `$` when dealing with single elements.

	Findr f = $('#foo')
    $('#foo').click()
    $('#foo').eval { WebElement e -> ... }
    $('#foo').where { WebElement e -> e.text == 'Hello' }
    
### $$

Counterpart of `$`, but returns a ListFindr, for list of elements. Shortcut for `findr().elemList(By.cssSelector(...))`. Use `$$` when dealing with selectors/conditions
on multiple elements.

	ListFindr lf = $$('.foo')
    $$('.bar).whereElemCount(5).eval()
    $$('.bar).at(3).where(Findrs.isDisplayed()).eval()
    // you can use index-based [] notation too :
    $$('#foo .bar)[0].click()
    
> The static predicates in `Findrs` can also be `static import`s for more compact style :

    $$('.bar).at(3).where(isDisplayed()).eval()

### +

Overloaded _plus_ operator for adding ListFindrs and Predicates to Findrs. Shortcut for `Findr.elem`, `Findr.where`, or `ListFindr.where`, depending on what you add to what...

    Findr f = $('#foo') + isDisplayed()
    Findr.ListFindr lf = $('#foo') + $$('.bar') + isDisplayed() 
    Findr f = $('#foo') + $$('.bar') + at(2) + $('.baz') + textEquals('Hi There!')

### >>

_Right shift_ operator : allows for more readable `eval()`, `click()` and `sendKeys()`. Allows to bypass parenthesis and gives the whole chain and evaluation process a more natural look.

	// dotted notation (invoke click() on Findr)
    $('#foo').click() 
    // same as this :
    $('#foo') >> click()
    
    $$('#foo .bar')[5] + Findrs.textEquals("clickme") >> click()
    $$('#foo .bar')[5] + { e -> e.text=='clickme' } >> click()
    $('#foo input.bar') >> sendKeys('This is some text')
        
    // you can pass a Closure to eval() as well
    $(...) + ... >> { WebElement e -> ... }
    ... + $$(...) >> { List<WebElement> elems -> ... }

### whereElemCount, at

The `whereElemCount` and `at` static funtions can also be used, along with `$$` and `+`.

    // wait until we have 3 elems with class 'bar', 
    // that the 1st one has text 'clickme', and click it
    $$('.bar') + 
        whereElemCount(3) + 
        at(0) + 
        textEquals('clickme') >> click()

## Test runner

There are many test runners already available on the market. JUnit, TestNG... you name it.
You can of course use Findr (as well as the Groovy enhancements) with these frameworks.
Findr was initially used in JUnit test suites only.

Nonetheless, if you start from scratch, or if you simply don't want to carry the burden of a heavyweight test framework, Taste has its own runner. It makes it easy to organize tests, run them, and get test reports.

### Groovy everywhere

The runner is implemented in Groovy, and so are your tests. We just use a `.taste` file extension in order to make it even more explicit. 

It's better to use a Groovy-capable IDE (like the excellent Jetbrains' IDEA), so that you have a minimum of support for writing your tests and helpers. Nevertheless, the taste "language" is meant to be simple to use, even without an IDE. A Groovy-highlighting text editor can be sufficient for writing .taste files, at least simple ones.

### Tests and Suites

There's a DSL-like API for organizing your code into tests and suites. A `.taste` file can contain a single test, or a suite (several tests grouped together).

Here's a test (`my-test.taste`):

    import static com.pojosontheweb.taste.Taste.*

    test('My first test') {
        ...
    }


And a suite (`my-suite.taste`):

	import static com.pojosontheweb.taste.Taste.*

	suite('My Test Suite') {

		add test('My first test') {
			...
		}

		add test('My other test') {
			...
		}
	}
	
The static import `Taste.*` gives you access to `suite()` and `add test()` functions. Those functions actually create the test (or suite) that the runner will execute.

You can run the test or suite with the command line runner :

    $ taste my-test.taste

Command line options are detailed below.

### Configuration

`taste` runs can be parameterized via a config script. This allows to leave all environment
details out of the tests, and to run the tests on different configurations (e.g. different browsers).
The various `Findr` options (browser, video recording, verbosity, etc.) can be fine-tuned, depending on the context.

Again, DSL-like functions are provided, by static importing `Cfg.*`.

Here's a config example :

    import static com.pojosontheweb.taste.Cfg.*

    config {
        output {
            json()                              // we output json
        }

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

The Config script is evaluated at startup time when you launch the `taste` executable with `-c` option.

> You can also place a `~/.taste/cfg.taste` config file in your user dir, it will be used as the default, unless another one is explicitly specified.

### Install the taste runner 

Set the `TASTE_HOME` environment variable to the folder where
you have expanded `taste` :

    export TASTE_HOME=/path/to/taste

You can also add `$TASTE_HOME/bin` to your PATH, or create a sym link to
the `taste` script.

### Command line reference

The `taste` runner is launched from a command line like this :

    $ taste [options] <file>
    
This runs the test/suite defined in `<file>` with passed options.

<table>
<tbody>
<thead>
<tr>
    <th>
        option
    </th>
    <th>
        argument
    </th>
    <th>
        allowed
    </th>
    <th>
        &nbsp;
    </th>
</tr>
</thead>
<tr>
    <td>-b,--browser</td>
    <td>browser</td>
    <td>firefox | chrome</td>
    <td>The browser to be used</td>
</tr>
<tr>
    <td>-c,--config</td>
    <td>config_file</td>
    <td>path to a valid .taste config</td>
    <td>The config to be used</td>
</tr>
<tr>
    <td>-cp,--classpath</td>
    <td>classpath</td>
    <td></td>
    <td>
        A semicolon-separated class/script path. If your tests/suites imports 
        other classes, then you need to tell `taste` where to find them. Paths 
        can point to jar files, or folders.
    </td>
</tr>
<tr>
    <td>-o,--output-format</td>
    <td>output_format</td>
    <td>text | html | json</td>
    <td>The format of the output for the report. Defaults to text.</td>
</tr>
<tr>
    <td>-v,--verbose</td>
    <td>-</td>
    <td>-</td>
    <td>
    Verbose mode : Taste will then output logs for everything it does.
    </td>
</tr>

</tbody>
</table>