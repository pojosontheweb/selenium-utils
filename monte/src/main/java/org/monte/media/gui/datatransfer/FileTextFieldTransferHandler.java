
package org.monte.media.gui.datatransfer;

import java.awt.datatransfer.*;
import java.awt.im.InputContext;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;


public class FileTextFieldTransferHandler extends TransferHandler {

    private boolean shouldRemove;
    private JTextComponent exportComp;
    private int p0;
    private int p1;
    private int fileSelectionMode;
    private FileFilter fileFilter;

    
    public FileTextFieldTransferHandler() {
        this(JFileChooser.FILES_ONLY);
    }

    
    public FileTextFieldTransferHandler(int fileSelectionMode) {
        this(fileSelectionMode, null);
    }

    
    public FileTextFieldTransferHandler(int fileSelectionMode, FileFilter filter) {
        this.fileFilter = filter;
        if (fileSelectionMode != JFileChooser.FILES_AND_DIRECTORIES
                && fileSelectionMode != JFileChooser.FILES_ONLY
                && fileSelectionMode != JFileChooser.DIRECTORIES_ONLY) {
            throw new IllegalArgumentException("illegal file selection mode:" + fileSelectionMode);
        }
        this.fileSelectionMode = fileSelectionMode;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        JTextComponent c = (JTextComponent) comp;





        if (c == exportComp && c.getCaretPosition() >= p0 && c.getCaretPosition() <= p1) {
            shouldRemove = false;
            return true;
        }

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
                    c.setText(file.getPath());
                }
                imported = true;
            } catch (UnsupportedFlavorException ex) {

            } catch (IOException ex) {

            }
        }

        if (!imported) {
            DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors(), c);
            if (importFlavor != null) {
                InputContext ic = c.getInputContext();
                if (ic != null) {
                    ic.endComposition();
                }
                try {
                    String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                    Reader r = importFlavor.getReaderForText(t);
                    boolean useRead = false;
                    handleReaderImport(r, c, useRead);
                    imported = true;
                } catch (UnsupportedFlavorException ex) {

                } catch (BadLocationException ex) {

                } catch (IOException ex) {

                }
            }
        }
        return imported;
    }

    @Override
    protected Transferable createTransferable(JComponent comp) {

        CompositeTransferable t;

        JTextComponent c = (JTextComponent) comp;
        shouldRemove = true;
        p0 = c.getSelectionStart();
        p1 = c.getSelectionEnd();
        if (p0 != p1) {
            t = new CompositeTransferable();

            String text = c.getSelectedText();




            t.add(new StringTransferable(text));
            t.add(new PlainTextTransferable(text));
        } else {
            t = null;
        }


        return t;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        JTextComponent c = (JTextComponent) comp;
        if (!(c.isEditable() && c.isEnabled())) {
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

    
    protected DataFlavor getImportFlavor(DataFlavor[] flavors, JTextComponent c) {
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

    
    protected void handleReaderImport(Reader in, JTextComponent c, boolean useRead)
            throws BadLocationException, IOException {
        if (useRead) {
            int startPosition = c.getSelectionStart();
            int endPosition = c.getSelectionEnd();
            int length = endPosition - startPosition;
            EditorKit kit = c.getUI().getEditorKit(c);
            Document doc = c.getDocument();
            if (length > 0) {
                doc.remove(startPosition, length);
            }
            kit.read(in, doc, startPosition);
        } else {
            char[] buff = new char[1024];
            int nch;
            boolean lastWasCR = false;
            int last;
            StringBuffer sbuff = null;



            while ((nch = in.read(buff, 0, buff.length)) != -1) {
                if (sbuff == null) {
                    sbuff = new StringBuffer(nch);
                }
                last = 0;
                for (int counter = 0; counter < nch; counter++) {
                    switch (buff[counter]) {
                        case '\r':
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                            } else {
                                lastWasCR = true;
                            }
                            break;
                        case '\n':
                            if (lastWasCR) {
                                if (counter > (last + 1)) {
                                    sbuff.append(buff, last, counter - last - 1);
                                }


                                lastWasCR = false;
                                last = counter;
                            }
                            break;
                        default:
                            if (lastWasCR) {
                                if (counter == 0) {
                                    sbuff.append('\n');
                                } else {
                                    buff[counter - 1] = '\n';
                                }
                                lastWasCR = false;
                            }
                            break;
                    }
                }
                if (last < nch) {
                    if (lastWasCR) {
                        if (last < (nch - 1)) {
                            sbuff.append(buff, last, nch - last - 1);
                        }
                    } else {
                        sbuff.append(buff, last, nch - last);
                    }
                }
            }
            if (lastWasCR) {
                sbuff.append('\n');
            }
            System.out.println("FileTextTransferHandler " + c.getSelectionStart() + ".." + c.getSelectionEnd());
            c.replaceSelection(sbuff != null ? sbuff.toString() : "");
        }
    }


    
    @Override
    public int getSourceActions(JComponent comp) {
        JTextComponent c = (JTextComponent) comp;

        if (c instanceof JPasswordField
                && c.getClientProperty("JPasswordField.cutCopyAllowed")
                != Boolean.TRUE) {
            return NONE;
        }

        return c.isEditable() ? COPY_OR_MOVE : COPY;
    }

    
    @Override
    protected void exportDone(JComponent comp, Transferable data, int action) {
        JTextComponent c = (JTextComponent) comp;



        if (shouldRemove && action == MOVE) {
            try {
                Document doc = c.getDocument();
                doc.remove(p0, p1 - p0);
            } catch (BadLocationException e) {
            }
        }
        exportComp = null;
    }

    
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    
    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}

