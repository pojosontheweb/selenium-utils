
package org.monte.media.concurrent;

import java.util.*;

public abstract class EventLoop {

    protected Thread eventProcessor;


    private int priority;


    private final Vector queue = new Vector();


    private boolean isCoalesce;

    private volatile boolean isAlive = true;


    public EventLoop() {
        this(Thread.NORM_PRIORITY);
    }

    public EventLoop(int priority) {
        this.priority = priority;
    }

    protected void collectEvent(Object event) {
        synchronized(queue) {
            if (! isCoalesce || ! queue.contains(event)) {
                queue.addElement(event);
                if (isAlive) startProcessor();
            }
        }
    }


    public void setCoalesce(boolean b) {
        isCoalesce = b;
    }

    public boolean isCoalesce() {
        return isCoalesce;
    }


    public void start() {
        synchronized(queue) {
            isAlive = true;
            startProcessor();
        }
    }


    public void stop() {
        isAlive = false;
    }

    public void join() throws InterruptedException {
        Thread t = eventProcessor;
        if (t != null) {
            t.join();
        }
    }



    public void clear() {
        synchronized(queue) {
            queue.removeAllElements();
        }
    }


    private void startProcessor() {
        synchronized(queue) {
            if (eventProcessor == null) {
                eventProcessor = new Thread(this+" Event Processor") {
                    public void run() {
                        processEvents();
                    }
                };
                try {



                    eventProcessor.setDaemon(false);
                } catch (SecurityException e) {}
                try {
                    eventProcessor.setPriority(priority);
                } catch (SecurityException e) {}
                eventProcessor.start();
            }
        }
    }


   protected abstract void processEvent(Object event);


    protected void processEvents() {
        Object event;
        while (isAlive) {
            synchronized(queue) {
                if (queue.isEmpty()) {
                    eventProcessor = null;
                    return;
                }
                event = queue.elementAt(0);
                queue.removeElementAt(0);
            }
            try {
                processEvent(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
