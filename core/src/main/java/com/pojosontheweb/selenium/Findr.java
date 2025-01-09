package com.pojosontheweb.selenium;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility for accessing Selenium DOM safely, wait-style.
 * Allows to create chains of conditions and execute those conditions
 * inside a WebDriverWait, in a transparent fashion.
 * Instances are immutable and can be reused safely.
 */
public final class Findr {

    /**
     * the default wait timeout
     */
    public static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * the sys prop name for enabling logs in findr eval(s)
     */
    public static final String SYSPROP_VERBOSE = "webtests.findr.verbose";

    private static final FindrActions DEFAULT_ACTIONS = new FindrActions();

    /**
     * ref to the driver
     */
    private final WebDriver driver;

    /**
     * the composed function
     */
    private final Function<SearchContext, SearchContext> f;

    /**
     * A list of strings that represent the "path" for this findr,
     * used to create meaningful failure messages
     */
    private final List<String> path;

    private final Duration waitTimeout;

    /**
     * The sleep interval (between polls)
     */
    private final Duration sleep;

    private final FindrActions findrActions;

    public static boolean isDebugEnabled() {
        return Boolean.getBoolean(SYSPROP_VERBOSE);
    }

    private static Function<String, ?> debugHandler = input -> {
        System.out.println(input);
        return null;
    };

    /**
     * Pass a function that gets called-back with the logs. By default, logs
     * messages to stdout.
     *
     * @param h the debug log handler function
     */
    public static void setDebugHandler(Function<String, ?> h) {
        debugHandler = h;
    }

    static Function<String, ?> getDebugHandler() {
        return debugHandler;
    }

    public static void logDebug(String message) {
        if (isDebugEnabled()) {
            debugHandler.apply(message);
        }
    }

    /**
     * Create a Findr with passed arguments
     *
     * @param driver the WebDriver
     */
    public Findr(WebDriver driver) {
        this(driver, WAIT_TIMEOUT);
    }

    /**
     * Create a Findr with passed arguments
     *
     * @param driver      the WebDriver
     * @param waitTimeout the wait timeout
     */
    public Findr(WebDriver driver, Duration waitTimeout) {
        this(
                driver,
                waitTimeout,
                Duration.ofMillis(500),
                null,
                Collections.emptyList(),
                DEFAULT_ACTIONS
        );
    }

    /**
     * Create a Findr with passed arguments
     *
     * @param driver               the WebDriver
     * @param waitTimeoutInSeconds the wait timeout in seconds
     * @deprecated use {@link #Findr(WebDriver, Duration)}
     */
    @Deprecated
    public Findr(WebDriver driver, int waitTimeoutInSeconds) {
        this(
                driver,
                Duration.ofSeconds(waitTimeoutInSeconds),
                Duration.ofMillis(500),
                null,
                Collections.emptyList(),
                DEFAULT_ACTIONS
        );
    }

    /**
     * Return the web driver passed at construction time
     *
     * @return the web driver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Return the timeout for this findr in seconds
     *
     * @return the timeout in seconds
     */
    public Duration getTimeout() {
        return waitTimeout;
    }

    /**
     * Helper for "nested" Findrs. Allows to use a <code>WebElement</code> as the
     * root of a new Findr.
     *
     * @param driver     The WebDriver
     * @param webElement the WebElement to use as root
     * @return a new Findr that has the specified WebElement as its root
     */
    public static Findr fromWebElement(WebDriver driver, final WebElement webElement) {
        return fromWebElement(driver, webElement, WAIT_TIMEOUT);
    }

    /**
     * Helper for "nested" Findrs. Allows to use a <code>WebElement</code> as the
     * root of a new Findr.
     *
     * @param driver      The WebDriver
     * @param webElement  the WebElement to use as root
     * @param waitTimeout the wait timeout in seconds
     * @return a new Findr that has the specified WebElement as its root
     */
    public static Findr fromWebElement(WebDriver driver, final WebElement webElement, Duration waitTimeout) {
        Findr f = new Findr(driver, waitTimeout);
        return f.compose(input -> webElement, "fromWebElement(" + webElement + ")", null);
    }

    /**
     * Helper for "nested" Findrs. Allows to use a <code>WebElement</code> as the
     * root of a new Findr.
     *
     * @param driver        The WebDriver
     * @param webElement    the WebElement to use as root
     * @param waitTimeoutMs the wait timeout in seconds
     * @return a new Findr that has the specified WebElement as its root
     * @deprecated use {@link #fromWebElement(WebDriver, WebElement, Duration)}
     */
    public static Findr fromWebElement(WebDriver driver, final WebElement webElement, int waitTimeoutMs) {
        Findr f = new Findr(driver, Duration.ofMillis(waitTimeoutMs));
        return f.compose(input -> webElement, "fromWebElement(" + webElement + ")", null);
    }

