package com.pojosontheweb.selenium;


import org.openqa.selenium.TimeoutException;
import static com.pojosontheweb.selenium.Findr.logDebug;

public class Retry {

    private final Runnable runnable;
    private final int count;

    private Retry(int count, Runnable runnable) {
        this.count = count;
        this.runnable = runnable;
    }

    public static Retry retry() {
        return new Retry(0, new Runnable() {
            @Override
            public void run() {
                // no-op !
            }
        });
    }

    public Retry add(final Runnable other) {
        return new Retry(count + 1, new Runnable() {
            @Override
            public void run() {
                runnable.run();
                logDebug("[Retry] step #" + count);
                other.run();
            }
        });
    }

    public Retry add(final Findr f) {
        return add(new Runnable() {
            @Override
            public void run() {
                f.eval();
            }
        });
    }

    public Retry add(final Findr.ListFindr f) {
        return add(new Runnable() {
            @Override
            public void run() {
                f.eval();
            }
        });
    }

    public void eval(int retries) {
        logDebug("[Retry] >> eval retries = " + retries);
        try {
            this.runnable.run();
            logDebug("[Retry] << Ok");
        } catch (TimeoutException e) {
            if (retries > 1)  {
                eval(retries - 1);
            } else {
                throw new TimeoutException("Exhausted retries", e);
            }
        }
    }

}
