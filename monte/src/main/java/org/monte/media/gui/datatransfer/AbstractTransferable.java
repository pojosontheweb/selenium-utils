

package org.monte.media.gui.datatransfer;

import java.awt.datatransfer.*;
import java.io.*;


public abstract class AbstractTransferable implements Transferable {
    private DataFlavor[] flavors;
    
    
    public AbstractTransferable(DataFlavor[] flavors) {
        this.flavors = flavors;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : flavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
