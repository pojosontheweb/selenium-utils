package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Findrs
import groovy.json.JsonBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.GroovyClass

import static com.pojosontheweb.selenium.Findr.logDebug
import static com.pojosontheweb.selenium.SysProps.*

class Taste extends Findrs {

    private static final String LOGO = """
_/_/_/_/_/                      _/
   _/      _/_/_/    _/_/_/  _/_/_/_/    _/_/
  _/    _/    _/  _/_/        _/      _/_/_/_/
 _/    _/    _/      _/_/    _/      _/
_/      _/_/_/  _/_/_/        _/_/    _/_/_/
                        Selenium With Style
"""

    static void main(String[] args) {

        // parse args

        def cli = new CliBuilder(usage:'taste [options] <file>', posix: false)
        cli.b(longOpt:'browser', args:1, argName:'browser', 'chrome|firefox')
        cli.v(longOpt:'verbose', 'show all logs')
        cli.j(longOpt:'json', 'output test results as json')
        cli.h(longOpt:'help', 'print this message')
        cli.c(longOpt:'config', args:1, argName:'config_file', 'path to a taste config file')
        cli.cp(longOpt:'classpath', args:1, argName:'paths', 'path(s) to search for scripts and classes (semicolon separated)')

        def invalidArgs = {
            println LOGO
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

        logDebug(LOGO)

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
        if (options.j) {
            jsonOutput = true
        }

        // let's go

        String fileName = files[0]

        logDebug("[Taste] evaluating $fileName")

        // configure paths of the Groovy loader
        GroovyClassLoader loader = new GroovyClassLoader()
        String scriptPaths = options.cp ?: System.getProperty('user.dir')
        scriptPaths.split(';').each { path ->
            loader.addClasspath(path)
            logDebug("[Taste] $path added to scripts paths")
        }

        // create shell and eval script
        GroovyShell shell = new CustomShell(loader, fileName)
        def res = shell.evaluate(new InputStreamReader(new FileInputStream(fileName)))

        if (res instanceof Test) {
            Test test = (Test) res
            TestResult testResult = test.execute(cfg)
            if (jsonOutput) {
                Map map = testResult.toMap()
                map['fileName'] = fileName
                println toJson(map)
            } else {
                logDebug("")
                println "Test '$test.name' executed\n"
                println printTestResult(fileName, testResult)
                printConfig(cfg)
            }

        } else if (res instanceof Suite) {
            Suite suite = (Suite)res
            SuiteResult suiteResult = suite.execute(cfg)
            if (jsonOutput) {
                Map map = suiteResult.toMap(true)
                map['fileName'] = fileName
                int nbSuccess = 0,
                    nbFailed = 0,
                    total = suiteResult.testResults.size()
                suiteResult.testResults.each { TestResult tr ->
                    if (tr instanceof ResultFailure) {
                        nbFailed++
                    } else if (tr instanceof ResultSuccess) {
                        nbSuccess++
                    }
                }
                map.total = total
                map.failed = nbFailed
                map.success = nbSuccess
                println toJson(map)
            } else {
                Map map = suiteResult.toMap(false)
                map['fileName'] = fileName
                logDebug("")
                int nbSuccess = 0,
                    nbFailed = 0,
                    total = suiteResult.testResults.size()

                StringBuilder sb = new StringBuilder()
                suiteResult.testResults.each { TestResult tr ->
                    sb << '\n' << printTestResult(fileName, tr)
                    if (tr instanceof ResultFailure) {
                        nbFailed++
                    } else if (tr instanceof ResultSuccess) {
                        nbSuccess++
                    }
                }
                def percent = nbSuccess / total * 100
                println "$suiteResult.name : $total tests, SUCCESS $nbSuccess, FAILED $nbFailed - $percent %\n"
                println toTxt(map)
                println ""
                println "Tests : "
                println sb
                println ""
                printConfig(cfg)
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

    private static def prettifyKeys(Map map) {
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

    private static def toJson(Map map) {
        new JsonBuilder(map).toPrettyString()
    }

    private static def toTxt(Map map) {
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

    private static def printTestResult(String fName, TestResult testResult) {
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
        "$prefix $testResult.testName : $status\n${toTxt(map)}"
    }

    private static def printConfig(Cfg cfg) {
        if (cfg) {
            println "Config :"
            prettifyKeys(cfg.sysProps).each { k, v->
                println "- $k : $v"
            }
        } else {
            println "Config : none."
        }
    }

}

class CustomShell extends GroovyShell {

    private final String scriptName

    CustomShell(GroovyClassLoader loader, String scriptName) {
        super(loader, new Binding())
        this.scriptName = scriptName
    }

    @Override
    protected synchronized String generateScriptName() {
        return scriptName

    }
}
