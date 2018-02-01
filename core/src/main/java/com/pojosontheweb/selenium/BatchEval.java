package com.pojosontheweb.selenium;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static com.pojosontheweb.selenium.Findr.logDebug;

/**
 * Allows to batch several "steps" (individual Findr evaluations) into a single, "atomic", retry-all unit.
 * BatchEvals will evaluate all "steps" one after the other, piping intermediate results
 * along the way, and return the final result only if everything went ok.
 * If a step fails, then BatchEval will retry everything from the beginning, until
 * the max retry count is reached (and then throw a <code>TimeoutException</code>
 *
 * Just like Findr instances, BatchEval is immutable, and can be safely (re)used in any context.
 *
 * @param <R> the result of the batch evaluation, if successful
 */
public class BatchEval<R> {

    public static final int DEFAULT_RETRIES = 5;

    private final Function<Void,R> func;
    private final int count;

    private BatchEval(Function<Void, R> func, int count) {
        this.func = func;
        this.count = count;
    }

    /**
     * Create a new BatchEval instance to use as an entry point for <code>add()</code> and <code>eval()</code>
     * @param findr the findr to evaluate
     * @param func the eval function callback
     * @param <R> the result of evaluation
     * @return a BatchEval instance
     */
    public static <R> BatchEval<R> batch(final Findr findr, final Function<WebElement, R> func) {
        return new BatchEval<R>(new Function<Void, R>() {
            @Override
            public R apply(Void aVoid) {
                logDebug(">> Batch #0");
                R res = findr.eval(func);
                logDebug("<< Batch #0, returning " + res);
                return res;
            }
        }, 1);
    }


    /**
     * Evaluates everything with the default retry count
     */
    public R eval() {
        return eval(DEFAULT_RETRIES);
    }

    /**
     * Evaluates everything with passed retry count
     * @param retries the retry count
     */
    public R eval(int retries) {
        try {
            logDebug(">> Batch eval starting, retries = " + retries);
            R res = func.apply(null);
            logDebug("<< Batch eval OK");
            return res;
        } catch (TimeoutException e) {
            if (retries > 0) {
                // try again
                logDebug("Batch eval caught timeout exception, retries = " + retries + ", retrying");
                return eval(retries - 1);
            } else {
                // stop trying and bail out
                throw new TimeoutException("Batch eval out of retries", e);
            }
        }
    }

    /**
     * Callback interface for "adding" steps to the batch eval.
     */
    public interface BatchEvalCallback<InType,OutType> {
        OutType apply(WebElement e, InType t);
    }

    /**
     * Add a "step" to this batch eval.
     * @param otherFindr the findr for the step to add
     * @param callback the callback for the step to add
     * @param <O> the return type of the new step
     * @return a new BatchEval instance with added step
     */
    public <O> BatchEval<O> add(final Findr otherFindr, final BatchEvalCallback<R,O> callback) {
        return new BatchEval<O>(
                Functions.compose(
                        new Function<R,O>() {
                            @Override
                            public O apply(final R r) {
                                return otherFindr.eval(new Function<WebElement, O>() {
                                    @Override
                                    public O apply(WebElement webElement) {
                                        logDebug(">> Batch #" + count);
                                        O res = callback.apply(webElement, r);
                                        logDebug("<< Batch #" + count + ", returning " + res);
                                        return res;
                                    }
                                });
                            }
                        },
                        func
                ),
                count + 1
        );
    }


}
