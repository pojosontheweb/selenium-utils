package com.pojosontheweb.taste

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import groovy.json.JsonBuilder
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

class Taste {

    private static void invalidArgs() {

    }

    static void main(String[] args) {

        def cli = new CliBuilder(usage:'taste [options] files...', posix: false)
        cli.b(longOpt:'browser', args:1, argName:'browser', 'browser to use (chrome or firefox, defaults to FF)')
        cli.v(longOpt:'verbose', 'show logs')
        cli.j(longOpt:'json', 'output json')

        def invalidArgs = {
            cli.usage()
            System.exit(0)
        }

        def options = cli.parse(args)

        if (!options) {
            invalidArgs()
        }

        def files = options.arguments()
        if (!files) {
            invalidArgs()
        }

        boolean verbose = false
        if (options.v) {
            verbose = true
            System.setProperty(Findr.SYSPROP_VERBOSE, "true")
        }

        if (options.b) {
            System.setProperty(DriverBuildr.SysPropsBuildr.PROP_WEBTESTS_BROWSER, options.b)
        }

        def log = { msg ->
            if (verbose) {
                println msg
            }
        }

        log("""

_/_/_/_/_/                      _/
   _/      _/_/_/    _/_/_/  _/_/_/_/    _/_/
  _/    _/    _/  _/_/        _/      _/_/_/_/
 _/    _/    _/      _/_/    _/      _/
_/      _/_/_/  _/_/_/        _/_/    _/_/_/
""")

        String fileName = files[0]

        log("- $fileName (${options.b})")

        Binding b = new Binding()
        GroovyShell shell = new CustomShell(b)
        // TODO handle cast in case folks try to do something else than running tests
        TestResult res = shell.evaluate(new InputStreamReader(new FileInputStream(fileName)))

        def printJson = {
            Map m = res.toMap()
            m['fileName'] = fileName
            println new JsonBuilder(m).toPrettyString()
        }

        if (res instanceof ResultFailure) {
            ResultFailure f = (ResultFailure)res
            if (options.j) {
                printJson()
            } else {
                println("""Test '$f.testName' FAILED : $f.err.message
- fileName      : $fileName
- startedOn     : $f.startedOn
- finishedOn    : $f.finishedOn
- stackTrace    : $f.stackTrace""")
            }
        } else if (res instanceof ResultSuccess) {
            ResultSuccess s = (ResultSuccess)res
            if (options.j) {
                printJson()
            } else {
                println("""Test '$s.testName' SUCCESS
- fileName      : $fileName
- startedOn     : $s.startedOn
- finishedOn    : $s.finishedOn
- retVal        : $s.retVal""")
            }

        } else {
            throw new IllegalStateException("File $fileName returned invalid TestResult : $res")
        }

        System.exit(0)
    }

    static TestResult test(String testName, @DelegatesTo(TestContext) Closure c) {
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {

            // create driver
            WebDriver webDriver = DriverBuildr.fromSysProps().build()

            // quit at end
            webDriver.withQuit {

                TestContext tc = new TestContext(webDriver, testName)
                def code = c.rehydrate(tc, this, this)
                code.resolveStrategy = DELEGATE_ONLY
                try {
                    def res = code()
                    new ResultSuccess(testName, tc.startTime, new Date(), res)
                } catch (Throwable err) {
                    new ResultFailure(testName, tc.startTime, new Date(), err)
                }
            }

        }
    }

}

@Mixin(DollrCategory)
class TestContext {

    private final WebDriver webDriver
    private final String testName
    private final Date startTime

    TestContext(WebDriver webDriver, String testName) {
        this.webDriver = webDriver
        this.testName = testName
        startTime = new Date()
    }

    Findr findr() {
        new Findr(webDriver)
    }

    WebDriver getWebDriver() {
        return webDriver
    }

    String getTestName() {
        return testName
    }

    Date getStartTime() {
        return startTime
    }
}

class CustomShell extends GroovyShell {


    CustomShell(Binding binding) {
        super(binding)
    }

    @Override
    protected synchronized String generateScriptName() {
        return "Skunk"

    }
}
