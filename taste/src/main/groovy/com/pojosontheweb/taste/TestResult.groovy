package com.pojosontheweb.taste

abstract class TestResult {

    private final String testName
    private final Date startedOn
    private final Date finishedOn

    TestResult(String testName, Date startedOn, Date finishedOn) {
        this.testName = testName
        this.startedOn = startedOn
        this.finishedOn = finishedOn
    }

    String getTestName() {
        return testName
    }

    Date getStartedOn() {
        return startedOn
    }

    Date getFinishedOn() {
        return finishedOn
    }

    abstract Map toMap()

}

class ResultSuccess extends TestResult {

    private final def retVal

    ResultSuccess(String testName, Date startedOn, Date finishedOn, retVal) {
        super(testName, startedOn, finishedOn)
        this.retVal = retVal
    }

    def getRetVal() {
        return retVal
    }


    @Override
    public String toString() {
        return "ResultSuccess{" +
            "testName='" + testName + '\'' +
            ", startedOn=" + startedOn +
            ", finishedOn=" + finishedOn +
            ", retVal=" + retVal +
            '}';
    }

    @Override
    Map toMap() {
        [
            success     : true,
            testName    : testName,
            startedOn   : startedOn,
            finishedOn  : finishedOn,
            retVal      : retVal
        ]
    }
}

class ResultFailure extends TestResult {

    private final Throwable err

    ResultFailure(String testName, Date startedOn, Date finishedOn, Throwable err) {
        super(testName, startedOn, finishedOn)
        this.err = err
    }

    Throwable getErr() {
        return err
    }

    String getStackTrace() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        bos.withWriter { Writer w ->
            err.printStackTrace(new PrintWriter(w))
        }
        bos.toString()
    }

    @Override
    public String toString() {
        return "ResultFailure{" +
            "testName='" + testName + '\'' +
            ", startedOn=" + startedOn +
            ", finishedOn=" + finishedOn +
            ", err=" + err +
            '}';
    }

    @Override
    Map toMap() {
        [
            success     : false,
            testName    : testName,
            startedOn   : startedOn,
            finishedOn  : finishedOn,
            err         : err.message,
            stack       : stackTrace
        ]
    }

}
