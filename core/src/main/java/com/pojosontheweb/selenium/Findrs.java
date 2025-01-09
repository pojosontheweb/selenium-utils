package com.pojosontheweb.selenium;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Groups useful predicates and functions.
 */
public class Findrs {

    /**
     * Map a web element before composing it with the given matcher.
     *
     * @param describe Describe the mapping
     * @param fun      Map web element for matcher
     * @param matcher  Compose with this matcher
     * @return New composed matcher
     */
    public static <T> Matcher<WebElement> mapped(String describe, Function<WebElement, T> fun, Matcher<T> matcher) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(Object item) {
                if (item != null) {
                    return matcher.matches(fun.apply((WebElement) item));
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("mapped(%s,", describe)).appendDescriptionOf(matcher).appendText(")");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                if (item != null) {
                    description.appendText("was ").appendValue(fun.apply((WebElement) item));
                } else {
                    description.appendText("was null");
                }
            }
        };
    }

    /**
     * A regex matcher.
     *
     * @param regex The pattern to match
     * @return New regex matcher
     */
    public static Matcher<String> matchesPattern(String regex) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(Object item) {
                if (item instanceof String s) {
                    return s.matches(regex);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("/%s/", regex));
            }
        };
    }

    private static Predicate<WebElement> matchAttribute(String attrName, Matcher<String> matcher) {
        return matcherPredicate(mapped(String.format("getAttribute(%s)", attrName), w -> w.getAttribute(attrName), matcher));
    }

    private static Predicate<WebElement> matchText(Matcher<String> matcher) {
        return matcherPredicate(mapped("getText", WebElement::getText, matcher));
    }

    /**
     * Create and return a new Predicate that matches an element's attribute value
     *
     * @param attrName      the name of the attribute
     * @param expectedValue the expected value of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrEquals(final String attrName, final String expectedValue) {
        return matchAttribute(attrName, CoreMatchers.equalTo(expectedValue));
    }

    /**
     * Create and return a new Predicate that checks for an attribute start
     *
     * @param attrName           the name of the attribute
     * @param expectedStartsWith the expected start of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrStartsWith(final String attrName, final String expectedStartsWith) {
        return matchAttribute(attrName, CoreMatchers.startsWith(expectedStartsWith));
    }

    /**
     * Create and return a new Predicate that checks for an attribute end
     *
     * @param attrName         the name of the attribute
     * @param expectedEndsWith the expected start of the attribute
     * @return a new Predicate
     */
    public static Predicate<WebElement> attrEndsWith(final String attrName, final String expectedEndsWith) {
        return matchAttribute(attrName, CoreMatchers.endsWith(expectedEndsWith));
    }

    /**
     * Create and return a new Predicate that checks for the presence of a css class
     * on a an element.
     *
     * @param className the expected css class
     * @return a new Predicate
     */
    public static Predicate<WebElement> hasClass(final String className) {
        return matcherPredicate(mapped("class", w -> Arrays.asList(w.getAttribute("class").split("\\s")),
                CoreMatchers.hasItem(className)));
    }

    /**
     * Create and return a new Predicate that checks for an element's
     * inner text.
     *
     * @param expected the expected inner text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textEquals(final String expected) {
        return matchText(CoreMatchers.equalTo(expected));
    }

    /**
     * Create and return a new Predicate checking that an element's
     * inner text starts with passed text.
     *
     * @param expectedStartsWith the expected start of text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textStartsWith(final String expectedStartsWith) {
        return matchText(CoreMatchers.startsWith(expectedStartsWith));
    }

    /**
     * Create and return a new Predicate checking that an element's
     * inner text contains passed text.
     *
     * @param expectedContains the expected contained text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textContains(final String expectedContains) {
        return matchText(CoreMatchers.containsString(expectedContains));
    }

    /**
     * Create and return a new Predicate checking that an element's
     * inner text ends with passed text.
     *
     * @param expectedEndsWith the expected start of text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textEndsWith(final String expectedEndsWith) {
        return matchText(CoreMatchers.endsWith(expectedEndsWith));
    }

    /**
     * Create and return a new Predicate that checks if the element is enabled.
     *
     * @return a new Predicate
     */
    public static Predicate<WebElement> isEnabled() {
        return matcherPredicate(mapped("isEnabled", WebElement::isEnabled, CoreMatchers.equalTo(true)));
    }

    /**
     * Create and return a new Predicate that checks if the element is displayed.
     *
     * @return a new Predicate
     */
    public static Predicate<WebElement> isDisplayed() {
        return matcherPredicate(mapped("isDisplayed", WebElement::isDisplayed, CoreMatchers.equalTo(true)));
    }

    /**
     * Create and return a new Predicate that checks if the element's text matches passed regexp.
     *
     * @return a new Predicate
     */
    public static Predicate<WebElement> textMatches(final String regexp) {
        return matchText(matchesPattern(regexp));
    }

    /**
     * Create and return a new Predicate that checks for a css value on the element.
     *
     * @param propName      the css prop name
     * @param expectedValue the expected css value
     * @return a new Predicate
     */
    public static Predicate<WebElement> cssValue(final String propName, final String expectedValue) {
        return matcherPredicate(mapped(String.format("getCssValue(%s)", propName), w -> w.getCssValue(propName), CoreMatchers.equalTo(expectedValue)));
    }

    /**
     * Create and return a new Predicate that inverses passed predicate.
     *
     * @param in the predicate to inverse
     * @return a new Predicate
     */
    public static Predicate<WebElement> not(final Predicate<WebElement> in) {
        return new Predicate<>() {
            @Override
            public boolean test(WebElement input) {
                return !in.test(input);
            }

            @Override
            public String toString() {
                return "not " + in.toString();
            }
        };
    }

    public static Function<WebElement, Boolean> click() {
        return new Function<>() {
            @Override
            public Boolean apply(WebElement webElement) {
                try {
                    webElement.click();
                } catch (Exception e) {
                    // click threw : try again !
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "click()";
            }
        };
    }

    public static Function<WebElement, Boolean> clear() {
        return new Function<>() {
            @Override
            public Boolean apply(WebElement webElement) {
                try {
                    webElement.clear();
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "clear()";
            }
        };
    }

    public static Function<WebElement, Boolean> sendKeys(final CharSequence... keys) {
        return new Function<>() {
            @Override
            public Boolean apply(WebElement webElement) {
                try {
                    webElement.sendKeys(keys);
                } catch (Exception e) {
                    // sendKeys throws, try again !
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "sendKeys(" + Arrays.toString(keys) + ")";
            }
        };
    }

    // for testing
    static <T> Predicate<T> matcherPredicate(Matcher<T> matcher) {
        return new MatcherPredicate<>(matcher);
    }

    record MatcherPredicate<T>(Matcher<T> matcher) implements Predicate<T> {

        public boolean test(T w) {
            return matcher.matches(w);
        }

        @Override
        public String toString() {
            return StringDescription.toString(matcher);
        }
    }
}
