package com.pojosontheweb.taste

import groovy.transform.Immutable

@Immutable
class SuiteResult {

    String name
    Date startedOn
    Date finishedOn
    List<TestResult> testResults

    Map toMap(boolean withResults) {
        Map res = [
            name: name,
            startedOn: startedOn,
            finishedOn: finishedOn,
        ]
        if (withResults) {
            res.testResults = testResults.collect { it.toMap() }
        }
        return res
    }

    boolean isSuccess() {
        !testResults?.find { it instanceof ResultFailure }
    }

}
