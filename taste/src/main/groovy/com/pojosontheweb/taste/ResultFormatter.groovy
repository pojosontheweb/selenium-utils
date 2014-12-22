package com.pojosontheweb.taste

interface ResultFormatter {

    void format(Cfg cfg, String fileName, TestResult testResult, Writer out)

    void format(Cfg cfg, String fileName, SuiteResult suiteResult, Writer out)

}