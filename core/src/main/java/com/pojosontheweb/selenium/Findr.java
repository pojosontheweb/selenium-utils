package com.pojosontheweb.selenium;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility for accessing Selenium DOM safely, wait-style.
 *
 * Allows to create chains of conditions and execute those conditions
 * inside a WebDriverWait, in a transparent fashion.
 *
 * Instances are immutable and can be reused safely.
 */
public final class Findr {

    /** the default wait timeout */
    public static final int WAIT_TIMEOUT_SECONDS = 10; // secs

    /** the sys prop name for enabling logs in findr eval(s) */
    public static final String SYSPROP_VERBOSE = "webtests.findr.verbose";

    /** ref to the driver */
    private final WebDriver driver;

    /** the composed function */
    private final Function<SearchContext,WebElement> f;

    /**
     * A list of strings that represent the "path" for this findr,
     * used to create meaningful failure messages
     */
    private final List<String> path;

    /**
     * The wait timeout (in seconds)
     */
    private final int waitTimeout;

    private final long sleepInMillis;

    public static boolean isDebugEnabled() {
        return Boolean.valueOf(System.getProperty(SYSPROP_VERBOSE, "false"));
    }

    private static Function<String,?> debugHandler = new Function<String, Object>() {
        @Override
        public Object apply(String input) {
            System.out.println(input);
            return null;
        }
    };

    /**
     * Pass a function that gets called-back with the logs. By default, logs
     * messages to stdout.
     * @param h the debug log handler function
     */
    public static void setDebugHandler(Function<String,?> h) {
        debugHandler = h;
    }

    public static void logDebug(String message) {
        if (isDebugEnabled()) {
            debugHandler.apply(message);
        }
    }

    /**
     * Create a Findr with passed arguments
     * @param driver the WebDriver
     */
    public Findr(WebDriver driver) {
        this(driver, WAIT_TIMEOUT_SECONDS);
    }

    /**
     * Create a Findr with passed arguments
     * @param driver the WebDriver
     * @param waitTimeout the wait timeout in seconds
     */
    public Findr(WebDriver driver, int waitTimeout) {
        this(driver, waitTimeout, WebDriverWait.DEFAULT_SLEEP_TIMEOUT, null, Collections.<String>emptyList());
    }

    /**
     * Return the web driver passed at construction time
     * @return the web driver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Return the timeout for this findr in seconds
     * @return the timeout in seconds
     */
    public int getTimeout() {
        return waitTimeout;
    }

    /**
     * Helper for "nested" Findrs. Allows to use a <code>WebElement</code> as the
     * root of a new Findr.
     * @param driver The WebDriver
     * @param webElement the WebElement to use as root
     * @return a new Findr that has the specified WebElement as its root
     */
    public static Findr fromWebElement(WebDriver driver, final WebElement webElement) {
        return fromWebElement(driver, webElement, WAIT_TIMEOUT_SECONDS);
    }

    /**
     * Helper for "nested" Findrs. Allows to use a <code>WebElement</code> as the
     * root of a new Findr.
     * @param driver The WebDriver
     * @param webElement the WebElement to use as root
     * @param waitTimeout the wait timeout in seconds
     * @return a new Findr that has the specified WebElement as its root
     */
    public static Findr fromWebElement(WebDriver driver, final WebElement webElement, int waitTimeout) {
        Findr f = new Findr(driver, waitTimeout);
        return f.compose(new Function<SearchContext, WebElement>() {
            @Override
            public WebElement apply(SearchContext input) {
                return webElement;
            }
        }, "fromWebElement(" + webElement + ")");
    }

    private Findr(WebDriver driver,
                  int waitTimeout,
                  long sleepInMillis,
                  Function<SearchContext, WebElement> f,
                  List<String> path) {
        this.driver = driver;
        this.waitTimeout = waitTimeout;
        this.sleepInMillis = sleepInMillis;
        this.f = f;
        this.path = path;
    }