    private Findr(WebDriver driver,
                  Duration waitTimeout,
                  Duration sleep,
                  Function<SearchContext, SearchContext> f,
                  List<String> path,
                  FindrActions actions) {
        this.driver = driver;
        this.waitTimeout = waitTimeout;
        this.sleep = sleep;
        this.f = f;
        this.path = path;
        this.findrActions = actions;
    }

    private <F, T> Function<F, T> withoutWebDriverException(final Function<F, T> function) {
        return input -> {
            try {
                return function.apply(input);
            } catch (WebDriverException e) {
                // retry in case of exception
                return null;
            }
        };
    }

    private Findr compose(final Function<SearchContext, SearchContext> function, final String pathElem, Function<SearchContext, String> describeFailure) {
        final Function<SearchContext, SearchContext> newFunction = withoutWebDriverException(function);
        ArrayList<String> newPath = new ArrayList<String>(path);
        if (pathElem != null) {
            newPath.add(pathElem);
        }
        Function<SearchContext, SearchContext> composed;
        if (f == null) {
            composed = input -> {
                SearchContext res = newFunction.apply(input);
                if (res == null) {
                    logDebug("[Findr]  ! " + pathElem + " (null)");
                } else {
                    logDebug("[Findr]  > " + pathElem + " : " + res);
                }
                return res;
            };
        } else {
            composed = input -> {
                SearchContext res1 = f.apply(input);
                if (res1 == null) {
                    logDebug("[Findr]  - " + pathElem);
                    return null;
                } else {
                    SearchContext res2 = newFunction.apply(res1);
                    if (res2 == null && isDebugEnabled()) {
                        if (describeFailure != null) {
                            var failure = describeFailure.apply(res1);
                            logDebug("[Findr]  ! " + pathElem + ", " + failure);
                        } else {
                            logDebug("[Findr]  ! " + pathElem);
                        }
                    } else {
                        logDebug("[Findr]  > " + pathElem + " : " + res2);
                    }
                    return res2;
                }
            };
        }
        return new Findr(driver, waitTimeout, sleep, composed, newPath, findrActions);

    }

    /**
     * Set the timeout (in seconds) and return an updated Findr
     *
     * @param timeout the timeout Duration
     * @return an updated Findr instance
     */
    public Findr setTimeout(Duration timeout) {
        return new Findr(driver, timeout, sleep, f, path, findrActions);
    }


    /**
     * Set the timeout (in seconds) and return an updated Findr
     *
     * @param seconds the timeout in seconds
     * @return an updated Findr instance
     * @deprecated use {@link #setTimeout(Duration)}
     */
    @Deprecated
    public Findr setTimeout(int seconds) {
        return this.setTimeout(Duration.ofSeconds(seconds));
    }

    /**
     * Set the WebDriverWait sleep interval. Use to control polling frequency.
     *
     * @param sleep the sleep interval
     * @return an updated Findr instance
     */
    public Findr setSleep(Duration sleep) {
        return new Findr(driver, waitTimeout, sleep, f, path, findrActions);
    }

    public Findr setSleep(long ms) {
        return this.setSleep(Duration.ofMillis(ms));
    }

    /**
     * Set the WebDriverWait sleep interval (in ms). Use to control polling frequency.
     *
     * @param ms the sleep interval in milliseconds
     * @return an updated Findr instance
     * @deprecated use {@link #setSleep(long)}
     */
    @Deprecated
    public Findr setSleepInMillis(long ms) {
        return this.setSleep(Duration.ofMillis(ms));
    }

    public Findr setActions(FindrActions actions) {
        return new Findr(driver, waitTimeout, sleep, f, path, actions);
    }

    /**
     * Empty the condition chain. Use to create new Findrs with same settings but
     * a different condition chain.
     *
     * @return a new empty Findr with other props untouched
     */
    public Findr empty() {
        return new Findr(driver, waitTimeout, sleep, null, path, findrActions);
    }

    /**
     * Adds specified single-element selector to the chain, and return a new Findr.
     *
     * @param by the selector
     * @return a new Findr with updated condition chain
     */
    public Findr elem(final By by) {
        return compose(input -> {
                    if (input == null) {
                        return null;
                    }
                    try {
                        return input.findElement(by);
                    } catch (Exception e) {
                        return null;
                    }
                },
                by.toString(),
                null
        );
    }

