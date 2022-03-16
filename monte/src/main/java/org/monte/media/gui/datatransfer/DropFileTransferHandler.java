
package org.monte.media.gui.datatransfer;

import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.InputContext;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;


public class DropFileTransferHandler extends TransferHandler {

    private boolean shouldRemove;
    private int p0;
    private int p1;
    private int fileSelectionMode;
    private FileFilter fileFilter;
    private ActionListener actionListener;


    public DropFileTransferHandler() {
        this(JFileChooser.FILES_ONLY);
    }


    public DropFileTransferHandler(int fileSelectionMode) {
        this(fileSelectionMode, null);
    }


    public DropFileTransferHandler(int fileSelectionMode, FileFilter filter) {
        this(fileSelectionMode,filter,null);
    }

    public DropFileTransferHandler(int fileSelectionMode, FileFilter filter,ActionListener l) {
        this.fileFilter = filter;
        if (fileSelectionMode != JFileChooser.FILES_AND_DIRECTORIES
                && fileSelectionMode != JFileChooser.FILES_ONLY
                && fileSelectionMode != JFileChooser.DIRECTORIES_ONLY) {
            throw new IllegalArgumentException("illegal file selection mode:" + fileSelectionMode);
        }
        this.fileSelectionMode = fileSelectionMode;
        setActionListener(l);
    }

    public void setActionListener(ActionListener l) {
        this.actionListener=l;
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {

        boolean imported = false;
        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            InputContext ic = c.getInputContext();
            if (ic != null) {
                ic.endComposition();
            }

            try {
                List list = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                if (list.size() > 0) {
                    File file = (File) list.get(0);

                    switch (fileSelectionMode) {
                        case JFileChooser.FILES_AND_DIRECTORIES:
                            break;
                        case JFileChooser.FILES_ONLY:
                            if (file.isDirectory()) {
                                return false;
                            }
                            break;
                        case JFileChooser.DIRECTORIES_ONLY:
                            if (!file.isDirectory()) {
                                return false;
                            }
                            break;
                    }
                    if (fileFilter != null && !fileFilter.accept(file)) {
                        return false;
                    }
                    actionListener.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,file.getPath()));
                }
                imported = true;
            } catch (UnsupportedFlavorException ex) {

            } catch (IOException ex) {

            }
        }

        return imported;
    }

    @Override
    protected Transferable createTransferable(JComponent comp) {
        return null;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (!(comp.isEnabled())) {
            return false;
        }

        for (DataFlavor flavor : transferFlavors) {
            if (flavor.isFlavorJavaFileListType()
                    || flavor.isFlavorTextType()) {
                return true;
            }
        }

        return false;
    }


    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JComponent c) {
        DataFlavor plainFlavor = null;
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;

        for (int i = 0; i < flavors.length; i++) {
            String mime = flavors[i].getMimeType();
            if (mime.startsWith("text/plain")) {
                return flavors[i];
            } else if (refFlavor == null && mime.startsWith("application/x-java-jvm-local-objectref") && flavors[i].getRepresentationClass() == String.class) {
                refFlavor = flavors[i];
            } else if (stringFlavor == null && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
        }
        if (refFlavor != null) {
            return refFlavor;
        } else if (stringFlavor != null) {
            return stringFlavor;
        }
        return null;
    }



    @Override
    public int getSourceActions(JComponent comp) {
            return NONE;

    }


    @Override
    protected void exportDone(JComponent comp, Transferable data, int action) {

    }


    public FileFilter getFileFilter() {
        return fileFilter;
    }


    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}