    private <F,T> Function<F,T> wrapAndTrapCatchSeleniumException(final Function<F, T> function) {
        return new Function<F,T>() {
            @Override
            public T apply(F input) {
                try {
                    return function.apply(input);
                } catch(WebDriverException e) {
                    // retry in case of exception
                    return null;
                }
            }
        };
    }

    private Findr compose(final Function<SearchContext,WebElement> function, final String pathElem) {
        final Function<SearchContext,WebElement> newFunction = wrapAndTrapCatchSeleniumException(function);
        ArrayList<String> newPath = new ArrayList<String>(path);
        if (pathElem!=null) {
            newPath.add(pathElem);
        }
        Function<SearchContext,WebElement> composed;
        if (f==null) {
            composed = new Function<SearchContext, WebElement>() {
                @Override
                public WebElement apply(SearchContext input) {
                    WebElement res = newFunction.apply(input);
                    if (res==null) {
                        logDebug("[Findr]  ! " + pathElem + " (null)");
                    } else {
                        logDebug("[Findr]  > " + pathElem + " : " + res);
                    }
                    return res;
                }
            };
        } else {
            composed = new Function<SearchContext, WebElement>() {
                @Override
                public WebElement apply(SearchContext input) {
                    WebElement res1 = f.apply(input);
                    if (res1==null) {
                        logDebug("[Findr]  - " + pathElem);
                        return null;
                    } else {
                        WebElement res2 = newFunction.apply(res1);
                        if (res2==null) {
                            logDebug("[Findr]  ! " + pathElem);
                        } else {
                            logDebug("[Findr]  > " + pathElem + " : " + res2);
                        }
                        return res2;
                    }
                }
            };
        }
        return new Findr(driver, waitTimeout, sleepInMillis, composed, newPath);

    }

    /**
     * Set the timeout (in seconds) and return an updated Findr
     * @param timeoutInSeconds the timeout in seconds
     * @return an updated Findr instance
     */
    public Findr setTimeout(int timeoutInSeconds) {
        return new Findr(driver, timeoutInSeconds, sleepInMillis, f, path);
    }

    /**
     * Set the WebDriverWait sleep interval (in ms). Use to control polling frequency.
     * @param sleepInMillis the sleep interval in milliseconds
     * @return an updated Findr instance
     */
    public Findr setSleepInMillis(long sleepInMillis) {
        return new Findr(driver, waitTimeout, sleepInMillis, f, path);
    }

    /**
     * Adds specified single-element selector to the chain, and return a new Findr.
     * @param by the selector
     * @return a new Findr with updated condition chain
     */
    public Findr elem(final By by) {
        return compose(
                new Function<SearchContext, WebElement>() {
                    @Override
                    public WebElement apply(SearchContext input) {
                        if (input==null) {
                            return null;
                        }
                        try {
                            return input.findElement(by);
                        } catch(Exception e) {
                            return null;
                        }
                    }
                },
                by.toString()
        );
    }

    /**
     * Adds specified multiple element selector to the chain, and return a new ListFindr.
     * @param by the selector
     * @return a new ListFindr with updated condition chain
     */
    public ListFindr elemList(By by) {
        return new ListFindr(by);
    }

    public ListFindr append(ListFindr lf) {
        return new ListFindr(lf.by, lf.filters, lf.checkers);
    }

    public Findr append(Findr f) {
        List<String> newPath = new ArrayList<String>(path!=null?path:new ArrayList<String>());
        if (f.path!=null) {
            newPath.addAll(f.path);
        }
        return compose(f.f, "append[" + Joiner.on(", ").join(f.path) + "]");
    }

