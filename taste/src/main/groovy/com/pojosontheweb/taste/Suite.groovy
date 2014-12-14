package com.pojosontheweb.taste

import com.pojosontheweb.selenium.Findr

class Suite {

    String name
    Closure body

    List<Test> tests

    SuiteResult execute(Cfg cfg) {
        Findr.logDebug("[Suite][$name] Loading tests")
        Date startedOn = new Date()
        def code = body.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        tests = []
        code()
        Findr.logDebug("[Suite][$name] Will execute ${tests.size()} test(s)")
        List<TestResult> results = tests.collect { Test t ->
            t.execute(cfg)
        }
        Findr.logDebug("[Suite][$name] Done, returning results")
        new SuiteResult(name, startedOn, new Date(), results)
    }

    void add(Test test) {
        Findr.logDebug("[Suite][$name] << $test.name")
        tests << test
    }



}
