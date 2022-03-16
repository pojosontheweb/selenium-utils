

package org.monte.media.gui.datatransfer;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class StringTransferable extends AbstractTransferable {
    private String string;
    
    public StringTransferable(String string) {
        this(getDefaultFlavors(), string);
    }
    public StringTransferable(DataFlavor flavor, String string) {
        this(new DataFlavor[] { flavor }, string);
    }
    public StringTransferable(DataFlavor[] flavors, String string) {
        super(flavors);
        this.string = string;
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (! isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return string;
    }
    
    protected static DataFlavor[] getDefaultFlavors() {
        try {
            return new DataFlavor[] {
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class=java.lang.String"),
                DataFlavor.stringFlavor
            };
        } catch (ClassNotFoundException cle) {
            InternalError ie = new InternalError(
                    "error initializing StringTransferable");
            ie.initCause(cle);
            throw ie;
        }
    }
}