    /**
     * Shortcut for <code>elem(By.cssSelector(...))</code>.
     *
     * @param selector the css selector
     * @return a new Findr with updated chain
     */
    public Findr $(String selector) {
        return elem(By.cssSelector(selector));
    }

    /**
     * Adds specified multiple element selector to the chain, and return a new ListFindr.
     *
     * @param by the selector
     * @return a new ListFindr with updated condition chain
     */
    public ListFindr elemList(By by) {
        return new ListFindr(by);
    }

    /**
     * Shortcut for <code>elemList(By.cssSelector(selector))</code>
     *
     * @param selector the css selector
     * @return a new ListFindr with updated condition chain
     */
    public ListFindr $$(String selector) {
        return elemList(By.cssSelector(selector));
    }

    public ListFindr append(ListFindr lf) {
        return new ListFindr(lf.by, lf.filters, lf.checkers);
    }

    public Findr append(Findr f) {
        String sp;
        if (f.path != null) {
            sp = String.join(", ", f.path);
        } else {
            sp = "";
        }
        return compose(f.f, "append[" + sp + "]", null);
    }

    private static String createPathMsg(List<String> path) {
        return String.join("->", path);
    }

    private <T> T wrapWebDriverWait(final Function<SearchContext, T> callback) throws TimeoutException {
        try {
            return new WebDriverWait(driver, waitTimeout, sleep).until(callback);
        } catch (TimeoutException e) {
            // failed to find element(s), build exception message
            // and re-throw exception
            throw new TimeoutException("Timed out trying to find path=" + createPathMsg(path) + ", callback=" + callback, e);
        }
    }

    /**
     * Evaluates this Findr, and invokes passed callback if the whole chain succeeds. Throws
     * a TimeoutException otherwise.
     *
     * @param callback the callback to invoke (called if the whole chain of conditions succeeded)
     * @param <T>      the return type of the callback
     * @return the result of the callback
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public <T> T eval(final Function<WebElement, T> callback) throws TimeoutException {
        return wrapWebDriverWait(withoutWebDriverException(eval_(callback)));
    }

    // for testing
    <T> Function<SearchContext, T> eval_(final Function<WebElement, T> callback) throws TimeoutException {
        return input -> {
            if (f == null) {
                throw new EmptyFindrException();
            }
            logDebug("[Findr] eval");
            SearchContext e = f.apply(input);
            if (e == null) {
                logDebug("[Findr]  => Chain STOPPED before callback");
                return null;
            }
            T res = callback.apply((WebElement) e);
            if (res == null || (res instanceof Boolean && !((Boolean) res))) {
                logDebug("[Findr]  => " + callback + " result : " + res + ", will try again");
            } else {
                logDebug("[Findr]  => " + callback + " result : " + res + ", OK");
            }
            return res;
        };
    }

    public static final Function<WebElement, ?> IDENTITY_FOR_EVAL = (e) -> true;

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
     *
     * @param failureMessage A message to be included to the timeout exception
     */
    public void eval(String failureMessage) throws TimeoutException {
        try {
            eval();
        } catch (TimeoutException e) {
            throw new TimeoutException(failureMessage, e);
        }
    }

    /**
     * Evaluates this Findr, and invokes passed callback if the whole chain succeeds. Throws
     * a TimeoutException with passed failure message otherwise.
     *
     * @param callback       the callback to invoke (called if the whole chain of conditions succeeded)
     * @param <T>            the return type of the callback
     * @param failureMessage A message to be included to the timeout exception
     * @return the result of the callback
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public <T> T eval(final Function<WebElement, T> callback, String failureMessage) throws TimeoutException {
        try {
            return eval(callback);
        } catch (TimeoutException e) {
            throw new TimeoutException(failureMessage, e);
        }
    }

    /**
     * Adds a Predicate (condition) to the chain, and return a new Findr
     * with updated chain.
     *
     * @param predicate the condition to add
     * @return a Findr with updated conditions chain
     */
    public Findr where(final Predicate<? super WebElement> predicate) {
        if (predicate instanceof Findrs.MatcherPredicate<? super WebElement> mp) {
            return where(mp.matcher());
        }
        return compose(input -> {
                    if (input == null) {
                        return null;
                    }
                    if (input instanceof WebElement) {
                        if (predicate.test((WebElement) input)) {
                            return input;
                        }
                        return null;
                    } else {
                        throw new RuntimeException("input is not a WebElement : " + input);
                    }
                },
                predicate.toString(),
                null
        );
    }

