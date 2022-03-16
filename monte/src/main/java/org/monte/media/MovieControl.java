

package org.monte.media;

import java.awt.Component;


public interface MovieControl {
    public void setPlayer(Player player);
    public void setVisible(boolean newValue);
    public Component getComponent();
    public void setEnabled(boolean b);
}
