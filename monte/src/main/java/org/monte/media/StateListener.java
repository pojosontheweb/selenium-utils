
package org.monte.media;

import java.util.EventListener;

public interface StateListener
extends EventListener {

  public void stateChanged(StateEvent event);
}
