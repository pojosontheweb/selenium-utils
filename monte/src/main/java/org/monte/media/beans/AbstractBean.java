
package org.monte.media.beans;

import java.beans.*;


public class AbstractBean extends Object implements java.io.Serializable {
	protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);


	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener( propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
