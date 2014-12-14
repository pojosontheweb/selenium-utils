package com.pojosontheweb.taste

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findrs
import groovy.json.JsonBuilder

class Taste extends Findrs {

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

        log("""_/_/_/_/_/                      _/
   _/      _/_/_/    _/_/_/  _/_/_/_/    _/_/
  _/    _/    _/  _/_/        _/      _/_/_/_/
 _/    _/    _/      _/_/    _/      _/
_/      _/_/_/  _/_/_/        _/_/    _/_/_/
""")

        String fileName = files[0]

        log("[Taste] Running $fileName (${options.b})...")

        Binding b = new Binding()
        GroovyShell shell = new CustomShell(b)
        // TODO handle cast in case folks try to do something else than running tests
        def res = shell.evaluate(new InputStreamReader(new FileInputStream(fileName)))

        log("[Taste] '$fileName' evaluated")

        def toJson = { Map map ->
            new JsonBuilder(map).toPrettyString()
        }

        def toTxt = { Map map ->
            StringBuilder buf = new StringBuilder()
            def keys = map.keySet()
            for (def it = keys.iterator(); it.hasNext(); ) {
                def k = it.next(), v = map[k]
                buf << "- $k\t: $v"
                if (it.hasNext()) {
                    buf << "\n"
                }
            }
            buf.toString()
        }

        def printTestResult = { String fName, TestResult testResult ->
            Map map = testResult.toMap()
            map['fileName'] = fName
            String status
            String prefix
            if (testResult instanceof ResultFailure) {
                status = "FAILED"
                prefix = "!"
            } else {
                status = "SUCCESS"
                prefix = ">"
            }
            println "$prefix $testResult.testName : $status\n${toTxt(map)}"
        }

        if (res instanceof Test) {
            Test test = (Test) res
            TestResult testResult = test.execute()
            if (options.j) {
                Map map = testResult.toMap()
                map['fileName'] = fileName
                println toJson(map)
            } else {
                printTestResult(fileName, testResult)
            }

        } else if (res instanceof Suite) {
            Suite suite = (Suite)res
            SuiteResult suiteResult = suite.execute()
            if (options.j) {
                Map map = suiteResult.toMap(true)
                map['fileName'] = fileName
                println toJson(map)
            } else {
                Map map = suiteResult.toMap(false)
                map['fileName'] = fileName
                Findr.logDebug("")
                println "Suite executed\n\n${toTxt(map)}"
                println "\nTests (${suiteResult.testResults.size()})\n"
                suiteResult.testResults.each {
                    printTestResult(fileName, it)
                    println ""
                }
            }

        } else {
            throw new IllegalStateException("File $fileName returned invalid Test : $res")
        }

        System.exit(0)
    }

    static Test test(String testName, @DelegatesTo(TestContext) Closure c) {
        new Test(name: testName, body: c)
    }

    static Suite suite(String testName, @DelegatesTo(Suite) Closure c) {
        new Suite(name: testName, body: c)
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
