

package org.monte.media.gui.border;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class BackdropBorder implements Border {
    private Border foregroundBorder;
    private Border backgroundBorder;
    
    
    public BackdropBorder(Border backdropBorder) {
        this(null, backdropBorder);
    }
    public BackdropBorder(Border foregroundBorder, Border backdropBorder) {
        this.backgroundBorder = backdropBorder;
        this.foregroundBorder = foregroundBorder;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (foregroundBorder != null) {
            foregroundBorder.paintBorder(c, g, x, y, width, height);
        }
    }
    
    public Border getBackgroundBorder() {
        return backgroundBorder;
    }

    public Insets getBorderInsets(Component c) {
        if (foregroundBorder != null) {
            return foregroundBorder.getBorderInsets(c);
        } else {
            return backgroundBorder.getBorderInsets(c);
        }
    }

    public boolean isBorderOpaque() {
        return false;
    }    
}
