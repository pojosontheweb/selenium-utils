
package org.monte.movieconverter;

import org.monte.media.Buffer;
import org.monte.media.Movie;
import org.monte.media.gui.Worker;
import org.monte.media.math.Rational;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.TransferHandler;



public class MovieConverterPanel extends javax.swing.JPanel {

    private ExecutorService executor;
private Buffer imageBuffer=new Buffer();
    private class Handler implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == Movie.PLAYHEAD_PROPERTY) {
                updateImage();
            }
        }
    }
    private Handler handler = new Handler();
    private long imageTime = -1;


    public MovieConverterPanel() {
        initComponents();

    }

    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        movieControlPanel.setTransferHandler(newHandler);
        jPanel1.setTransferHandler(newHandler);
        toolBar.setTransferHandler(newHandler);
        toolBar.putClientProperty("Quaqua.ToolBar.style", "bottom");
    }

    private void updateImage() {
        final Movie movie = getMovie();
        if (movie == null) {
            return;
        }

        execute(new Worker<BufferedImage>() {

            @Override
            protected BufferedImage construct() throws Exception {
                Rational time=movie.getInsertionPoint();


                return null;
            }

            @Override
            protected void done(BufferedImage value) {
                imagePanel.setImage(value);
            }

        });
    }

    public void execute(Runnable worker) {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(worker);
    }


    @SuppressWarnings("unchecked")

    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        movieControlPanel = new org.monte.media.gui.JMovieControlPanel();
        imagePanel = new org.monte.media.gui.ImagePanel();

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        jLabel1.setText(" ");
        toolBar.add(jLabel1);

        add(toolBar, java.awt.BorderLayout.PAGE_END);

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(movieControlPanel, java.awt.BorderLayout.SOUTH);
        jPanel1.add(imagePanel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }

    private org.monte.media.gui.ImagePanel imagePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private org.monte.media.gui.JMovieControlPanel movieControlPanel;
    private javax.swing.JToolBar toolBar;


    public void setMovie(Movie movie) {
        Movie oldValue = movieControlPanel.getMovie();
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(handler);
        }

        movieControlPanel.setMovie(movie);
        if (movie != null) {
            movie.addPropertyChangeListener(handler);
        }
        imageTime = -1;

        updateImage();
    }

    private Movie getMovie() {
        return movieControlPanel.getMovie();
    }
}
