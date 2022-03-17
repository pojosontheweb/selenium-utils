

package org.monte.media.imgseq;

import java.io.File;
import java.util.Comparator;


public class FileComparator implements Comparator<File> {
private OSXCollator collator=new OSXCollator();

    @Override
    public int compare(File o1, File o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
}
