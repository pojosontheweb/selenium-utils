
package org.monte.media.binary;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;


public class BinaryPanel
        extends JComponent {

    public final static Color[] HIGHLIGHT_COLORS = new Color[]{
        new Color(0xff756c),
        new Color(0xfab555),
        new Color(0xf2df5a),
        new Color(0xbddc5a),
        new Color(0xcb9dde),
        new Color(0x66b1ff),
        new Color(0xb5b5b5),
    };
    private BinaryModel model_;
    private final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static class Highlight {

        public final long from;
        public final long to;
        public final Color color;
        private final String label;

        public Highlight(long from, long to, Color c, String label) {
            this.from = from;
            this.to = to;
            this.color = c;
            this.label = label;
        }

        @Override
        public String toString() {
            return "Highlight[" + label + ", " + from + ".." + to + ", color:" + Integer.toHexString(color.getRGB()) + "]";
        }
    }
    private ArrayList<Highlight> highlights = new ArrayList<Highlight>();

    public BinaryPanel() {
        model_ = new ByteArrayBinaryModel();
        updateUI();
        setToolTipText("Hi");
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d = new Dimension(
                fm.charWidth('0') * 68,
                (int) (fm.getHeight() * (model_.getLength() + 15) / 16));
        return d;
    }

    public void setModel(BinaryModel m) {
        model_ = m;
        revalidate();
        repaint();
    }

    public BinaryModel getModel() {
        return model_;
    }

    public void clearHighlights() {
        highlights.clear();
    }

    public void addHighlight(int from, int to) {
        addHighlight(from, to, HIGHLIGHT_COLORS[2], null);
    }

    public void addHighlight(int from, int to, Color c, String label) {
        highlights.add(new Highlight(from, to, c, label));
    }

    public void addHighlight(Highlight h) {
        highlights.add(h);
    }

    public void addHighlights(List<Highlight> h) {
        if (h != null) {
            highlights.addAll(h);
        }
    }

    public void setHighlights(List<Highlight> h) {
        clearHighlights();
        addHighlights(h);
    }

    
    private int getOffsetAt(int x, int y) {
        FontMetrics fm = getFontMetrics(getFont());
        int row = y / fm.getHeight();
        int column = x / fm.getWidths()['0'];
        int offset;
        if (column < 10) {
            offset = -1;
        } else if (column < 10+8) {
            offset = row * 16 + (column - 10) / 2;
        } else if (column < 10+8+1) {
            offset = -1;
        } else if (column < 10+8+1+8) {
            offset = row * 16 + (column - (10+1)) / 2;
        } else if (column < 10+8+1+8+1) {
            offset = -1;
        } else if (column < 10+8+1+8+1+8) {
            offset = row * 16 + (column - (10+2)) / 2;
        } else if (column < 10+8+1+8+1+8+1) {
            offset = -1;
        } else if (column < 10+8+1+8+1+8+1+8) {
            offset = row * 16 + (column - (10+3)) / 2;
        } else if (column < 48) {
            offset = -1;
        } else if (column < 48+4) {
            offset = row * 16 + (column - 48);
        } else if (column < 48+4+1) {
            offset = -1;
        } else if (column < 48+4+1+4) {
            offset = row * 16 + (column - (48+1));
        } else if (column < 48+4+1+4+1) {
            offset = -1;
        } else if (column < 48+4+1+4+1+4) {
            offset = row * 16 + (column - (48+2));
        } else if (column < 48+4+1+4+1+4+1) {
            offset = -1;
        } else if (column < 48+4+1+4+1+4+1+4) {
            offset = row * 16 + (column - (48+3));
        } else {
            offset = -1;
        }
        return offset;
    }

    private Highlight getHighlightAt(int x, int y) {
        int offset = getOffsetAt(x, y);

        if (offset == -1) {
            return null;
        }
        for (int i = highlights.size() - 1; i >= 0; i--) {
            Highlight hl = highlights.get(i);
            if (hl.from <= offset && hl.to > offset) {
                return hl;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        Rectangle clipRect = g.getClipBounds();
        FontMetrics fm = g.getFontMetrics(getFont());

        g.setColor(getBackground());
        g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
        g.setColor(getForeground());

        int startLine = clipRect.y / fm.getHeight();
        int endLine = Math.min(
                (clipRect.y + clipRect.height) / fm.getHeight() + 1,
                (int) ((model_.getLength() + 15) / 16));
        byte[] bytes = new byte[16];
        char[] chars = new char[69];


        for (; startLine < endLine; startLine++) {
            Arrays.fill(chars, ' ');

            int offset = 0;
            int startOfData = 0;


            int address = startLine * 16;
            for (int i = 0; i < 8; i++) {
                chars[offset++] = HEX[address >>> 28];
                address <<= 4;
            }
            chars[offset++] = '>';
            offset++;
            startOfData = offset;

            int len = model_.getBytes(startLine * 16, 16, bytes);

            for (int i = 0; i < len; i++) {

                chars[offset++] = HEX[(bytes[i] >>> 4) & 0x0f];
                chars[offset++] = HEX[bytes[i] & 0x0f];


                char ch = (char) (bytes[i] & 0xff);
                chars[i + 48 + i / 4] = Character.isISOControl(ch) ? '.' : ch;

                if (i % 4 == 3) {
                    offset++;
                }
            }
            String str = new String(chars);
            for (Highlight hl : highlights) {
                address = startLine * 16;
                if (address + 16 >= hl.from && address <= hl.to) {
                    int dataStart = (int) Math.max(0, hl.from - address);
                    int dataEnd = (int) Math.max(0, Math.min(address + 16, hl.to) - address);
                    if (dataEnd > dataStart) {

                        int from = (int) fm.getStringBounds(str, 0, startOfData + dataStart * 2 + (dataStart / 4), g).getWidth();
                        int length = (int) fm.getStringBounds(str, startOfData + dataStart * 2 + (dataStart / 4), startOfData + dataEnd * 2 + Math.max(0, (dataEnd - 1) / 4), g).getWidth();
                        g.setColor(hl.color);
                        g.fillRect(from + 1, startLine * fm.getHeight(), length - 2, fm.getAscent() + 1);
                        from = (int) fm.getStringBounds(str, 0, startOfData + 38 + dataStart + (dataStart / 4), g).getWidth();
                        length = (int) fm.getStringBounds(str, startOfData + 38 + dataStart + (dataStart / 4), startOfData + 38 + dataEnd + Math.max(0, (dataEnd - 1) / 4), g).getWidth();
                        g.fillRect(from + 1, startLine * fm.getHeight(), length - 2, fm.getAscent() + 1);
                    }
                }
            }
            g.setColor(getForeground());
            g.drawString(
                    str,
                    0,
                    startLine * fm.getHeight() + fm.getAscent());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setBackground(UIManager.getColor("TextArea.background"));
        setForeground(UIManager.getColor("TextArea.foreground"));
        if (UIManager.getLookAndFeel().getID().equals("MacOS")) {
            setFont(
                    new Font("Monospaced", Font.PLAIN, 10));
        } else {
            setFont(
                    new Font("Monospaced", Font.PLAIN, 12));
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Highlight h = getHighlightAt(event.getX(), event.getY());
        return h == null ? null : h.label;
    }
}
