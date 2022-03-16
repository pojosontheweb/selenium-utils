

package org.monte.media.mpo;

import org.monte.media.jpeg.JFIFInputStream;
import org.monte.media.jpeg.JFIFOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MPOFiles {

    private MPOFiles() {
    }
    
    public static ArrayList<File> splitMPOFile(File f) throws IOException {
        int imgCount = 0;
        ArrayList<File> splittedFiles=new ArrayList<File>();
        JFIFOutputStream out = null;
        byte[] buf = new byte[2048];
        JFIFInputStream in = new JFIFInputStream(f);
        for (JFIFInputStream.Segment seg = in.getNextSegment(); seg != null; seg = in.getNextSegment()) {
            if (seg.marker == JFIFInputStream.SOI_MARKER) {
                String ext;
                switch (imgCount++) {
                    case 0:
                        ext = "_l.JPG";
                        break;
                    case 1:
                        ext = "_r.JPG";
                        break;
                    default:
                        ext = "_" + imgCount + ".JPG";
                        break;
                }
                String name = f.getName();
                int p = name.lastIndexOf('.');
                if (p == -1) {
                    p = name.length();
                }
                File imgFile = new File(f.getParentFile(), name.substring(0, p) + ext);
                splittedFiles.add(imgFile);
                out = new JFIFOutputStream(imgFile);

                out.pushSegment(seg.marker);
                out.popSegment();
            } else if (out != null) {

                if (seg.marker == JFIFInputStream.APP2_MARKER) {

                    int len = 4, off = 0, n = 0;
                    while (n < len) {
                        int count = in.read(buf, off + n, len - n);
                        if (count < 0) {
                            break;
                        }
                        n += count;
                    }
                    if (n == 4 && (buf[0] & 0xff) == 'M' && (buf[1] & 0xff) == 'P' && (buf[2] & 0xff) == 'F' && buf[3] == 0) {
                        continue;
                    } else {
                        out.pushSegment(seg.marker);
                        out.write(buf, 0, n);
                    }
                } else {
                    out.pushSegment(seg.marker);
                }

                for (int len = in.read(buf, 0, buf.length); len != -1; len = in.read(buf, 0, buf.length)) {
                    out.write(buf, 0, len);
                }
                out.popSegment();
            }
        }
        return splittedFiles;
    }


}
