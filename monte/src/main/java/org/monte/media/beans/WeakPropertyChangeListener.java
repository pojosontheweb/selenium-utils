
package org.monte.media.beans;

import java.beans.*;
import java.lang.ref.*;


public class WeakPropertyChangeListener implements PropertyChangeListener {
    private WeakReference<PropertyChangeListener> weakRef;

    public WeakPropertyChangeListener(PropertyChangeListener target) {
        this.weakRef = new WeakReference<PropertyChangeListener>(target);
    }

    
    protected void removeFromSource(PropertyChangeEvent event) {

        Object src = event.getSource();
        try {
            src.getClass().getMethod("removePropertyChangeListener", new Class[] {PropertyChangeListener.class}).invoke(src, this);
        } catch (Exception ex) {
            InternalError ie = new InternalError("Could not remove WeakPropertyChangeListener from "+src+".");
            ie.initCause(ex);
            throw ie;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        PropertyChangeListener listener = (PropertyChangeListener) weakRef.get();
        if (listener == null) {
            removeFromSource(event);
            return;
        }
        listener.propertyChange(event);
    }

    
    public PropertyChangeListener getTarget() {
        return weakRef.get();
    }

    @Override
    public String toString() {
        return super.toString()+"["+weakRef.get()+"]";
    }
}
