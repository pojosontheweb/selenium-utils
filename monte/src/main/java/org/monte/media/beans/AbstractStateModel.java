

package org.monte.media.beans;

import javax.swing.event.*;

public class AbstractStateModel {
    protected EventListenerList listenerList;
    protected ChangeEvent changeEvent;


    public AbstractStateModel() {
    }

    public void addChangeListener(ChangeListener l) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
        if (listenerList != null) {

            Object[] listeners = listenerList.getListenerList();


            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==ChangeListener.class) {

                    if (changeEvent == null) {
                        changeEvent = new ChangeEvent(this);
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
                }
            }
        }
    }
}