    /**
     * Adds a matcher (condition) to the chain, and return a new Findr
     * with updated chain.
     *
     * @param matcher the condition to add
     * @return a Findr with updated conditions chain
     */
    public Findr where(final Matcher<? super WebElement> matcher) {
        return compose(input -> {
                    if (input == null) {
                        return null;
                    }
                    if (input instanceof WebElement) {
                        if (matcher.matches(input)) {
                            return input;
                        }
                        return null;
                    } else {
                        throw new RuntimeException("input is not a WebElement : " + input);
                    }
                },
                matcher.toString(),
                input -> {
                    var d = new StringDescription();
                    matcher.describeMismatch(input, d);
                    return d.toString();
                }
        );
    }

    /**
     * Shortcut method : evaluates chain, and sends keys to target WebElement of this
     * Findr. If sendKeys throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     *
     * @param keys the text to send
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void sendKeys(CharSequence... keys) throws TimeoutException {
        eval(findrActions.sendKeys(keys));
    }

    /**
     * Shortcut method : evaluates chain, and clicks target WebElement of this
     * Findr. If the click throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     *
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void click() {
        eval(findrActions.click());
    }

    /**
     * Shortcut method : evaluates chain, and clears target WebElement of this
     * Findr. If clear throws an exception, then the whole chain is evaluated again, until
     * no exception is thrown, or timeout.
     *
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void clear() {
        eval(findrActions.clear());
    }

    private static final Function<List<WebElement>, Object> IDENTITY_LIST = webElement -> webElement;

    public Findr shadowRoot() {
        return this.compose(sc -> {
            if (sc instanceof WebElement) {
                return ((WebElement) sc).getShadowRoot();
            }
            return null;
        }, "shadow-root", null);
    }

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
            return (T input) -> {
                if (input == null) {
                    return false;
                }
                try {
                    return predicate.test(input);
                } catch (WebDriverException e) {
                    return false;
                }
            };
        }

        private <T> T wrapWebDriverWaitList(final Function<WebDriver, T> callback) throws TimeoutException {
            try {
                return new WebDriverWait(driver, waitTimeout, sleep).until(callback);
            } catch (TimeoutException e) {
                // failed to find element(s), build exception message
                // and re-throw exception
                ArrayList<String> newPath = new ArrayList<String>(path);
                newPath.add(by.toString());
                throw new TimeoutException("Timed out trying to find path=" + createPathMsg(newPath) + ", callback=" + callback, e);
            }
        }

        /**
         * Adds a filtering predicate, and returns a new ListFindr with updated chain.
         *
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
                public boolean test(WebElement input) {
                    return (filters == null || filters.test(input)) && wrapAndTrap(predicate).test(input);
                }

                @Override
                public String toString() {
                    if (filters != null) {
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
                public boolean test(List<WebElement> input) {
                    return (checkers == null || checkers.test(input)) && wrapAndTrap(predicate).test(input);
                }

                @Override
                public String toString() {
                    if (filters != null) {
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
         *
         * @param index the index of the element to wait for
         * @return a new Findr with updated chain
         */
        public Findr at(final int index) {
            return compose(input -> {
                        List<WebElement> elements;
                        List<WebElement> filtered;
                        try {
                            elements = input.findElements(by);
                            filtered = filterElements(elements);
                        } catch (Exception e) {
                            return null;
                        }
                        if (checkers != null && !checkers.test(filtered)) {
                            logDebug("[Findr]  ! checkList KO: " + checkers);
                            logDebug("[Findr]  => Chain STOPPED before callback");
                            return null;
                        } else {
                            if (isDebugEnabled() && checkers != null) {
                                logDebug("[Findr]  > checkList OK: " + checkers);
                            }
                        }
                        if (index >= filtered.size()) {
                            return null;
                        }
                        return filtered.get(index);
                    },
                    by.toString() + "[" + index + "]",
                    null
            );
        }

        private List<WebElement> filterElements(List<WebElement> source) {
            List<WebElement> filtered = new ArrayList<WebElement>();
            for (WebElement element : source) {
                if (filters == null || filters.test(element)) {
                    filtered.add(element);
                }
            }
            if (isDebugEnabled() && filters != null) {
                int srcSize = source.size();
                int filteredSize = filtered.size();
                logDebug("[Findr]  > [" + by + "]* filter(" + filters + ") : " + srcSize + " -> " + filteredSize);
            }
            return filtered;
        }

