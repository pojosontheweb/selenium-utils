
package org.monte.media;

import org.monte.media.StateEvent;
import org.monte.media.StateListener;


public class StateTracker
implements StateListener {

    private StateModel model_;
    private int[] targetStates_;


    public StateTracker(StateModel model) {
        setStateModel(model);
    }


    public void setStateModel(StateModel model) {
        if (model_ != null) {
            model_.removeStateListener(this);
        }

        model_ = model;

        if (model_ != null) {
            model_.addStateListener(this);
        }
    }


    public void waitForState(int state) {
        int[] statelist = { state };
        waitForState( statelist );
    }


    public int waitForState(int[] states) {
        synchronized (this) {
            targetStates_ = states;

            while (true) {
                int state = model_.getState();
                for (int i=0; i < targetStates_.length; i++) {
                    if (state == targetStates_[i]) {
                        return targetStates_[i];
                    }
                }
                try { wait(); } catch (InterruptedException e) {}
            }
        }
    }


    public void stateChanged(StateEvent event) {
        synchronized (this) {
            if (targetStates_ != null) {
                int state = event.getNewState();

                for (int i=0; i < targetStates_.length; i++) {
                    if (state == targetStates_[i]) {
                        notifyAll();
                        break;
                    }
                }
            }
        }
    }
}
