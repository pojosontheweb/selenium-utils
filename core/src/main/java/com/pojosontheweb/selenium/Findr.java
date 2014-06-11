package com.pojosontheweb.selenium;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.base.Functions;
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
    private static final int WAIT_TIMEOUT_SECONDS = 10; // secs

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
     */
    public Findr(WebDriver driver, int waitTimeout) {
        this(driver, waitTimeout, null, Collections.<String>emptyList());
    }

    private Findr(WebDriver driver, int waitTimeout, Function<SearchContext, WebElement> f, List<String> path) {
        this.driver = driver;
        this.waitTimeout = waitTimeout;
        this.f = f;
        this.path = path;
    }

    private <F,T> Function<F,T> wrapAndTrapCatchStaleElementException(final Function<F,T> function) {
        return new Function<F,T>() {
            @Override
            public T apply(F input) {
                try {
                    return function.apply(input);
                } catch(StaleElementReferenceException e) {
                    return null;
                }
            }
        };
    }

    private Findr compose(Function<SearchContext,WebElement> function, String pathElem) {
        Function<SearchContext,WebElement> newFunction = wrapAndTrapCatchStaleElementException(function);
        ArrayList<String> newPath = new ArrayList<String>(path);
        if (pathElem!=null) {
            newPath.add(pathElem);
        }
        if (f==null) {
            return new Findr(driver, waitTimeout, newFunction, newPath);
        } else {
            return new Findr(driver, waitTimeout, Functions.compose(newFunction, f), newPath);
        }
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

    private <T> T wrapWebDriverWait(final Function<WebDriver,T> callback) throws TimeoutException {
        try {
            return new WebDriverWait(driver, waitTimeout).until(callback);
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
        return wrapWebDriverWait(wrapAndTrapCatchStaleElementException(new Function<WebDriver, T>() {
            @Override
            public T apply(WebDriver input) {
                WebElement e = f.apply(input);
                if (e == null) {
                    return null;
                }
                return callback.apply(e);
            }
        }));
    }

    private static final Function<WebElement,?> IDENTITY_FOR_EVAL = new Function<WebElement, Object>() {
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

    private static final Predicate<WebElement> TRUE = com.google.common.base.Predicates.alwaysTrue();

    /**
     * Shortcut method : evaluates chain, and sends keys to target WebElement of this
     * Findr.
     * @param keys the text to send
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void sendKeys(final CharSequence... keys) throws TimeoutException {
        eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
                webElement.sendKeys(keys);
                return true;
            }

            @Override
            public String toString() {
                return "sendKeys(" + Arrays.toString(keys) + ")";
            }
        });
    }

    /**
     * Shortcut method : evaluates chain, and clicks target WebElement of this
     * Findr.
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void click() {
        eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
                webElement.click();
                return true;
            }

            @Override
            public String toString() {
                return "click()";
            }
        });
    }

    /**
     * Shortcut method : evaluates chain, and clears target WebElement of this
     * Findr.
     * @throws TimeoutException if at least one condition in the chain failed
     */
    public void clear() {
        eval(new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
                webElement.clear();
                return true;
            }

            @Override
            public String toString() {
                return "clear()";
            }
        });
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
        private final Integer waitCount;

        private ListFindr(By by) {
            this(by, TRUE, null);
        }

        private ListFindr(By by, Predicate<WebElement> filters, Integer waitCount) {
            this.by = by;
            this.filters = filters;
            this.waitCount = waitCount;
        }

        private Predicate<WebElement> wrapAndTrap(final Predicate<? super WebElement> predicate) {
            return new Predicate<WebElement>() {
                @Override
                public boolean apply(WebElement input) {
                    if (input==null) {
                        return false;
                    }
                    try {
                        return predicate.apply(input);
                    } catch(StaleElementReferenceException e) {
                        return false;
                    }

                }
            };
        }

        private <T> T wrapWebDriverWaitList(final Function<WebDriver,T> callback) throws TimeoutException {
            try {
                return new WebDriverWait(driver, waitTimeout).until(callback);
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
         */
        public ListFindr where(final Predicate<? super WebElement> predicate) {
            return new ListFindr(by, com.google.common.base.Predicates.<WebElement>and(filters, wrapAndTrap(predicate)), waitCount);
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
                    try {
                        elements = filterElements(input.findElements(by));
                    } catch(Exception e) {
                        return null;
                    }
                    if (elements==null) {
                        return null;
                    }
                    if (index>=elements.size()) {
                        return null;
                    }
                    return elements.get(index);
                }
            },
                    by.toString() + "[" + index + "]"
            );
        }

        private List<WebElement> filterElements(List<WebElement> source) {
            List<WebElement> filtered = new ArrayList<WebElement>();
            for (WebElement element : source) {
                if (filters.apply(element)) {
                    filtered.add(element);
                }
            }
            return filtered;
        }

        /**
         * Wait for the list findr to mach passed count
         * @param elemCount the expected count
         * @return a new ListFindr with updated chain
         */
        public ListFindr whereElemCount(int elemCount) {
            return new ListFindr(by, filters, elemCount);
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
            return wrapWebDriverWaitList(wrapAndTrapCatchStaleElementException(new Function<WebDriver, T>() {
                @Override
                public T apply(WebDriver input) {
                    SearchContext c = f==null ? input : f.apply(input);
                    if (c == null) {
                        return null;
                    }
                    List<WebElement> elements = c.findElements(by);
                    if (elements == null) {
                        return null;
                    }
                    if (waitCount!=null && elements.size()!=waitCount) {
                        return null;
                    }
                    return callback.apply(filterElements(elements));
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

    }

    // Utility statics
    // ---------------

    /**
     * Create and return a new Predicate that matches an element's attribute value
     * @param attrName the name of the attribute
     * @param expectedValue the expected value of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrEquals(final String attrName, final String expectedValue) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String attrVal = webElement.getAttribute(attrName);
                return attrVal!=null && attrVal.equals(expectedValue);
            }

            @Override
            public String toString() {
                return "attrEquals(" + attrName + "," + expectedValue + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks for an attribute start
     * @param attrName the name of the attribute
     * @param expectedStartsWith the expected start of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrStartsWith(final String attrName, final String expectedStartsWith) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String attrVal = webElement.getAttribute(attrName);
                return attrVal!=null && attrVal.startsWith(expectedStartsWith);
            }

            @Override
            public String toString() {
                return "attrStartsWith(" + attrName + "," + expectedStartsWith + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks for an attribute end
     * @param attrName the name of the attribute
     * @param expectedEndsWith the expected start of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrEndsWith(final String attrName, final String expectedEndsWith) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String attrVal = webElement.getAttribute(attrName);
                return attrVal!=null && attrVal.endsWith(expectedEndsWith);
            }

            @Override
            public String toString() {
                return "attrEndsWith(" + attrName + "," + expectedEndsWith + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks for the presence of a css class
     * on a an element.
     * @param className the expected css class
     * @return a new Predicate
     */
    public static Predicate<WebElement> hasClass(final String className) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String cssClasses = webElement.getAttribute("class");
                // TODO needs tokenize (substring ain't enough)
                return cssClasses!=null && cssClasses.contains(className);
            }

            @Override
            public String toString() {
                return "hasClass(" + className + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks for an element's
     * inner text.
     * @param expected the expected inner text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textEquals(final String expected) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String text = webElement.getText();
                return text!=null && text.equals(expected);
            }

            @Override
            public String toString() {
                return "textEquals(" + expected + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate checking that an element's
     * inner text starts with passed text.
     * @param expectedStartsWith the expected start of text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textStartsWith(final String expectedStartsWith) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                String text = input.getText();
                if (text==null) {
                    return false;
                }
                return text.startsWith(expectedStartsWith);
            }

            @Override
            public String toString() {
                return "textStartsWith(" + expectedStartsWith + ")";

            }
        };
    }

    /**
     * Create and return a new Predicate checking that an element's
     * inner text ends with passed text.
     * @param expectedEndsWith the expected start of text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textEndsWith(final String expectedEndsWith) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                String text = input.getText();
                if (text==null) {
                    return false;
                }
                return text.endsWith(expectedEndsWith);
            }

            @Override
            public String toString() {
                return "textEndsWith(" + expectedEndsWith + ")";

            }
        };
    }

    /**
     * Create and return a new Predicate that checks if the element is enabled.
     * @return a new Predicate
     */
    public static Predicate<WebElement> isEnabled() {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.isEnabled();
            }

            @Override
            public String toString() {
                return "isEnabled";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks if the element is displayed.
     * @return a new Predicate
     */
    public static Predicate<WebElement> isDisplayed() {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.isDisplayed();
            }

            @Override
            public String toString() {
                return "isDisplayed";
            }
        };
    }

    /**
     * Create and return a new Predicate that checks for a css value on the element.
     * @param propName the css prop name
     * @param expectedValue the expected css value
     * @return a new Predicate
     */
    public static Predicate<WebElement> cssValue(final String propName, final String expectedValue) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement webElement) {
                String attrVal = webElement.getCssValue(propName);
                return attrVal!=null && attrVal.equals(expectedValue);
            }

            @Override
            public String toString() {
                return "cssValue(" + propName + "," + expectedValue + ")";
            }
        };
    }

    /**
     * Create and return a new Predicate that inverses passed predicate.
     * @param in the predicate to inverse
     * @return a new Predicate
     */
    public static Predicate<WebElement> not(final Predicate<WebElement> in) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return !in.apply(input);
            }

            @Override
            public String toString() {
                return "not " + in.toString();
            }
        };
    }


}
