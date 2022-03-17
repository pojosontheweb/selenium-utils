

package org.monte.media.gui.datatransfer;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

public class CompositeTransferable implements Transferable {
    private HashMap<DataFlavor,Transferable> transferables = new HashMap<DataFlavor,Transferable>();
    private LinkedList<DataFlavor> flavors = new LinkedList<DataFlavor>();


    public CompositeTransferable() {
    }

    public void add(Transferable t) {
        DataFlavor[] f = t.getTransferDataFlavors();
        for (int i=0; i < f.length; i++) {
            if (! transferables.containsKey(f[i])) {
                flavors.add(f[i]);
            }
            transferables.put(f[i], t);

        }
    }


    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Transferable t = (Transferable) transferables.get(flavor);
        if (t == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return t.getTransferData(flavor);
    }


    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) flavors.toArray(new DataFlavor[transferables.size()]);
    }


    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferables.containsKey(flavor);
    }
}
