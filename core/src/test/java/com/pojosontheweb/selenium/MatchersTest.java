package com.pojosontheweb.selenium;

import org.hamcrest.CoreMatchers;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchersTest {

    @Test
    public void matcherPredicate() {
        var predicate = Findrs.matcher(CoreMatchers.nullValue());
        assertEquals("null", predicate.toString());
        assertTrue(predicate.test(null));
        assertFalse(predicate.test(13));
        assertEquals("null was <13>", predicate.toString());
    }

    @Test
    public void mappedMatcher() {
        var matcher = Findrs.mapped("gnu", WebElement::getTagName, CoreMatchers.notNullValue());
        assertEquals("mapped(gnu,not null)", matcher.toString());
        {
            var d = new StringDescription();
            matcher.describeTo(d);
            assertEquals("mapped(gnu,not null)", d.toString());
        }
        {
            var d = new StringDescription();
            matcher.describeMismatch(null, d);
            assertEquals("[was null]", d.toString());
        }
        {
            var d = new StringDescription();
            matcher.describeMismatch(new FakeWebElement(), d);
            assertEquals("[was \"\"]", d.toString());
        }
    }

    @Test
    public void attrEquals() throws Exception {
        var predicate = Findrs.attrEquals("foo", "bar");
        assertEquals("mapped(getAttribute(foo),\"bar\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "bar";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "boom";
            }
        }));
        assertEquals("mapped(getAttribute(foo),\"bar\") [was \"boom\"]", predicate.toString());
    }

    @Test
    public void attrStartsWith() throws Exception {
        var predicate = Findrs.attrStartsWith("foo", "bar");
        assertEquals("mapped(getAttribute(foo),a string starting with \"bar\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "barbar";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "boom";
            }
        }));
        assertEquals("mapped(getAttribute(foo),a string starting with \"bar\") [was \"boom\"]", predicate.toString());
    }

    @Test
    public void attrEndsWith() throws Exception {
        var predicate = Findrs.attrEndsWith("foo", "bar");
        assertEquals("mapped(getAttribute(foo),a string ending with \"bar\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "barbar";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "boom";
            }
        }));
        assertEquals("mapped(getAttribute(foo),a string ending with \"bar\") [was \"boom\"]", predicate.toString());
    }

    @Test
    public void hasClass() throws Exception {
        var predicate = Findrs.hasClass("foo");
        assertEquals("mapped(class,a collection containing \"foo\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "gnu foo bar";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getAttribute(String name) {
                return "tra la la";
            }
        }));
        assertEquals("mapped(class,a collection containing \"foo\") [was <[tra, la, la]>]", predicate.toString());
    }

    @Test
    public void textEquals() throws Exception {
        var predicate = Findrs.textEquals("foo");
        assertEquals("mapped(getText,\"foo\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "foo";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "bar";
            }
        }));
        assertEquals("mapped(getText,\"foo\") [was \"bar\"]", predicate.toString());
    }

    @Test
    public void textStartsWith() throws Exception {
        var predicate = Findrs.textStartsWith("foo");
        assertEquals("mapped(getText,a string starting with \"foo\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "foofoo";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "bar";
            }
        }));
        assertEquals("mapped(getText,a string starting with \"foo\") [was \"bar\"]", predicate.toString());
    }

    @Test
    public void textContains() throws Exception {
        var predicate = Findrs.textContains("oof");
        assertEquals("mapped(getText,a string containing \"oof\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "foofoo";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "bar";
            }
        }));
        assertEquals("mapped(getText,a string containing \"oof\") [was \"bar\"]", predicate.toString());
    }

    @Test
    public void textEndsWith() throws Exception {
        var predicate = Findrs.textEndsWith("foo");
        assertEquals("mapped(getText,a string ending with \"foo\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "foofoo";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "bar";
            }
        }));
        assertEquals("mapped(getText,a string ending with \"foo\") [was \"bar\"]", predicate.toString());
    }

    @Test
    public void isEnabled() throws Exception {
        var predicate = Findrs.isEnabled();
        assertEquals("mapped(isEnabled,<true>)", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        }));
        assertFalse(predicate.test(new FakeWebElement()));
        assertEquals("mapped(isEnabled,<true>) [was <false>]", predicate.toString());
    }

    @Test
    public void isDisplayed() throws Exception {
        var predicate = Findrs.isDisplayed();
        assertEquals("mapped(isDisplayed,<true>)", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public boolean isDisplayed() {
                return true;
            }
        }));
        assertFalse(predicate.test(new FakeWebElement()));
        assertEquals("mapped(isDisplayed,<true>) [was <false>]", predicate.toString());
    }

    @Test
    public void textMatches() throws Exception {
        var predicate = Findrs.textMatches("\\d+");
        assertEquals("mapped(getText,/\\d+/)", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getText() {
                return "13";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement(){
            @Override
            public String getText() {
                return "toto42";
            }
        }));
        assertEquals("mapped(getText,/\\d+/) [was \"toto42\"]", predicate.toString());
    }

    @Test
    public void cssValue() throws Exception {
        var predicate = Findrs.cssValue("mycss", "foo");
        assertEquals("mapped(getCssValue(mycss),\"foo\")", predicate.toString());
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement() {
            @Override
            public String getCssValue(String name) {
                return "foo";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement(){
            @Override
            public String getCssValue(String name) {
                return "bar";
            }
        }));
        assertEquals("mapped(getCssValue(mycss),\"foo\") [was \"bar\"]", predicate.toString());
    }

    @Test
    public void notPredicate() throws Exception {
        var predicate = Findrs.not(Findrs.cssValue("mycss", "foo"));
        assertEquals("not mapped(getCssValue(mycss),\"foo\")", predicate.toString());
        assertTrue(predicate.test(null));
        assertTrue(predicate.test(new FakeWebElement(){
            @Override
            public String getCssValue(String name) {
                return "bar";
            }
        }));
        assertFalse(predicate.test(new FakeWebElement() {
            @Override
            public String getCssValue(String name) {
                return "foo";
            }
        }));
        assertEquals("not mapped(getCssValue(mycss),\"foo\")", predicate.toString());
    }

    static class FakeWebElement implements WebElement {

        @Override
        public void click() {

        }

        @Override
        public void submit() {

        }

        @Override
        public void sendKeys(CharSequence... keysToSend) {

        }

        @Override
        public void clear() {

        }

        @Override
        public String getTagName() {
            return "";
        }

        @Override
        public String getAttribute(String name) {
            return "";
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public String getText() {
            return "";
        }

        @Override
        public List<WebElement> findElements(By by) {
            return List.of();
        }

        @Override
        public WebElement findElement(By by) {
            return null;
        }

        @Override
        public boolean isDisplayed() {
            return false;
        }

        @Override
        public Point getLocation() {
            return null;
        }

        @Override
        public Dimension getSize() {
            return null;
        }

        @Override
        public Rectangle getRect() {
            return null;
        }

        @Override
        public String getCssValue(String propertyName) {
            return "";
        }

        @Override
        public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
            return null;
        }
    }


}
