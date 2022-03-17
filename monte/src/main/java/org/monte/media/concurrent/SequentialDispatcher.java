
package org.monte.media.concurrent;


public class SequentialDispatcher extends EventLoop {

    public SequentialDispatcher() {
    }

    public SequentialDispatcher(int priority) {
        super(priority);
    }


    protected void processEvent(Object event) {
        Runnable r = (Runnable) event;
        r.run();
    }


    public void dispatch(Runnable r) {
        collectEvent(r);
    }
}
