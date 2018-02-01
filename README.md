# Selenium Utils

Takes the misery out of selenium !

[![Build Status](https://travis-ci.org/pojosontheweb/selenium-utils.svg?branch=master)](https://travis-ci.org/pojosontheweb/selenium-utils)

## Findr

`Findr` is a simple yet very powerful utility class that helps to write tests in a "wait-style", without accessing WebDriverWait directly.

The API is slick, easy to use and helps to be DRY and concise. It's based on chained methods in order to expose a clear API, and uses function composition in order to create chains of conditions. This chain is then evaluated atomically inside a WebDriverWait, under the hood.

Evaluation fails if the chain doesn't completely completes within a given timeout, and an exception is thrown.

Simple example over Google search :

```
// get google
driver.get("http://www.google.com");

final Findr findr = new Findr(driver);

// perform the search
findr			
    .$("#gbqfq")            
    .sendKeys("pojos on the web", Keys.ENTER); 

// check the results
findr			
    .$("#ires")
    .$$("h3.r")
    .at(0)		
    .$("a")
    .where(textEquals("POJOs on the Web!: Woko"))
    .eval();	
```

`Findr` instances are immutable and can be safely shared and reused :

```
Findr container = new Findr(driver).$("#container");
container.$("#username").sendKeys("john.doe");
container.$("#the-button").click();
```


### Built-in predicates

The `Findrs` class exposes a set of static factory methods that create `Predicate<WebElement>`s for the recurrent stuff, for example :

* attrEquals(String attrName, String expectedValue)
* hasClass(String className)
* textEquals(final String expected)
 
Those can be used directly in your findrs :

```
new Findr(driver)
	.$("div.my-class"))
	.where(attrEquals("my-attr", "my-value"))
	.where(textEquals("This is some content"))
	.eval();
``` 

### Batch evaluation

The library also provides support for composing several Findr evaluations into
a single, retry-all operation. This allows to group a set of interactions
and make sure that all of them are actually performed.
See `com.pojosontheweb.selenium.BatchEval`.

### Error reporting

`Findr` tries to report failures in condition chains by including a String-ified version of the path. Of course, the stack trace of the Timeout exception will tell where the evaluation failed.

There are also variants to `eval()` that accept a `failureMessage` argument.

### Understanding failures

`Findr` executes the various functions you compose as a "back box", and it's sometimes 
hard to understand where it went wrong in the conditions chain. In order to get insights about what's going on, you can set the sys prop `webtests.findr.verbose`, so that it outputs the logs (to stdout) when 
asserting the condition chain. 


## WebDriver init

Use `DriverBuilder` in order to create instances of `WebDriver`. The API can be used statically :

```
// create a simple Chrome Driver
WebDriver driver = DriverBuildr
	.chrome()
	.setDriverPath(new File("/path/to/chromedriver"))
	.build();
```

Or by defining system properties :

```
WebDriver = DriverBuilder.fromSysProps().build();
```

The latter approach allows for more flexible builds. 

### System Properties

Here is a list of all supported System Properties :

<table>
	<thead>
	<tr>
		<th>property</th>
		<th>allowed values</th>
		<th>default</th>
		<th>comment</th>
	</tr>
	</thead>
	<tbody>
    <tr>
        <td colspan="4"><em>General props</em></td>
    </tr>
	<tr>
		<td>webtests.browser</td>
		<td>firefox,chrome</td>
		<td>firefox</td>
		<td></td>
	</tr>
	<tr>
		<td>webtests.locales</td>
		<td>en, fr, ...</td>
		<td></td>
		<td>Comma-separated list of locale(s) for the tests (browser language)</td>
	</tr>
	<tr>
		<td>webtests.hub.url</td>
		<td>valid remote driver url</td>
		<td></td>
		<td>Connect to a Selenium Grid (RemoteWebDriver). Video recording is not available when using remote drivers.</td>
	</tr>
	<tr>
		<td>webtests.findr.timeout</td>
		<td>Any (reasonable) positive integer</td>
		<td>10</td>
		<td>The Findr timeout in seconds</td>
	</tr>
	<tr>
		<td>webtests.findr.sleep</td>
		<td>Any (reasonable) positive long</td>
		<td>500</td>
		<td>The Findr sleep interval in milliseconds. Allows to control polling frequency.</td>
	</tr>
	<tr>
		<td>webtests.findr.verbose</td>
		<td>true,fase</td>
		<td>false</td>
		<td>log some infos about findr evaluation chains (helps debugging)</td>
	</tr>
	<tr>
		<td>webtests.video.enabled</td>
		<td>true,false</td>
		<td>false</td>
		<td>enables video recording of failed tests</td>
	</tr>
	<tr>
		<td>webtests.video.dir</td>
		<td>path to folder</td>
		<td>tmp dir</td>
		<td></td>
	</tr>
	<tr>
		<td>webtests.video.failures.only</td>
		<td>true,false</td>
		<td>true</td>
		<td>keep videos for failures only, or for all tests</td>
	</tr>
	<tr>
    	<td colspan="4"><em>Chrome only</em></td>
	</tr>
    <tr>
        <td>webdriver.chrome.driver</td>
        <td>path to driver exe</td>
        <td></td>
        <td>mandatory for Chrome</td>
    </tr>
	</tbody>
</table>

## TestCase plumbing

Base classes are included that manage the driver init/close and video stuff. If you use JUnit for example, you simply have to extend a base class :

```
public class MyTest extends ManagedDriverJunit4TestBase {

    @Test
    public void testMe()  {
    	WebDriver d = getWebDriver();
    	...
    }

}
```

Doing so will allow you to run your test directly, and parameterize it using sys props. 


> There's also a `TestUtil` class that implements the lifecycle of a typical test. You can delegate to that one if you already extend a base class in your test.

### Video recording

We have a very basic `ScreenRecordr` class that performs video capture on the host that runs the webdriver. It's activated by the TestCase plumbing, via sys props. 

It's built on [Monte Media Library](http://www.randelshofer.ch/monte/), and is pure Java. It's been tested on a different platforms (mac, windows, linux), and even seems to work in headless/xvfb environments. 

## Using with Maven

Add the dependency to your pom :

```
<dependency>
    <groupId>com.pojosontheweb</groupId>
    <artifactId>selenium-utils-core</artifactId>
    <version>LATEST-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

Configure surefire :

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
    	<systemPropertyVariables>
            <webtests.browser>${webtests.browser}</webtests.browser>
            <webtests.video.enabled>${webtests.video.enabled}</webtests.video.enabled>
            <webtests.video.dir>${project.build.directory}/webtests-videos</webtests.video.dir>
            <webdriver.chrome.driver>${webdriver.chrome.driver}</webdriver.chrome.driver>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

Invoke maven :

```
$> mvn test
```

With sys props :

```
$> mvn test -Dwebtests.browser=chrome -Dwebdriver.chrome.driver=/opt/chromedriver -Dwebtests.video.enabled=true
```

## Page Objects

Included is a simple yet useful `AbstractPageObject` class that you can use to create your own page helper libraries.

## Groovy

For Groovy users, a set of extensions and additional stuff is available as a separate module. Have
a look at [Taste](taste/README.md) for more infos.
