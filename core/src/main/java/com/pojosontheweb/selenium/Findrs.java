package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

public class Findrs {

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
                    if (cssClasses==null) {
                        return false;
                    }
                    List<String> tokens = Arrays.asList(cssClasses.split("\\s"));
                    return tokens.contains(className);
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
     * inner text contains passed text.
     * @param expectedContains the expected contained text
     * @return a new Predicate
     */
    public static Predicate<WebElement> textContains(final String expectedContains) {
        return new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                String text = input.getText();
                if (text==null) {
                    return false;
                }
                return text.contains(expectedContains);
            }

            @Override
            public String toString() {
                return "textContains(" + expectedContains + ")";

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
         * Create and return a new Predicate that checks if the element's text matches passed regexp.
         * @return a new Predicate
         */
        public static Predicate<WebElement> textMatches(final String regexp) {
            return new Predicate<WebElement>() {
                @Override
                public boolean apply(WebElement input) {
                    String text = input.getText();
                    if (text==null) {
                        return false;
                    }
                    return text.matches(regexp);
                }

                @Override
                public String toString() {
                    return "matches(" + regexp + ")";
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

    public static Function<WebElement, ?> click() {
        return new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
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

    public static Function<WebElement, ?> clear() {
        return new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
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

    public static Function<WebElement, ?> sendKeys(final CharSequence... keys) {
        return new Function<WebElement, Object>() {
            @Override
            public Object apply(WebElement webElement) {
                try {
                    webElement.sendKeys(keys);
                } catch(Exception e) {
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

}
