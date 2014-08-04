# Selenium Utils

Takes the misery out of selenium !

## WebDriver init

Use `DrivrBuilder` in order to create instances of `WebDriver`. The API can be used statically :

```
// create a simple Chrome Driver
WebDriver driver = DriverBuildr
	.chrome()
	.setDriverPath(new File("/path/to/chromedriver"))
	.build();
```

Or by defining system properties :

```
WebDriver = DriverBuildr.fromSysProps().build();
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
		<td>webtests.findr.timeout</td>
		<td>Any (reasonable) positive integer</td>
		<td>10</td>
		<td>The default Findr timeout in seconds</td>
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

## Findr

`Findr` is a simple yet very powerful utility class that helps to write tests in a "wait-style", without accessing WebDriverWait directly.

The API is slick, easy to use and helps to be DRY and concise. It's based on chained methods in order to expose a clear API, and uses function composition in order to create chains of conditions. This chain is then evaluated atomically inside a WebDriverWait, under the hood.

Evaluation fails if the chain doesn't completely completes within a given timeout, and an exception is thrown.

Simple example over Google search :

```
// get google
driver.get("http://www.google.com");

// perform the search
new Findr(driver)			// create a Findr
	.elem(By.id("gbqfq"))  // wait for the elem located by id "gbqfq"
    .sendKeys("pojos on the web", Keys.ENTER);  // type the query

// check the results
new Findr(driver)			// create Findr
	.elem(By.id("ires"))	// wait for elem by id
    .elemList(By.cssSelector("h3.r")) // wait for a list of elements
    .at(0)					// wait for 1st in the list
    .elem(By.tagName("a"))	// wait for some <a> tag under the first list elem
    .where(Findr.textEquals("POJOs on the Web!: Woko")) // wait for the text in the link
    .eval();	// evaluate the whole stuff ! will block until success, or timeout
```

### Built-in predicates

`Findr` has a set of static factory methods that create `Predicate<WebElement>`s for the recurrent stuff, for example :

* attrEquals(String attrName, String expectedValue)
* hasClass(String className)
* textEquals(final String expected)
 
Those can be used directly in your findrs :

```
new Findr(driver)
	.elem(By.cssSelector("div.my-class"))
	.where(Findr.attrEquals("my-attr", "my-value"))
	.where(Findr.textEquals("This is some content"))
	.eval();
``` 


### Error reporting

`Findr` tries to report failures in condition chains by including a String-ified version of the path. Of course, the stack trace of the Timeout exception will tell where the evaluation failed.

There are also variants to `eval()` that accept a `failureMessage` argument.

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
