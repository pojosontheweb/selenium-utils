

package org.monte.media.gui.border;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ButtonStateBorder implements Border {
    public final static int E = 0;
    public final static int EP = 1;
    public final static int ES = 2;
    public final static int EPS = 3;
    public final static int D = 4;
    public final static int DS = 5;
    public final static int I = 6;
    public final static int IS = 7;
    public final static int DI = 8;
    public final static int DIS = 9;
    public final static int DEFAULT = 10;
    
    private Border[] borders;

    
    private Image tiledImage;
    
    private int tileCount;
    
    private boolean isTiledHorizontaly;

    private Insets borderInsets;
    
    private boolean fill;
    
    private Insets imageInsets;


    
    public ButtonStateBorder(Border e, Border es) {
        borders = new Border[DEFAULT+1];
        borders[E] = e;
        borders[EP] = es;
        borders[ES] = es;
        borders[EPS] = es;
        borders[D] = e;
        borders[DS] = es;
        borders[I] = e;
        borders[IS] = es;
        borders[DI] = es;
        borders[DIS] = es;
    }
    
    public ButtonStateBorder(Border e, Border ep, Border es, Border eps,
    Border d, Border ds, Border i, Border is, Border di, Border dis) {
        borders = new Border[DEFAULT+1];
        borders[E] = e;
        borders[EP] = ep;
        borders[ES] = es;
        borders[EPS] = eps;
        borders[D] = d;
        borders[DS] = ds;
        borders[I] = i;
        borders[IS] = is;
        borders[DI] = dis;
        borders[DIS] = dis;
    }
    
    public ButtonStateBorder(Border[] borders) {
        this.borders = new Border[DEFAULT+1];
        System.arraycopy(borders, 0, this.borders, 0, Math.min(borders.length, this.borders.length));
    }

    
    public ButtonStateBorder(Image tiledImage, int tileCount, boolean isTiledHorizontaly,
    Insets imageInsets, Insets borderInsets, boolean fill) {
        this.tiledImage = tiledImage;
        this.tileCount = tileCount;
        this.isTiledHorizontaly = isTiledHorizontaly;
        this.imageInsets = imageInsets;
        this.borderInsets = borderInsets;
        this.fill = fill;
    }

    public void setBorder(int key, Border b) {
        borders[key] = b;
    }


    public Insets getBorderInsets(Component c) {
        if (borderInsets != null) {
            return (Insets) borderInsets.clone();
        } else {
            return (Insets) borders[0].getBorderInsets(c).clone();
        }
    }

    public boolean isBorderOpaque() {
        return borders[0].isBorderOpaque();
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Border border = getBorder(c);
        if (border != null) {
            border.paintBorder(c, g, x, y, width, height);
        }
    }

    protected Border getBorder(Component c) {
        Border border;
        boolean isActive = true;

        if (c instanceof AbstractButton) {
            ButtonModel model = ((AbstractButton) c).getModel();
            if (isActive) {
                if (model.isEnabled()) {
                    if (model.isPressed() && model.isArmed()) {
                        if (model.isSelected()) {
                            border = borders[EPS];
                        } else {
                            border = borders[EP];
                        }
                    } else if (model.isSelected()) {
                        border = borders[ES];
                    } else {
                        if (!model.isPressed() &&
                        borders[DEFAULT] != null &&
                        (c instanceof JButton) &&
                        ((JButton) c).isDefaultButton()
                        ) {
                            border = borders[DEFAULT];
                        } else {
                            border = borders[E];
                        }
                    }
                } else {
                    if (model.isSelected()) {
                        border = borders[DS];
                    } else {
                        border = borders[D];
                    }
                }
            } else {
                if (model.isEnabled()) {
                    if (model.isSelected()) {
                        border = borders[IS];
                    } else {
                        border = borders[I];
                    }
                } else {
                    if (model.isSelected()) {
                        border = borders[DIS];
                    } else {
                        border = borders[DI];
                    }
                }
            }
        } else {
            if (isActive) {
                if (c.isEnabled()) {
                    border = borders[E];
                } else {
                    border = borders[D];
                }
            } else {
                if (c.isEnabled()) {
                    border = borders[I];
                } else {
                    border = borders[DI];
                }
            }
        }
        return border;
    }
}