        /**
         * Alias for <code>count</code>
         */
        public ListFindr whereElemCount(int elemCount) {
            return count(elemCount);
        }

        /**
         * Wait for the list findr to mach passed count
         *
         * @param elemCount the expected count
         * @return a new ListFindr with updated chain
         */
        public ListFindr count(int elemCount) {
            return new ListFindr(by, filters, composeCheckers(checkElemCount(elemCount)));
        }

        /**
         * Wait for the list findr so that at least one of its element match the specified predicate. Check is OK if list is empty.
         *
         * @param predicate the element predicate to match
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereAny(final Predicate<? super WebElement> predicate) {
            return new ListFindr(by, filters, composeCheckers(checkAny(predicate)));
        }

        /**
         * Wait for the list findr so that all of its element match the specified predicate. Check is OK if list is empty.
         *
         * @param predicate the element predicate to match
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereAll(final Predicate<? super WebElement> predicate) {
            return new ListFindr(by, filters, composeCheckers(checkAll(predicate)));
        }

        private Predicate<List<WebElement>> checkAny(final Predicate<? super WebElement> predicate) {
            return new Predicate<>() {
                @Override
                public boolean test(List<WebElement> elements) {
                    if (elements.size() == 0) {
                        return true;
                    }
                    for (WebElement element : elements) {
                        if (predicate.test(element)) {
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
            return new Predicate<>() {
                @Override
                public boolean test(List<WebElement> elements) {
                    for (WebElement element : elements) {
                        if (!predicate.test(element)) {
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
            return new Predicate<>() {
                @Override
                public boolean test(List<WebElement> elements) {
                    return elements != null && elements.size() == expectedCount;
                }

                @Override
                public String toString() {
                    return "elemCount(" + expectedCount + ")";
                }
            };
        }

        /**
         * Counts that there's 1 element matching the list findr, and return it.
         * Shorthand for <code>count(1).at(0)</code>.
         *
         * @return
         */
        public Findr expectOne() {
            return count(1).at(0);
        }

        /**
         * Evaluates this ListFindr and invokes passed callback if the whole chain suceeded. Throws
         * a TimeoutException if the condition chain didn't match.
         *
         * @param callback the callback to call if the chain succeeds
         * @param <T>      the rturn type of the callback
         * @return the result of the callback
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public <T> T eval(final Function<List<WebElement>, T> callback) throws TimeoutException {
            logDebug("[Findr] ListFindr eval");
            return wrapWebDriverWaitList(withoutWebDriverException(input -> {
                SearchContext c = f == null ? input : f.apply(input);
                if (c == null) {
                    return null;
                }
                List<WebElement> elements = c.findElements(by);
                if (elements == null) {
                    return null;
                }
                List<WebElement> filtered = filterElements(elements);
                if (checkers != null && !checkers.test(filtered)) {
                    logDebug("[Findr]  ! checkList KO: " + checkers);
                    logDebug("[Findr]  => Chain STOPPED before callback");
                    return null;
                } else {
                    if (isDebugEnabled() && checkers != null) {
                        logDebug("[Findr]  > checkList OK: " + checkers);
                    }
                }
                T res = callback.apply(filtered);
                if (res == null || (res instanceof Boolean && !((Boolean) res))) {
                    logDebug("[Findr]  => " + callback + " result : " + res + ", will try again");
                } else {
                    logDebug("[Findr]  => " + callback + " result : " + res + ", OK");
                }
                return res;
            }));
        }

        /**
         * Evaluates this ListFindr. Throws
         * a TimeoutException if the condition chain didn't match.
         *
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public void eval() throws TimeoutException {
            eval(IDENTITY_LIST);
        }

        /**
         * Evaluates this ListFindr. Throws
         * a TimeoutException if the condition chain didn't match.
         *
         * @param failureMessage A message to include in the timeout exception
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public void eval(String failureMessage) throws TimeoutException {
            try {
                eval(IDENTITY_LIST);
            } catch (TimeoutException e) {
                throw new TimeoutException(failureMessage, e);
            }
        }

        /**
         * Evaluates this ListFindr and invokes passed callback if the whole chain suceeded. Throws
         * a TimeoutException with passed failure message if the condition chain didn't match.
         *
         * @param callback the callback to call if the chain succeeds
         * @param <T>      the rturn type of the callback
         * @return the result of the callback
         * @throws TimeoutException if at least one condition in the chain failed
         */
        public <T> T eval(Function<List<WebElement>, T> callback, String failureMessage) throws TimeoutException {
            try {
                return eval(callback);
            } catch (TimeoutException e) {
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
