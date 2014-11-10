package com.pojosontheweb.selenium;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class ArticleTest extends ManagedDriverJunit4TestBase {

    @Test
    public void testNoWaitJustDoesntWork() {
        WebDriver d = getWebDriver();

        d.get("http://0.0.0.0:8080/index.html");

        // click initial button : this removes it from DOM and replaces by a new one
        WebElement container = d.findElement(By.id("container"));
        WebElement btn0 = container.findElement(By.className("btn"));
        btn0.click();

        try {
            container.findElement(By.className("btn0"));
            Assert.fail("Should have thrown");
        } catch (NoSuchElementException e) {
            // expected : elem was removed from dom while we tried to find it
        } catch (Exception e) {
            Assert.fail("Unexpected error : " + e);
        }
    }

    @Test
    public void testImplicitWaitSeemsToWork() {
        WebDriver d = getWebDriver();

        d.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        d.get("http://0.0.0.0:8080/index.html");

        // hold ref to button
        WebElement container = d.findElement(By.id("container"));

        // click once
        container.findElement(By.className("btn")).click();

        // click again
        container.findElement(By.className("btn")).click();

        Assert.assertEquals("OK", container.findElement(By.className("result")).getText());
    }


    @Test
    public void testImplicitWaitDoesntAllowRefReuse() {

        WebDriver d = getWebDriver();

        d.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        d.get("http://0.0.0.0:8080/index.html");

        // hold ref to button
        WebElement btn = d.findElement(By.id("container")).findElement(By.className("btn"));

        // click once
        btn.click();

        // click again
        try {
            btn.click();
            Assert.fail("Should have thrown");
        } catch (StaleElementReferenceException e) {
            // expected !
            System.out.println(e);
        }
    }

    @Test
    public void testImplicitWaitDoesntMixWellWithExpectedConditions() {
        WebDriver d = getWebDriver();

        d.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        d.get("http://0.0.0.0:8080/index.html");

        // hold ref to button
        WebElement container = d.findElement(By.id("container"));

        // click once
        container.findElement(By.className("btn")).click();

        // click again
        WebElement btn = container.findElement(By.className("btn"));
        btn.click();

        Assert.assertEquals("OK", container.findElement(By.className("result")).getText());

        try {
            new WebDriverWait(d, 10).until(ExpectedConditions.elementToBeClickable(btn));
        } catch(StaleElementReferenceException e) {
            // expected : elementToBeClickable takes... a WebElement ! so here you're in major pain, you
            // need to use the "By" variant of the ExpectedConditions, which doesn't chain right
            System.out.println(e);
        }
    }
}
