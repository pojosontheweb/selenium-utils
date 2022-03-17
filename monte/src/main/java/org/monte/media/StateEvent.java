
package org.monte.media;

import java.util.EventObject;

public class StateEvent
extends EventObject {

    private int state_;

    public StateEvent(Object source, int state) {
        super(source);
        state_ = state;
    }

    public int getNewState() {
        return state_;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",state=" + state_ + "]";

    }
}
