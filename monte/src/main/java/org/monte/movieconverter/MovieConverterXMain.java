
package org.monte.movieconverter;

import org.monte.media.DefaultMovie;
import org.monte.media.Movie;
import org.monte.media.MovieReader;
import org.monte.media.Registry;
import org.monte.media.gui.Worker;
import org.monte.media.gui.datatransfer.DropFileTransferHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;


public class MovieConverterXMain extends javax.swing.JFrame {

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            File f=new File(e.getActionCommand());
            if (isEnabled())
            setMovieFile(f);
        }

    }
    private Handler handler=new Handler();


    public MovieConverterXMain() {
        initComponents();
        DropFileTransferHandler dfth=new DropFileTransferHandler(JFileChooser.FILES_ONLY);
        dfth.setActionListener(handler);
        setTransferHandler(dfth);
        movieConverterPanel.setTransferHandler(dfth);
    }

    public void setMovieFile(final File newFile) {
        setEnabled(false);
        setTitle(null);
        getRootPane().putClientProperty("Window.documentFile", null);
        new Worker<Movie>() {

            @Override
            protected Movie construct() throws Exception {

                MovieReader r=Registry.getInstance().getReader(newFile);
                if (r==null)throw new IOException("no reader");
                DefaultMovie m=new DefaultMovie();
                m.setReader(r);
                return m;
            }

            @Override
            protected void done(Movie movie) {
        getRootPane().putClientProperty("Window.documentFile", newFile);
        setTitle(newFile.getName());
        movieConverterPanel.setMovie(movie);
            }

            @Override
            protected void finished() {
              setEnabled(true);
            }


        }.start();
    }


    @SuppressWarnings("unchecked")

    private void initComponents() {

        movieConverterPanel = new MovieConverterPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(movieConverterPanel, java.awt.BorderLayout.CENTER);

        pack();
    }


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MovieConverterXMain().setVisible(true);
            }
        });
    }

    private MovieConverterPanel movieConverterPanel;

}
