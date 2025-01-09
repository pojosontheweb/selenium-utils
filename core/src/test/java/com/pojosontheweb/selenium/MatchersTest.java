package com.pojosontheweb.selenium;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchersTest {

  @Test
  public void matcherPredicate() {
    var predicate = Findrs.matcherPredicate(CoreMatchers.nullValue());
    assertEquals("null", predicate.toString());
    assertTrue(predicate.test(null));
    assertFalse(predicate.test(13));
    assertEquals("null", predicate.toString());
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
      assertEquals("was null", d.toString());
    }
    {
      var d = new StringDescription();
      matcher.describeMismatch(new FakeWebElement(), d);
      assertEquals("was \"\"", d.toString());
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
    assertEquals("mapped(getAttribute(foo),\"bar\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), new FakeWebElement() {
      @Override
      public String getAttribute(String name) {
        return "boom";
      }
    });
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getAttribute(foo),\"bar\"), was \"boom\""));
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
    assertEquals("mapped(getAttribute(foo),a string starting with \"bar\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), new FakeWebElement() {
      @Override
      public String getAttribute(String name) {
        return "boom";
      }
    });
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getAttribute(foo),a string starting with \"bar\"), was \"boom\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getAttribute(String name) {
        return "boom";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getAttribute(foo),a string ending with \"bar\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getAttribute(foo),a string ending with \"bar\"), was \"boom\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getAttribute(String name) {
        return "tra la la";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(class,a collection containing \"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(class,a collection containing \"foo\"), was <[tra, la, la]>"));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getText() {
        return "bar";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getText,\"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getText,\"foo\"), was \"bar\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getText() {
        return "bar";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getText,a string starting with \"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getText,a string starting with \"foo\"), was \"bar\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getText() {
        return "bar";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getText,a string containing \"oof\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getText,a string containing \"oof\"), was \"bar\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getText() {
        return "bar";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getText,a string ending with \"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getText,a string ending with \"foo\"), was \"bar\""));
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
    assertEquals("mapped(isEnabled,<true>)", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), new FakeWebElement());
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(isEnabled,<true>), was <false>"));
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
    assertEquals("mapped(isDisplayed,<true>)", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), new FakeWebElement());
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(isDisplayed,<true>), was <false>"));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getText() {
        return "toto42";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getText,/\\d+/)", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getText,/\\d+/), was \"toto42\""));
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
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getCssValue(String name) {
        return "bar";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("mapped(getCssValue(mycss),\"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! mapped(getCssValue(mycss),\"foo\"), was \"bar\""));
  }

  @Test
  public void notPredicate() throws Exception {
    var predicate = Findrs.not(Findrs.cssValue("mycss", "foo"));
    assertEquals("not mapped(getCssValue(mycss),\"foo\")", predicate.toString());
    assertTrue(predicate.test(null));
    assertTrue(predicate.test(new FakeWebElement() {
      @Override
      public String getCssValue(String name) {
        return "bar";
      }
    }));
    FakeWebElement nomatch = new FakeWebElement() {
      @Override
      public String getCssValue(String name) {
        return "foo";
      }
    };
    assertFalse(predicate.test(nomatch));
    assertEquals("not mapped(getCssValue(mycss),\"foo\")", predicate.toString());
    var lines = findrDebugCapture(findr -> findr.where(predicate), nomatch);
    assertThat(lines, CoreMatchers.hasItem("[Findr]  ! not mapped(getCssValue(mycss),\"foo\")"));
  }

  static List<String> findrDebugCapture(Function<Findr, Findr> fixture, WebElement element) {
    var findr = Findr.fromWebElement(null, element);
    var saved = Findr.getDebugHandler();
    final List<String> lines = new ArrayList<>();
    Findr.setDebugHandler(l -> {
      //System.out.println("findrDebugCapture|" + l);
      lines.add(l);
      return null;
    });
    try {
      fixture.apply(findr).eval_(w -> true).apply(element);
      return lines;
    } finally {
      Findr.setDebugHandler(saved);
    }
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
