package com.pojosontheweb.selenium;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class Issue23RetryTest {

    @Test
    public void testRetriesOk() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        final List<String> l = new ArrayList<String>();
        Retry.retry(5)
                .add(() -> {
                    i1.incrementAndGet();
                    l.add("A");
                })
                .add(() -> {
                    i2.incrementAndGet();
                    l.add("B");
                })
                .add(() -> {
                    i3.incrementAndGet();
                    l.add("C");
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(1, i3.get());
        assertEquals(Arrays.asList("A", "B", "C"), l);

    }

    @Test
    public void testRetriesResultOk() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final AtomicInteger i3 = new AtomicInteger(0);
        String s = Retry.retry()
                .add(() -> {
                    i1.incrementAndGet();
                    return "A";
                })
                .add(s2 -> {
                    i2.incrementAndGet();
                    return s2 + "B";
                })
                .add(s2 -> {
                    i3.incrementAndGet();
                    return s2 +"C";
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(1, i3.get());
        assertEquals("ABC", s);
    }


    @Test
    @Ignore
    public void testRetriesFindrs() {
        final WebDriver d = DriverBuildr.fromSysProps().build();
        final List<String> l = new ArrayList<String>();
        final AtomicInteger i = new AtomicInteger(0);
        boolean failed = false;
        try {
            final Findr f = new Findr(d);
            d.get("http://www.google.com");
            Retry.retry(5)
                    .add(f.$("#viewport"))
                    .add(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("I am useless");
                            l.add("A");
                            i.incrementAndGet();
                        }
                    })
                    .add(f.$$("#viewport").count(1))
                    .add(f.$$("#viewport").count(1).at(0))
                    .add(new Runnable() {
                        @Override
                        public void run() {
                            l.add("B");
                            f.$$("#viewport").count(1).at(0).eval();
                        }
                    })
                    .add(f.setTimeout(2).$("#i-dont-exist")) // just to make it fail
                    .eval();
        } catch (TimeoutException e) {
            failed = true;
        } finally {
            d.quit();
        }
        assertTrue(failed);
        assertEquals(5, i.get());
        assertEquals(Arrays.asList("A", "B", "A", "B", "A", "B", "A", "B", "A", "B"), l);
    }

    @Test
    public void testRetriesNoResultToResult() {
        final AtomicInteger i = new AtomicInteger(0);
        String s = Retry.retry(5)
                .add(() -> {
                    i.incrementAndGet();
                })
                .add(() -> "ABC")
                .eval();
        assertEquals(1, i.get());
        assertEquals("ABC", s);
    }


}
