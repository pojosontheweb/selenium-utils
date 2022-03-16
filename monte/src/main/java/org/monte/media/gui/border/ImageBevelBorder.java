

package org.monte.media.gui.border;

import org.monte.media.image.Images;
import java.awt.*;
import javax.swing.border.*;
import java.awt.image.*;


public class ImageBevelBorder implements Border {
    private final static boolean VERBOSE = false;

    private Image image;


    private Insets borderInsets;

    private Insets imageInsets;


    private boolean fillContentArea;


    public ImageBevelBorder(Image img, Insets borderInsets) {
        this(img, borderInsets, borderInsets, true);
    }


    public ImageBevelBorder(Image img, Insets imageInsets, Insets borderInsets) {
        this(img, imageInsets, borderInsets, true);
    }

    public ImageBevelBorder(Image img, Insets imageInsets, Insets borderInsets, boolean fillContentArea) {
        this.image = img;
        this.imageInsets = imageInsets;
        this.borderInsets = borderInsets;
        this.fillContentArea = fillContentArea;
    }


    public boolean isBorderOpaque() {
        return false;
    }


    public Insets getBorderInsets(Component c) {
        return (Insets) borderInsets.clone();
    }



    public void paintBorder(Component c, Graphics gr, int x, int y, int width, int height) {
        if (image == null) return;


        image = Images.toBufferedImage(image);
        BufferedImage bufImg = (BufferedImage) image;

        if (! gr.getClipBounds().intersects(x, y, width, height)) {
            return;
        }





        Graphics2D g = (Graphics2D) gr.create();


        int top = imageInsets.top;
        int left = imageInsets.left;
        int bottom = imageInsets.bottom;
        int right = imageInsets.right;
        int imgWidth = bufImg.getWidth();
        int imgHeight = bufImg.getHeight();



        if (fillContentArea) {
            if (width == imgWidth && height == imgHeight) {
                g.drawImage(image, x, y, c);
                g.dispose();
                return;
            }
        }


        if (width == imgWidth) {
            left = imgWidth;
            right = 0;
        }
        if (height == imgHeight) {
            top = imgHeight;
            bottom = 0;
        }


        if (width < left + right) {
            left = Math.min(left, width / 2);
            right = width - left;
        }
        if (height < top + bottom) {
            top = Math.min(top, height / 2);
            bottom = height - top;
        }


        if (top > 0 && left > 0) {
            g.drawImage(
            image,
            x, y, x + left, y + top,
            0, 0, left, top,
            c
            );
        }
        if (top > 0 && right > 0) {

            g.drawImage(
            image,
            x + width - right, y, x + width, y + top,
            imgWidth - right, 0, imgWidth, top,
            c
            );
        }
        if (bottom > 0 && left > 0) {
            g.drawImage(
            image,
            x, y + height - bottom, x + left, y + height,
            0, imgHeight - bottom, left, imgHeight,
            c
            );
        }
        if (bottom > 0 && right > 0) {
            g.drawImage(
            image,
            x + width - right, y + height - bottom, x + width, y + height,
            imgWidth - right, imgHeight - bottom, imgWidth, imgHeight,
            c
            );
        }


        BufferedImage subImg = null;
        TexturePaint paint;


        if (top > 0 && left + right < width) {
            if (imgWidth > right + left) {
            subImg = bufImg.getSubimage(left, 0, imgWidth - right - left, top);
            paint = new TexturePaint(subImg, new Rectangle(x+left, y, imgWidth - left - right, top));
            g.setPaint(paint);
            g.fillRect(x+left, y, width - left - right, top);
            }
        }

        if (bottom > 0 && left + right < width) {
            if (imgHeight > bottom && imgWidth > right + left) {
            subImg = bufImg.getSubimage(left, imgHeight - bottom, imgWidth - right - left, bottom);
            paint = new TexturePaint(subImg, new Rectangle(x+left, y + height - bottom, imgWidth - left - right, bottom));
            g.setPaint(paint);
            g.fillRect(x+left, y + height - bottom, width - left - right, bottom);
            }
        }

        if (left > 0 && top + bottom < height) {
            if (imgHeight > top + bottom) {
            subImg = bufImg.getSubimage(0, top, left, imgHeight - top - bottom);
            paint = new TexturePaint(subImg, new Rectangle(x, y+top, left, imgHeight - top - bottom));
            g.setPaint(paint);
            g.fillRect(x, y+top, left, height - top - bottom);
            }
        }

        if (right > 0 && top + bottom < height) {
            if (imgWidth > right + right && imgHeight > top + bottom) {
            subImg = bufImg.getSubimage(imgWidth - right, top, right, imgHeight - top - bottom);
            paint = new TexturePaint(subImg, new Rectangle(x+width-right, y + top, right, imgHeight - top - bottom));
            g.setPaint(paint);
            g.fillRect(x+width-right, y + top, right, height - top - bottom);
            }
        }


        if (fillContentArea) {
            if (left + right < width && top + bottom < height) {
                subImg = bufImg.getSubimage(left, top, imgWidth - right - left, imgHeight - top - bottom);
                paint = new TexturePaint(subImg, new Rectangle(x + left, y + top, imgWidth - right - left, imgHeight - top - bottom));
                g.setPaint(paint);
                g.fillRect(x+left, y + top, width - right - left, height - top - bottom);
            }
        }

        g.dispose();
    }

    public static class UIResource extends ImageBevelBorder implements javax.swing.plaf.UIResource {
        public UIResource(Image img, Insets borderInsets) {
            super(img, borderInsets);
        }
        public UIResource(Image img, Insets imageInsets, Insets borderInsets) {
            super(img, imageInsets, borderInsets);
        }
        public UIResource(Image img, Insets imageInsets, Insets borderInsets, boolean fillContentArea) {
            super(img, imageInsets, borderInsets, fillContentArea);
        }
    }
}