    private <T> T wrapWebDriverWait(final Function<WebDriver,T> callback) throws TimeoutException {
        try {
            return new WebDriverWait(driver, waitTimeout, sleepInMillis).until(callback);
        } catch(TimeoutException e) {
            // failed to find element(s), build exception message
            // and re-throw exception
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> it = path.iterator(); it.hasNext(); ) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append("->");
                }
            }
            throw new TimeoutException("Timed out trying to find path=" + sb.toString() + ", callback=" + callback, e);
        }
    }

    /**
     * Evaluates this Findr, and invokes passed callback if the whole chain succeeds. Throws
     * a TimeoutException otherwise.
     * @param callback the callback to invoke (called if the whole chain of conditions succeeded)
     * @param <T> the return type of the callback
     * @return the result of the callback
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public <T> T eval(final Function<WebElement,T> callback) throws TimeoutException {
        return wrapWebDriverWait(wrapAndTrapCatchSeleniumException(new Function<WebDriver, T>() {
            @Override
            public T apply(WebDriver input) {
                if (f==null) {
                    throw new EmptyFindrException();
                }
                logDebug("[Findr] eval");
                WebElement e = f.apply(input);
                if (e == null) {
                    logDebug("[Findr]  => Chain STOPPED before callback");
                    return null;
                }
                T res = callback.apply(e);
                if (res==null || (res instanceof Boolean && !((Boolean)res))) {
                    logDebug("[Findr]  => " + callback + " result : " + res + ", will try again");
                } else {
                    logDebug("[Findr]  => " + callback + " result : " + res + ", OK");
                }
                return res;
            }
        }));
    }

    public static final Function<WebElement,?> IDENTITY_FOR_EVAL = new Function<WebElement, Object>() {
        @Override
        public Object apply(WebElement webElement) {
            return true;
        }
    };

    /**
     * Evaluates this Findr, and blocks until all conditions are satisfied. Throws
     * a TimeoutException otherwise.
     */
    public void eval() throws TimeoutException {
        eval(IDENTITY_FOR_EVAL);
    }

    /**
     * Evaluates this Findr, and blocks until all conditions are satisfied. Throws
     * a TimeoutException otherwise.
     * @param failureMessage A message to be included to the timeout exception
     */
    public void eval(String failureMessage) throws TimeoutException {
        try {
            eval();
        } catch(TimeoutException e) {
            throw new TimeoutException(failureMessage, e);
        }
    }

    /**
     * Evaluates this Findr, and invokes passed callback if the whole chain succeeds. Throws
     * a TimeoutException with passed failure message otherwise.
     * @param callback the callback to invoke (called if the whole chain of conditions succeeded)
     * @param <T> the return type of the callback
     * @param failureMessage A message to be included to the timeout exception
     * @return the result of the callback
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public <T> T eval(final Function<WebElement,T> callback, String failureMessage) throws TimeoutException {
        try {
            return eval(callback);
        } catch (TimeoutException e) {
            throw new TimeoutException(failureMessage, e);
        }
    }

    /**
     * Adds a Predicate (condition) to the chain, and return a new Findr
     * with updated chain.
     * @param predicate the condition to add
     * @return a Findr with updated conditions chain
     */
    public Findr where(final Predicate<? super WebElement> predicate) {
        return compose(new Function<SearchContext, WebElement>() {
            @Override
            public WebElement apply(SearchContext input) {
                if (input==null) {
                    return null;
                }
                if (input instanceof WebElement) {
                    WebElement webElement = (WebElement)input;
                    if (predicate.apply(webElement)) {
                        return webElement;
                    }
                    return null;
                } else {
                    throw new RuntimeException("input is not a WebElement : " + input);
                }
            }
        },
                predicate.toString()
        );
    }

    /**
     * Shortcut method : evaluates chain, and sends keys to target WebElement of this
     * Findr. If sendKeys throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     * @param keys the text to send
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void sendKeys(final CharSequence... keys) throws TimeoutException {
        eval(Findrs.sendKeys(keys));
    }

    /**
     * Shortcut method : evaluates chain, and clicks target WebElement of this
     * Findr. If the click throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void click() {
        eval(Findrs.click());
    }

    /**
     * Shortcut method : evaluates chain, and clears target WebElement of this
     * Findr. If clear throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void clear() {
        eval(Findrs.clear());
    }

    private static final Function<List<WebElement>,Object> IDENTITY_LIST = new Function<List<WebElement>, Object>() {
        @Override
        public Object apply(List<WebElement> webElements) {
            return webElements;
        }
    };

    /**
     * Findr counterpart for element lists. Instances of this class are created and
     * returned by <code>Findr.elemList()</code>. Allows for index-based and filtering.
     */
    public class ListFindr {

        private final By by;
        private final Predicate<WebElement> filters;
        private final Predicate<List<WebElement>> checkers;

        private ListFindr(By by) {
            this(by, null, null);
        }

        private ListFindr(By by, Predicate<WebElement> filters, Predicate<List<WebElement>> checkers) {
            this.by = by;
            this.filters = filters;
            this.checkers = checkers;
        }

        private <T> Predicate<T> wrapAndTrap(final Predicate<T> predicate) {
            return new Predicate<T>() {
                @Override
                public boolean apply(T input) {
                    if (input==null) {
                        return false;
                    }
                    try {
                        return predicate.apply(input);
                    } catch(WebDriverException e) {
                        return false;
                    }

                }
            };
        }

        private <T> T wrapWebDriverWaitList(final Function<WebDriver,T> callback) throws TimeoutException {
            try {
                return new WebDriverWait(driver, waitTimeout, sleepInMillis).until(callback);
            } catch(TimeoutException e) {
                // failed to find element(s), build exception message
                // and re-throw exception
                ArrayList<String> newPath = new ArrayList<String>(path);
                newPath.add(by.toString());
                StringBuilder sb = new StringBuilder();
                for (Iterator<String> it = newPath.iterator(); it.hasNext(); ) {
                    sb.append(it.next());
                    if (it.hasNext()) {
                        sb.append("->");
                    }
                }
                throw new TimeoutException("Timed out trying to find path=" + sb.toString() + ", callback=" + callback, e);
            }
        }

        /**
         * Adds a filtering predicate, and returns a new ListFindr with updated chain.
         * @param predicate the predicate used for filtering the list of elements (applied on each element)
         * @return a new ListFindr with updated chain
         * @throws java.lang.IllegalArgumentException if called after <code>whereElemCount</code>.
         */
        public ListFindr where(final Predicate<? super WebElement> predicate) {
            if (checkers != null) {
                throw new IllegalArgumentException("It's forbidden to call ListFindr.where() after a whereXXX() method has been called.");
            }
            return new ListFindr(by, composeFilters(predicate), checkers);
        }

        private Predicate<WebElement> composeFilters(final Predicate<? super WebElement> predicate) {
            return new Predicate<WebElement>() {
                @Override
                public boolean apply(WebElement input) {
                    return (filters == null || filters.apply(input)) && wrapAndTrap(predicate).apply(input);
                }

                @Override
                public String toString() {
                    if (filters!=null) {
                        return filters.toString() + " + " + predicate.toString();
                    } else {
                        return predicate.toString();
                    }
                }
            };
        }

        private Predicate<List<WebElement>> composeCheckers(final Predicate<List<WebElement>> predicate) {
            return new Predicate<List<WebElement>>() {
                @Override
                public boolean apply(List<WebElement> input) {
                    return (checkers == null || checkers.apply(input)) && wrapAndTrap(predicate).apply(input);
                }

                @Override
                public String toString() {
                    if (filters!=null) {
                        return filters.toString() + " + " + predicate.toString();
                    } else {
                        return predicate.toString();
                    }
                }
            };
        }

        /**
         * Index-based access to the list of elements in this ListFindr. Allows
         * to wait for the n-th elem.
         * @param index the index of the element to wait for
         * @return a new Findr with updated chain
         */
        public Findr at(final int index) {
            return compose(new Function<SearchContext, WebElement>(){
                @Override
                public WebElement apply(SearchContext input) {
                    List<WebElement> elements;
                    List<WebElement> filtered;
                    try {
                        elements = input.findElements(by);
                        filtered = filterElements(elements);
                    } catch(Exception e) {
                        return null;
                    }
                    if (elements==null) {
                        return null;
                    }
                    if (checkers != null && !checkers.apply(filtered)) {
                        logDebug("[Findr]  ! checkList KO: " + checkers);
                        logDebug("[Findr]  => Chain STOPPED before callback");
                        return null;
                    } else {
                        if (isDebugEnabled() && checkers!=null) {
                            logDebug("[Findr]  > checkList OK: " + checkers);
                        }
                    }
                    if (index>=filtered.size()) {
                        return null;
                    }
                    return filtered.get(index);
                }
            },
                    by.toString() + "[" + index + "]"
            );
        }

        private List<WebElement> filterElements(List<WebElement> source) {
            List<WebElement> filtered = new ArrayList<WebElement>();
            for (WebElement element : source) {
                if (filters==null || filters.apply(element)) {
                    filtered.add(element);
                }
            }
            if (isDebugEnabled() && filters!=null) {
                int srcSize = source.size();
                int filteredSize = filtered.size();
                logDebug("[Findr]  > [" + by + "]* filter(" + filters + ") : " + srcSize + " -> " + filteredSize);
            }
            return filtered;
        }

        /**
         * Wait for the list findr to mach passed count
         * @param elemCount the expected count
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereElemCount(int elemCount) {
            return new ListFindr(by, filters, composeCheckers(checkElemCount(elemCount)));
        }

        /**
         * Wait for the list findr so that at least one of its element match the specified predicate. Check is OK if list is empty.
         * @param predicate the element predicate to match
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereAny(final Predicate<? super WebElement> predicate) {
            return new ListFindr(by, filters, composeCheckers(checkAny(predicate)));
        }

        /**
         * Wait for the list findr so that all of its element match the specified predicate. Check is OK if list is empty.
         * @param predicate the element predicate to match
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereAll(final Predicate<? super WebElement> predicate) {
            return new ListFindr(by, filters, composeCheckers(checkAll(predicate)));
        }

        private Predicate<List<WebElement>> checkAny(final Predicate<? super WebElement> predicate) {
            return new Predicate<List<WebElement>>() {
                @Override
                public boolean apply(List<WebElement> elements) {
                    if (elements.size() == 0) {
                        return true;
                    }
                    for (WebElement element : elements) {
                        if (predicate.apply(element)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return "any(" + predicate + ")";
                }
            };
        }

        private Predicate<List<WebElement>> checkAll(final Predicate<? super WebElement> predicate) {
            return new Predicate<List<WebElement>>() {
                @Override
                public boolean apply(List<WebElement> elements) {
                    for (WebElement element : elements) {
                        if (!predicate.apply(element)) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public String toString() {
                    return "all(" + predicate + ")";
                }
            };
        }

        private Predicate<List<WebElement>> checkElemCount(final int expectedCount) {
            return new Predicate<List<WebElement>>() {
                @Override
                public boolean apply(List<WebElement> elements) {
                    return elements != null && elements.size() == expectedCount;
                }

                @Override
                public String toString() {
                    return "elemCount(" + expectedCount + ")";
                }
            };
        }

        /**
         * Evaluates this ListFindr and invokes passed callback if the whole chain suceeded. Throws
         * a TimeoutException if the condition chain didn't match.
         * @param callback the callback to call if the chain succeeds
         * @param <T> the rturn type of the callback
         * @return the result of the callback
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public <T> T eval(final Function<List<WebElement>, T> callback) throws TimeoutException {
            logDebug("[Findr] ListFindr eval");
            return wrapWebDriverWaitList(wrapAndTrapCatchSeleniumException(new Function<WebDriver, T>() {
                @Override
                public T apply(WebDriver input) {
                    SearchContext c = f == null ? input : f.apply(input);
                    if (c == null) {
                        return null;
                    }
                    List<WebElement> elements = c.findElements(by);
                    if (elements == null) {
                        return null;
                    }
                    List<WebElement> filtered = filterElements(elements);
                    if (checkers != null && !checkers.apply(filtered)) {
                        logDebug("[Findr]  ! checkList KO: " + checkers);
                        logDebug("[Findr]  => Chain STOPPED before callback");
                        return null;
                    } else {
                        if (isDebugEnabled() && checkers!=null) {
                            logDebug("[Findr]  > checkList OK: " + checkers);
                        }
                    }
                    T res = callback.apply(filtered);
                    if (res==null || (res instanceof Boolean && !((Boolean)res))) {
                        logDebug("[Findr]  => " + callback + " result : " + res + ", will try again");
                    } else {
                        logDebug("[Findr]  => " + callback + " result : " + res + ", OK");
                    }
                    return res;
                }
            }));
        }

        /**
         * Evaluates this ListFindr. Throws
         * a TimeoutException if the condition chain didn't match.
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public void eval() throws TimeoutException {
            eval(IDENTITY_LIST);
        }

        /**
         * Evaluates this ListFindr. Throws
         * a TimeoutException if the condition chain didn't match.
         * @param failureMessage A message to include in the timeout exception
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public void eval(String failureMessage) throws TimeoutException {
            try {
                eval(IDENTITY_LIST);
            } catch(TimeoutException e) {
                throw new TimeoutException(failureMessage, e);
            }
        }

        /**
         * Evaluates this ListFindr and invokes passed callback if the whole chain suceeded. Throws
         * a TimeoutException with passed failure message if the condition chain didn't match.
         * @param callback the callback to call if the chain succeeds
         * @param <T> the rturn type of the callback
         * @return the result of the callback
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public <T> T eval(Function<List<WebElement>, T> callback, String failureMessage) throws TimeoutException {
            try {
                return eval(callback);
            } catch(TimeoutException e) {
                throw new TimeoutException(failureMessage, e);
            }

        }

        @Override
        public String toString() {
            return "ListFindr{" +
                    "by=" + by +
                    ", filters=" + filters +
                    ", checkers=" + checkers +
                    ", findr=" + Findr.this +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Findr{" +
                "driver=" + driver +
                ", path=" + path +
                ", waitTimeout=" + waitTimeout +
                '}';
    }

    // Utility statics
    // ---------------

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> attrEquals(final String attrName, final String expectedValue) {
        return Findrs.attrEquals(attrName, expectedValue);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> attrStartsWith(final String attrName, final String expectedStartsWith) {
        return Findrs.attrStartsWith(attrName, expectedStartsWith);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> attrEndsWith(final String attrName, final String expectedEndsWith) {
        return Findrs.attrEndsWith(attrName, expectedEndsWith);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> hasClass(final String className) {
        return Findrs.hasClass(className);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> textEquals(final String expected) {
        return Findrs.textEquals(expected);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> textStartsWith(final String expectedStartsWith) {
        return Findrs.textStartsWith(expectedStartsWith);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> textEndsWith(final String expectedEndsWith) {
        return Findrs.textEndsWith(expectedEndsWith);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> isEnabled() {
        return Findrs.isEnabled();
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> isDisplayed() {
        return Findrs.isDisplayed();
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> cssValue(final String propName, final String expectedValue) {
        return Findrs.cssValue(propName, expectedValue);
    }

    /**
     * @deprecated use Findrs.* instead
     */
    @Deprecated
    public static Predicate<WebElement> not(final Predicate<WebElement> in) {
        return Findrs.not(in);
    }

    public static final class EmptyFindrException extends IllegalStateException {
        public EmptyFindrException() {
            super("Calling eval() on an empty Findr ! You need to " +
                  "specify at least one condition before evaluating.");
        }
    }

}
