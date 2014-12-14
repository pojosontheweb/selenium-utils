package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findrs
import groovy.json.JsonBuilder

import static com.pojosontheweb.selenium.Findr.logDebug
import static com.pojosontheweb.selenium.SysProps.*

class Taste extends Findrs {

    static void main(String[] args) {

        // parse args

        def cli = new CliBuilder(usage:'taste [options] <file>', posix: false)
        cli.b(longOpt:'browser', args:1, argName:'browser', 'chrome|firefox')
        cli.v(longOpt:'verbose', 'show all logs')
        cli.j(longOpt:'json', 'output test results as json')
        cli.h(longOpt:'help', 'output this message')
        cli.c(longOpt:'cfg', args:1, argName:'config_file', 'path to a taste config file')

        def invalidArgs = {
            cli.usage()
            System.exit(0)
        }

        def options = cli.parse(args)

        if (!options) {
            invalidArgs()
        }

        if (options.h) {
            invalidArgs()
        }

        def files = options.arguments()
        if (!files) {
            invalidArgs()
        }

        // load config from file

        boolean verbose = false
        if (options.v) {
            verbose = true
            System.setProperty(webtests.findr.verbose, "true")
        }

        String cfgPath = options.c?:null
        File cfgFile = new File(cfgPath ?: System.getProperty("user.home") +
                                File.separator + ".taste" +
                                File.separator + "cfg.taste")
        Cfg cfg = Cfg.load(cfgFile)
        if (cfg) {
            cfg.sysProps.each { k, v ->
                System.setProperty(k, v)
            }
        }

        def prettifyKeys = { Map map ->
            int maxLen = 0
            def sorted = map.keySet().sort()
            sorted.each { String k ->
                maxLen = Math.max(k.length(), maxLen)
            }
            Map newMap = [:]
            sorted.collect { String k ->
                int nbSpaces = maxLen - k.length()
                def pk = k + " "*nbSpaces
                newMap[pk] = map[k]
            }
            return newMap
        }


        logDebug("""
_/_/_/_/_/                      _/
   _/      _/_/_/    _/_/_/  _/_/_/_/    _/_/
  _/    _/    _/  _/_/        _/      _/_/_/_/
 _/    _/    _/      _/_/    _/      _/
_/      _/_/_/  _/_/_/        _/_/    _/_/_/
                         WebTesting With Style
""")

        if (cfg) {
            logDebug("[Taste] loaded config from ${cfgFile.absolutePath} :")
            prettifyKeys(cfg.sysProps).each { k,v ->
                logDebug("[Taste]   - $k : $v")
            }
        } else {
            logDebug("[Taste] no config file provided, will use args from command line (not provided on command line, and not found in ~/.taste/cfg.taste)")
        }

        // post-load overrides

        if (verbose) {
            logDebug("[Taste] verbose mode")
            System.setProperty(webtests.findr.verbose, "true")
        }

        if (options.b) {
            logDebug("[Taste] browser=$options.b")
            System.setProperty(webtests.browser, options.b)
        }

        boolean jsonOutput = cfg ? cfg.json : false

        // let's go

        String fileName = files[0]

        logDebug("[Taste] evaluating $fileName")

        Binding b = new Binding()
        GroovyShell shell = new CustomShell(b)
        // TODO handle cast in case folks try to do something else than running tests
        def res = shell.evaluate(new InputStreamReader(new FileInputStream(fileName)))

        def toJson = { Map map ->
            new JsonBuilder(map).toPrettyString()
        }

        def toTxt = { Map map ->
            StringBuilder buf = new StringBuilder()
            def pmap = prettifyKeys(map)
            def keys = pmap.keySet()
            for (def it = keys.iterator(); it.hasNext(); ) {
                def k = it.next(), v = pmap[k]
                buf << "- $k : $v"
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

        def printConfig = {
            if (cfg) {
                println "Config :"
                prettifyKeys(cfg.sysProps).each { k, v->
                    println "- $k : $v"
                }
            } else {
                println "Config : none."
            }
        }

        if (res instanceof Test) {
            Test test = (Test) res
            TestResult testResult = test.execute(cfg)
            if (jsonOutput) {
                Map map = testResult.toMap()
                map['fileName'] = fileName
                println toJson(map)
            } else {
                printTestResult(fileName, testResult)
                printConfig()
            }

        } else if (res instanceof Suite) {
            Suite suite = (Suite)res
            SuiteResult suiteResult = suite.execute(cfg)
            if (jsonOutput) {
                Map map = suiteResult.toMap(true)
                map['fileName'] = fileName
                println toJson(map)
            } else {
                Map map = suiteResult.toMap(false)
                map['fileName'] = fileName
                Findr.logDebug("")
                println "Suite '$suiteResult.name' executed\n\n${toTxt(map)}"
                println "\nTests (${suiteResult.testResults.size()})\n"
                suiteResult.testResults.each {
                    printTestResult(fileName, it)
                    println ""
                }
                printConfig()
            }

        } else {
            throw new IllegalStateException("file $fileName returned invalid Test : $res")
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
