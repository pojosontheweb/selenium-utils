
package org.monte.media.mpo;

import org.monte.media.gui.Worker;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class MPOImageSplitter extends javax.swing.JPanel {

    private class Handler implements DropTargetListener {

        
        @Override
        public void dragEnter(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        
        @Override
        public void dragExit(DropTargetEvent event) {

        }

        
        @Override
        public void dragOver(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        
        @Override
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_COPY);

                try {
                    List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    splitMPOFiles(files);

                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(
                            MPOImageSplitter.this,
                            "Could not access the dropped data.",
                            "MPOImageSplitter: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                } catch (UnsupportedFlavorException e) {
                    JOptionPane.showConfirmDialog(
                            MPOImageSplitter.this,
                            "Unsupported data flavor.",
                            "MPOImageSplitter: Drop Failed",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                event.rejectDrop();
            }
        }

        
        @Override
        public void dropActionChanged(DropTargetDragEvent event) {

        }
    }
    private Handler handler = new Handler();

    
    public MPOImageSplitter() {
        initComponents();
        new DropTarget(this, handler);
        new DropTarget(label, handler);
    }

    public void splitMPOFiles(final List<File> files) {
        label.setEnabled(false);
        new Worker() {

            @Override
            protected Object construct() throws Exception {
                for (File f : files) {
                    MPOFiles.splitMPOFile(f);
                }
                return null;
            }

            @Override
            protected void finished() {
                label.setEnabled(true);
            }
        }.start();
    }

    
    @SuppressWarnings("unchecked")

    private void initComponents() {

        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("Drop MPO file here.");
        add(label, java.awt.BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame f = new JFrame("MPO Image Splitter");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(new MPOImageSplitter());
                f.setSize(200, 200);
                f.setVisible(true);
            }
        });
    }

    private javax.swing.JLabel label;

}
