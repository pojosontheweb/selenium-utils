package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.sun.jna.platform.unix.X11;
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
        final List<String> l = new ArrayList<String>();
        Retry.retry(5)
                .add(new Runnable() {
                    @Override
                    public void run() {
                        i1.incrementAndGet();
                        l.add("A");
                    }
                })
                .add(new Runnable() {
                    @Override
                    public void run() {
                        i2.incrementAndGet();
                        l.add("B");
                    }
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(Arrays.asList("A", "B"), l);

    }

    @Test
    public void testRetriesResultOk() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        String s = Retry.retry(5,
                    new Supplier<String>() {
                        @Override
                        public String get() {
                            i1.incrementAndGet();
                            return "A";
                        }
                    }
                )
                .add(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        i2.incrementAndGet();
                        return s+"B";
                    }
                })
                .eval();
        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals("AB", s);
    }


    @Test
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
    public void testRetriesResultFindrs() {
        final WebDriver d = DriverBuildr.fromSysProps().build();
        final List<String> l = new ArrayList<String>();
        final AtomicInteger i = new AtomicInteger(0);
        boolean failed = false;
        try {
            final Findr f = new Findr(d);
            d.get("http://www.google.com");
            String s = Retry.retry(
                            5,
                            new Supplier<String>() {
                                @Override
                                public String get() {
                                    return "A";
                                }
                            }
                    )
                    .add(f.$("#viewport"))
                    .add(
                            f.$("#viewport"),
                            new Function<String, String>() {
                                @Override
                                public String apply(String s) {
                                    return s + "B";
                                }
                            }
                    )
                    .add(
                            new Function<String, String>() {
                                @Override
                                public String apply(String s) {
                                    i.incrementAndGet();
                                    return s + "C";
                                }
                            }
                    )
                    .eval();
            assertEquals("ABC", s);
        } finally {
            d.quit();
        }
        assertEquals(1, i.get());
    }

}
