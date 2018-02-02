package com.pojosontheweb.selenium;

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
        Retry.retry()
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
                .eval(5);

        assertEquals(1, i1.get());
        assertEquals(1, i2.get());
        assertEquals(Arrays.asList("A", "B"), l);

    }

    @Test
    public void testRetriesFailure() {
        final AtomicInteger i1 = new AtomicInteger(0);
        final AtomicInteger i2 = new AtomicInteger(0);
        final List<String> l = new ArrayList<String>();
        boolean failed = false;
        try {
            Retry.retry()
                    .add(new Runnable() {
                        @Override
                        public void run() {
                            i1.incrementAndGet();
                            l.add("A");
                            throw new TimeoutException("too bad");
                        }
                    })
                    .add(new Runnable() {
                        @Override
                        public void run() {
                            l.add("B");
                            i2.incrementAndGet();
                        }
                    })
                    .eval(5);
        } catch (TimeoutException e) {
            failed = true;
        }

        assertTrue(failed);
        assertEquals(5, i1.get());
        assertEquals(0, i2.get());
        assertEquals(Arrays.asList("A", "A", "A", "A", "A"), l);

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
            Retry.retry()
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
                    .eval(5);
        } catch (TimeoutException e) {
            failed = true;
        } finally {
            d.quit();
        }
        assertTrue(failed);
        assertEquals(5, i.get());
        assertEquals(Arrays.asList("A", "B", "A", "B", "A", "B", "A", "B", "A", "B"), l);

    }

}
