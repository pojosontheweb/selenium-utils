package com.pojosontheweb.selenium;


import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import org.openqa.selenium.TimeoutException;

import static com.pojosontheweb.selenium.Findr.logDebug;

public class Retry {

    private static abstract class AbstractRetry {
        final int count;
        final int retries;

        AbstractRetry(int count, int retries) {
            this.count = count;
            this.retries = retries;
        }
    }

    public static class RetryWithResult<T> extends AbstractRetry {

        private final Supplier<T> func;

        private RetryWithResult(int count, int retries, Supplier<T> func) {
            super(count, retries);
            this.func = func;
        }

        public T eval() {
            return doEval(retries);
        }

        private T doEval(int retries) {
            try {
                return func.get();
            } catch (TimeoutException e) {
                if (retries > 1) {
                    return doEval(retries - 1);
                } else {
                    throw e;
                }
            }
        }

        public <O> RetryWithResult<O> add(final Function<T,O> mapper) {
            return new RetryWithResult<O>(count + 1, retries, new Supplier<O>() {
                @Override
                public O get() {
                    return mapper.apply(func.get());
                }
            });
        }

        public <O> RetryWithResult<O> add(final Findr other, final Function<T,O> mapper) {
            return add(new Function<T, O>() {
                @Override
                public O apply(T t) {
                    other.eval();
                    return mapper.apply(t);
                }
            });
        }

        public RetryWithResult<T> add(final Findr other) {
            return add(other, Functions.<T>identity());
        }

        public RetryNoResult add(final RetryConsumer<T> consumer) {
            return new RetryNoResult(
                    count,
                    retries,
                    new Runnable() {
                        @Override
                        public void run() {
                            consumer.accept(func.get());
                        }
                    }
            );
        }
    }

    public static class RetryNoResult extends AbstractRetry {

        private final Runnable runnable;

        private RetryNoResult(int count, int retries, Runnable runnable) {
            super(count, retries);
            this.runnable = runnable;
        }

        public void eval() {
            doEval(retries);
        }

        private void doEval(int retries) {
            try {
                runnable.run();
            } catch (TimeoutException e) {
                if (retries > 1) {
                    doEval(retries - 1);
                } else {
                    throw e;
                }
            }
        }

        public RetryNoResult add(final Runnable other) {
            return new RetryNoResult(count + 1, retries, new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    logDebug("[Retry] step #" + count);
                    other.run();
                }
            });
        }

        public RetryNoResult add(final Findr f) {
            return add(new Runnable() {
                @Override
                public void run() {
                    f.eval();
                }
            });
        }

        public RetryNoResult add(final Findr.ListFindr f) {
            return add(new Runnable() {
                @Override
                public void run() {
                    f.eval();
                }
            });
        }

        public <O> RetryWithResult<O> add(final Supplier<O> supplier) {
            return new RetryWithResult<O>(
                    count,
                    retries,
                    new Supplier<O>() {
                        @Override
                        public O get() {
                            runnable.run();
                            return supplier.get();
                        }
                    }
            );
        }

    }

    public interface RetryConsumer<T> {
        void accept(T t);
    }


    public static RetryNoResult retry(int retries) {
        return new RetryNoResult(0, retries, new Runnable() {
            @Override
            public void run() {
                // it's a noop
            }
        });
    }


    public static <T> RetryWithResult<T> retry(int retries, Supplier<T> supplier) {
        return new RetryWithResult<T>(0, retries, supplier);
    }



}
