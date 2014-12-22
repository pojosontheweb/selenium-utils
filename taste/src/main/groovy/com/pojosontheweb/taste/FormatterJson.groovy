package com.pojosontheweb.taste

import groovy.json.JsonBuilder

class FormatterJson implements ResultFormatter {

    @Override
    void format(Cfg cfg, String fileName, TestResult testResult, Writer out) {
        Map map = testResult.toMap()
        map['fileName'] = fileName
        out << toJson(map)
    }

    @Override
    void format(Cfg cfg, String fileName, SuiteResult suiteResult, Writer out) {
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
        out << toJson(map)
    }

    private static def toJson(Map map) {
        new JsonBuilder(map).toPrettyString()
    }
}
