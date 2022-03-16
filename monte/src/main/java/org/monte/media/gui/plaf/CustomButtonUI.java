

package org.monte.media.gui.plaf;

import org.monte.media.gui.border.BackdropBorder;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;


public class CustomButtonUI
        extends BasicButtonUI
        implements PlafConstants {
    private final static CustomButtonUI imageButtonUI = new CustomButtonUI();






    public CustomButtonUI() {
    }


    public static ComponentUI createUI(JComponent c) {
        return new CustomButtonUI();
    }




    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);

    }

    @Override
    public void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);

    }




    @Override
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new ImageButtonListener(b);
    }





    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        ButtonModel m = ((AbstractButton) c).getModel();

                Border b = c.getBorder();
        if (b instanceof BackdropBorder) {
            ((BackdropBorder) b).getBackgroundBorder().paintBorder(c, g, 0, 0, c.getWidth(), c.getHeight());
        }
        super.paint(g, c);
    }
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {




    }

    @Override
    protected void paintFocus(Graphics g, AbstractButton b,
            Rectangle viewRect, Rectangle textRect, Rectangle iconRect){


    }


    @Override
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = g.getFontMetrics();


        if(model.isEnabled()) {

            g.setColor(b.getForeground());
            BasicGraphicsUtils.drawString(g,text, model.getMnemonic(),
                    textRect.x,
                    textRect.y + fm.getAscent());
        } else {


            g.setColor(b.getForeground().brighter());
            BasicGraphicsUtils.drawString(g,text,model.getMnemonic(),
                    textRect.x, textRect.y + fm.getAscent());

        }
    }

}

class ImageButtonListener extends BasicButtonListener {

    public ImageButtonListener(AbstractButton b) {
        super(b);
    }

    @Override
    public void focusGained(FocusEvent e) {
        Component c = (Component)e.getSource();
        c.repaint();
    }
}


