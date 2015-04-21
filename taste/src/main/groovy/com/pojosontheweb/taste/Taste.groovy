package com.pojosontheweb.taste

import com.google.common.base.Function
import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.Findrs

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
        cli.o(longOpt:'output-format', args:1, argName:'output_format', 'text|html|json')
        cli.h(longOpt:'help', 'print this message')
        cli.c(longOpt:'config', args:1, argName:'config_file', 'path to a taste config file')
        cli.d(longOpt:'output-dir', args:1, argName:'output_dir', 'directory where the report(s) should be stored')
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

        // taste file to run
        String fileName = files[0]

        // do we output to file ?
        String outDir = options.d
        if (outDir!='false') {
            File outDirF = new File(outDir)
            outDirF.mkdirs()
            // redirect Findr logging to file
            File logFile = new File(outDir + File.separator + "${new File(fileName).name}.output.txt")
            logFile.text = ''
            Findr.setDebugHandler(new Function<String, Object>() {
                @Override
                Object apply(String input) {
                    println input
                    logFile.append(input + '\n')
                }
            })
        } else {
            outDir = null
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

        Writer out = new PrintWriter(System.out)
        try {
            OutputFormat format = cfg?.output ?: OutputFormat.text
            if (options.o) {
                format = OutputFormat.valueOf(options.o)
            }
            ResultFormatter formatter = format.formatter
            logDebug("[Taste] will output $format")

            // let's go

            File scriptFile = new File(fileName)

            logDebug("[Taste] evaluating $scriptFile.absolutePath")

            // configure paths of the Groovy loader
            GroovyClassLoader loader = new GroovyClassLoader()
            String scriptPaths = options.cp ?: System.getProperty('user.dir')
            scriptPaths.split(';').each { path ->
                loader.addClasspath(path)
                logDebug("[Taste] $path added to scripts paths")
            }

            // create shell and eval script
            GroovyShell shell = new CustomShell(loader, fileName)
            def res = shell.evaluate(new InputStreamReader(new FileInputStream(scriptFile)))

            if (res instanceof Test) {
                Test test = (Test) res
                TestResult testResult = test.execute(cfg)
                if (outDir) {
                    String reportName = test.name + '.' + format.name()
                    File report = new File(new File(outDir), reportName)
                    logDebug("Writing test report to $report.absolutePath")
                    report.withWriter { w ->
                        formatter.format(cfg, scriptFile.absolutePath, testResult, w)
                    }
                } else {
                    out << "=Taste-Begin-Output=\n"
                    formatter.format(cfg, scriptFile.absolutePath, testResult, out)
                    out << "=Taste-End-Output=\n"
                }
            } else if (res instanceof Suite) {
                Suite suite = (Suite) res
                SuiteResult suiteResult = suite.execute(cfg)
                if (outDir) {
                    String reportName = suite.name + '.' + format.name()
                    File report = new File(new File(outDir), reportName)
                    logDebug("Writing suite report to $report.absolutePath")
                    report.withWriter { w ->
                        formatter.format(cfg, scriptFile.absolutePath, suiteResult, w)
                    }
                } else {
                    out << "=Taste-Begin-Output=\n"
                    formatter.format(cfg, scriptFile.absolutePath, suiteResult, out)
                    out << "=Taste-End-Output=\n"
                }
            } else {
                throw new IllegalStateException("file $scriptFile.absolutePath returned invalid Test : $res")
            }
        } finally {
            out.flush()
            out.close()
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
