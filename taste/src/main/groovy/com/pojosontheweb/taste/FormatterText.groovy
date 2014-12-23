package com.pojosontheweb.taste


class FormatterText implements ResultFormatter {

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
        "$prefix Test '$testResult.testName' : $status\n${toTxt(map)}"
    }

    private static def printConfig(Cfg cfg) {
        if (cfg) {
            StringBuilder sb = new StringBuilder()
            sb << "Config :\n"
            prettifyKeys(cfg.sysProps).each { k, v->
                sb << "- $k : $v\n"
            }
            sb.toString()
        } else {
            "Config : none.\n"
        }
    }


    @Override
    void format(Cfg cfg, String fileName, TestResult testResult, Writer out) {
        out << printTestResult(fileName, testResult)
        out << printConfig(cfg)
    }

    @Override
    void format(Cfg cfg, String fileName, SuiteResult suiteResult, Writer out) {
        Map map = suiteResult.toMap(false)
        map['fileName'] = fileName
        int nbSuccess = 0,
            nbFailed = 0,
            total = suiteResult.testResults.size()

        StringBuilder sb = new StringBuilder()
        suiteResult.testResults.each { TestResult tr ->
            sb << '\n' << printTestResult(fileName, tr) << '\n'
            if (tr instanceof ResultFailure) {
                nbFailed++
            } else if (tr instanceof ResultSuccess) {
                nbSuccess++
            }
        }
        def percent = nbSuccess / total * 100
        out << "> Suite '$suiteResult.name' : $total tests, SUCCESS $nbSuccess, FAILED $nbFailed - $percent %\n"
        out << toTxt(map)
        out << "\n\n"
        out << "Tests :\n"
        out << sb.toString()
        out << "\n"
        out << printConfig(cfg)
    }
}
