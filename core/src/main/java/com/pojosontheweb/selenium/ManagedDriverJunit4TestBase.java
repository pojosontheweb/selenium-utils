package com.pojosontheweb.selenium;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

/**
 * Base JUnit4 test class.
 */
public class ManagedDriverJunit4TestBase {

    public static final String PROP_WEBTESTS_FINDR_TIMEOUT = "webtests.findr.timeout";

    private final TestUtil testUtil = new TestUtil();

    private static String toTestName(Description d) {
        return d.getClassName() + "." + d.getMethodName();
    }

    @Rule
    public TestRule watchman = new TestWatcher() {

        @Override
        public Statement apply(Statement base, Description description) {
            return super.apply(base, description);
        }

        @Override
        protected void succeeded(Description description) {
            if (testUtil.isFailuresOnly()) {
                testUtil.removeVideoFiles();
            } else {
                testUtil.moveVideoFiles(toTestName(description));
            }
        }

        @Override
        protected void failed(Throwable e, Description description) {
            // keep video for failed tests if video is on
            testUtil.moveVideoFiles(toTestName(description));
        }

        @Override
        protected void starting(Description description) {
            testUtil.setUp();
        }

        @Override
        protected void finished(Description description) {
            testUtil.tearDown();
        }
    };

    protected final WebDriver getWebDriver() {
        return testUtil.getWebDriver();
    }

    protected Findr findr() {
        String timeoutStr = System.getProperty(PROP_WEBTESTS_FINDR_TIMEOUT);
        int i = timeoutStr==null ? Findr.WAIT_TIMEOUT_SECONDS : Integer.parseInt(timeoutStr);
        return new Findr(getWebDriver(), i);
    }

    protected Findr findr(int timeoutInSeconds) {
        return new Findr(getWebDriver(), timeoutInSeconds);
    }

}
