
package org.monte.media.gui;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.monte.media.image.Images;


public class JMovieSliderAqua extends JComponent
        implements ChangeListener, MouseListener, MouseMotionListener {

    private final static int THUMB_WIDTH = 15, THUMB_HEIGHT = 16;
    private final static int HALF_THUMB_WIDTH = THUMB_WIDTH / 2;
    private BoundedRangeModel model_;
    protected int thumbPos_ = 0;
    private int progressPos_ = 0;
    private BoundedRangeModel progressModel_;
    private boolean isPressed, isArmed;
    private static Image trackEastImage, trackCenterImage, trackWestImage;
    private static Image trackEastUnloadedImage, trackCenterUnloadedImage, trackWestUnloadedImage;
    private static Image trackEastDisabledImage, trackCenterDisabledImage, trackWestDisabledImage;
    private static Image trackEastUnloadedDisabledImage, trackCenterUnloadedDisabledImage, trackWestUnloadedDisabledImage;
    private static Image thumbImage, thumbPressedImage, thumbDisabledImage;

    public JMovieSliderAqua() {
        model_ = new DefaultBoundedRangeModel();
        model_.addChangeListener(this);
        progressModel_ = new DefaultBoundedRangeModel();
        progressModel_.setValue(progressModel_.getMaximum());
        progressModel_.addChangeListener(this);
        setBackground(Color.lightGray);
        addMouseListener(this);
        addMouseMotionListener(this);

        if (trackEastImage == null) {
            Class c = getClass();

            trackEastImage = Images.createImage(c, "images/Player.trackEast.png");
            trackCenterImage = Images.createImage(c, "images/Player.trackCenter.png");
            trackWestImage = Images.createImage(c, "images/Player.trackWest.png");

            trackEastUnloadedImage = Images.createImage(c, "images/Player.trackUnloadedEast.png");
            trackCenterUnloadedImage = Images.createImage(c, "images/Player.trackUnloadedCenter.png");
            trackWestUnloadedImage = Images.createImage(c, "images/Player.trackUnloadedWest.png");

            trackEastDisabledImage = Images.createImage(c, "images/Player.trackEastD.png");
            trackCenterDisabledImage = Images.createImage(c, "images/Player.trackCenterD.png");
            trackWestDisabledImage = Images.createImage(c, "images/Player.trackWestD.png");

            trackEastUnloadedDisabledImage = Images.createImage(c, "images/Player.trackUnloadedEastD.png");
            trackCenterUnloadedDisabledImage = Images.createImage(c, "images/Player.trackUnloadedCenterD.png");
            trackWestUnloadedDisabledImage = Images.createImage(c, "images/Player.trackUnloadedWestD.png");

            thumbImage = Images.createImage(c, "images/Player.thumb.png");
            thumbPressedImage = Images.createImage(c, "images/Player.thumbP.png");
            thumbDisabledImage = Images.createImage(c, "images/Player.thumbD.png");

            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(trackEastImage, 0);
            tracker.addImage(trackCenterImage, 0);
            tracker.addImage(trackWestImage, 0);
            tracker.addImage(trackEastUnloadedImage, 0);
            tracker.addImage(trackCenterUnloadedImage, 0);
            tracker.addImage(trackWestUnloadedImage, 0);
            tracker.addImage(thumbImage, 0);
            tracker.addImage(thumbPressedImage, 0);
            tracker.checkAll(true);
        }

    }

    public void setModel(BoundedRangeModel m) {
        if (model_ != null) {
            model_.removeChangeListener(this);
        }
        model_ = m == null ? new DefaultBoundedRangeModel() : m;
        if (model_ != null) {
            model_.addChangeListener(this);
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 16);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(16, 16);
    }


    protected void moveThumb(int mousePosition) {
        int width = getSize().width;
        float mouseValue = (mousePosition - HALF_THUMB_WIDTH) / (float) (width - THUMB_WIDTH);
        model_.setValue((int) (mouseValue * (model_.getMaximum() - model_.getMinimum())));
    }

    protected int computeThumbPos() {
        BoundedRangeModel m = model_;
        if (m == null) {
            return 0;
        }
        int width = getSize().width - THUMB_WIDTH;

        float thumbPos = Math.max(0f, m.getValue() / (float) ((m.getMaximum() - m.getMinimum())));
        return (int) (width * thumbPos);
    }

    public synchronized void setProgressModel(BoundedRangeModel brm) {
        if (progressModel_ != null) {
            progressModel_.removeChangeListener(this);
        }
        progressModel_ = (brm == null) ? new DefaultBoundedRangeModel() : brm;
        if (progressModel_ != null) {
            progressModel_.addChangeListener(this);
        }
    }

    public BoundedRangeModel getProgressModel() {
        return progressModel_;
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        thumbPos_ = computeThumbPos();
        progressPos_ = computeProgressPos();
        paintTrack(g, progressPos_);
        paintThumb(g, thumbPos_);
    }

    private void paintTrack(Graphics g, int progressPos) {
        Dimension s = getSize();
        int width = s.width;
        boolean enabled = isEnabled();


        Image image;
        if (progressModel_.getValue() == progressModel_.getMinimum()) {
            image = (enabled) ? trackWestUnloadedImage : trackWestUnloadedDisabledImage;
        } else {
            image = (enabled) ? trackWestImage : trackWestDisabledImage;
        }
        g.drawImage(image, 0, 0, this);


        int trackWidth = width - trackEastImage.getWidth(this) - trackWestImage.getWidth(this);
        int imageWidth = Math.min(
                trackWidth,
                progressPos - trackWestImage.getWidth(this));
        if (imageWidth > 0) {
            image = (enabled) ? trackCenterImage : trackCenterDisabledImage;
            g.drawImage(image, trackWestImage.getWidth(this), 0,
                    imageWidth,
                    image.getHeight(this),
                    this);
        }
        imageWidth = trackWidth - imageWidth;
        if (imageWidth > 0) {
            image = (enabled) ? trackCenterUnloadedDisabledImage : trackCenterUnloadedDisabledImage;
            g.drawImage(image, width - trackEastImage.getWidth(this) - imageWidth, 0,
                    imageWidth,
                    image.getHeight(this),
                    this);
        }


        if (progressModel_.getValue() < progressModel_.getMaximum() - progressModel_.getExtent()) {
            image = (enabled) ? trackEastUnloadedImage : trackEastUnloadedDisabledImage;
        } else {
            image = (enabled) ? trackEastImage : trackEastDisabledImage;
        }
        g.drawImage(image, width - image.getWidth(this), 0, this);
    }

    public void paintThumb(Graphics g, int thumbPos) {
        Dimension s = getSize();
        int width = s.width;
        int height = s.height;

        Image image;
        if (!isEnabled()) {
            image = thumbDisabledImage;
        } else if (isPressed && isArmed) {
            image = thumbPressedImage;
        } else {
            image = thumbImage;
        }


        g.drawImage(image, thumbPos, 0, this);
    }

    protected int computeProgressPos() {
        BoundedRangeModel m = progressModel_;
        if (m == null) {
            return 0;
        }
        int trackWidth = getSize().width - trackEastImage.getWidth(this) - trackWestImage.getWidth(this);
        float thumbPos = Math.max(0f, m.getValue() / (float) ((m.getMaximum() - m.getMinimum())));
        return (int) (trackWidth * thumbPos) + trackWestImage.getWidth(this);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        BoundedRangeModel progress = progressModel_;
        if (progress != null && event.getSource() == progress) {

            if (computeProgressPos() != progressPos_) {
                repaint();
            }
        } else {

            if (computeThumbPos() != thumbPos_) {
                repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        isArmed = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        isArmed = false;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        isPressed = true;
        moveThumb(mouseEvent.getX());
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        isPressed = false;
        moveThumb(mouseEvent.getX());
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        moveThumb(mouseEvent.getX());
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
    }
}
