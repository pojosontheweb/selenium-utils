package com.pojosontheweb.taste

class Suite {

    String name
    Closure body

    List<Test> tests

    SuiteResult execute() {
        Date startedOn = new Date()
        def code = body.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        tests = []
        code()
        List<TestResult> results = tests.collect { Test t ->
            t.execute()
        }
        new SuiteResult(name, startedOn, new Date(), results)
    }

    void add(Test test) {
        tests << test
    }



}
