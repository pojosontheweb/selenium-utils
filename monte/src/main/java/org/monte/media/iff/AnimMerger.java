

package org.monte.media.iff;

import org.monte.media.AbortException;
import org.monte.media.ParseException;
import org.monte.media.gui.Worker;
import java.text.NumberFormat;
import javax.swing.tree.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class AnimMerger extends JFrame {
    private File file1, file2;
    private JFileChooser chooser;
    private static NumberFormat numFormat = NumberFormat.getInstance();


    public AnimMerger() {
        initComponents();
    }

    public final static int CMAP_ID = IFFParser.stringToID("CMAP");
    public final static int ILBM_ID = IFFParser.stringToID("ILBM");
    public final static int ANHD_ID = IFFParser.stringToID("ANHD");

    private final static int[] sequence = {
        IFFParser.stringToID("ANIM"),
        IFFParser.stringToID("8SVX"),
        IFFParser.stringToID("ILBM"),
        IFFParser.stringToID("VHDR"),
        IFFParser.stringToID("ANNO"),
        IFFParser.stringToID("CAMG"),
        IFFParser.stringToID("BMHD"),
        IFFParser.stringToID("ANHD"),
        IFFParser.stringToID("CHAN"),
        IFFParser.stringToID("SCTL"),
        IFFParser.stringToID("CMAP"),
        IFFParser.stringToID("DPPS"),
        IFFParser.stringToID("DPAN"),
        IFFParser.stringToID("DLTA"),
        IFFParser.stringToID("BODY"),
    };

    private static Comparator<IFFChunkNode> nodeComparator = new Comparator<IFFChunkNode>() {

        public int compare(IFFChunkNode o1, IFFChunkNode o2) {
            return o1.getSeqIndex() - o2.getSeqIndex();
        }
    };

    protected static class IFFChunkNode extends DefaultMutableTreeNode {
        private int type;
        private int id;
        private int size;
        private int offset;
        private byte[] data;

        public int getPaddedChunkSize() {
            return size + (size % 2) + 8;
        }

        public void write(DataOutputStream out) throws IOException {
            System.out.println("write "+IFFParser.idToString(id)+" size="+size);
            if (data != null) {
                out.writeInt(id);
                out.writeInt(size);
                out.write(data);
                if (data.length % 2 == 1) {
                    out.writeByte(0);
                }
            } else {
                out.writeInt(type);
                out.writeInt(size);
                out.writeInt(id);
                for (Enumeration i = children(); i.hasMoreElements(); ) {
                    IFFChunkNode child = (IFFChunkNode) i.nextElement();
                    child.write(out);
                }
            }
        }

        public void mergeFrom(IFFChunkNode that) {
            if (data != null) {
                if (id == ANHD_ID) mergeANHD(that);
            } else {

                if (this.isSameAs(that)) {
                    final ArrayList mergedChildren = new ArrayList(Math.max(this.getChildCount(), that.getChildCount()));
                    Collections.sort(this.children, nodeComparator);
                    Collections.sort(that.children, nodeComparator);

                    int thatCount = that.getChildCount();
                    int thisCount = this.getChildCount();
                    int thisIndex = 0, thatIndex = 0;
                    int comparison;
                    while (thisIndex < thisCount || thatIndex < thatCount) {
                        if (thisIndex >= thisCount) {
                            comparison = 1;
                        } else if (thatIndex >= thatCount) {
                            comparison = -1;
                        } else {
                            comparison = ((IFFChunkNode) this.getChildAt(thisIndex)).compareTo((IFFChunkNode) that.getChildAt(thatIndex));
                        }

                        if (comparison < 0) {
                            System.out.println("Inserting from File1:"+this.getChildAt(thisIndex)+" into "+this);
                            mergedChildren.add(this.getChildAt(thisIndex));
                            thisIndex++;
                        } else if (comparison == 0) {
                            ((IFFChunkNode) this.getChildAt(thisIndex)).mergeFrom((IFFChunkNode) that.getChildAt(thatIndex));
                            mergedChildren.add(this.getChildAt(thisIndex));
                            thatIndex++; thisIndex++;
                        } else {
                            IFFChunkNode thatNode = (IFFChunkNode) that.getChildAt(thatIndex);
                            if (thatNode.id == CMAP_ID || thatNode.id == ILBM_ID) {
                                System.out.println("Skipping from File2:"+that.getChildAt(thatIndex));
                            } else {
                                System.out.println("Inserting from File2:"+that.getChildAt(thatIndex)+" into "+this);
                                mergedChildren.add(that.getChildAt(thatIndex));
                            }
                            thatIndex++;
                        }
                    }

                    this.removeAllChildren();
                    this.size = 4;
                    for (Iterator i = mergedChildren.iterator(); i.hasNext(); ) {
                        IFFChunkNode newChild = (IFFChunkNode) i.next();
                        this.size += newChild.getPaddedChunkSize();
                        this.add(newChild);
                    }
                } else {
                    System.out.println(this+"!="+that);
                }
            }
        }

        public void mergeANHD(IFFChunkNode that) {
            System.out.print(IFFParser.idToString(id)+" "+IFFParser.idToString(that.id)+" reltime=");
            for (int i=0; i < 4; i++) {
                this.data[10+i]=0;
            }
            for (int i=0; i < 4; i++) {
                this.data[14+i]=that.data[14+i];
                System.out.print(that.data[14+i]+" ");
            }
            System.out.println();
        }

        public int compareTo(IFFChunkNode that) {
            return this.getSeqIndex() - that.getSeqIndex();
        }

        public int getSeqIndex() {
            for (int i=0; i < sequence.length; i++) {
                if (id == sequence[i]) return i;
            }
            throw new ArrayIndexOutOfBoundsException("no index for "+IFFParser.idToString(id));

        }

        public IFFChunkNode(int type, int id, int size, int offset, byte[] data) {
            this.type = type;
            this.id = id;
            this.size = size;
            this.offset = offset;
            this.data = data;
        }
        public String getType() {
            return IFFParser.idToString(type);
        }
        public String getID() {
            return IFFParser.idToString(id);
        }
        public int getSize() {
            return size;
        }
        public int getOffset() {
            return offset;
        }
        public byte[] getRawData() {
            return data;
        }

        public boolean isSameAs(IFFChunkNode that) {
            return this.type == that.type && this.id == that.id;
        }

        public String toString() {
            return IFFParser.idToString(type) + " "+IFFParser.idToString(id);
        }


        public void dump(int depth) {
            StringBuffer buf = new StringBuffer(depth);
            for (int i=0; i < depth; i++) {
                buf.append('.');
            }
            buf.append(IFFParser.idToString(type) + " "+IFFParser.idToString(id)+" "+numFormat.format(size));
            System.out.println(buf.toString());

            for (Enumeration i=children(); i.hasMoreElements(); ) {
                IFFChunkNode child = (IFFChunkNode) i.nextElement();
                child.dump(depth + 1);
            }
        }
    }


    protected static class Loader implements IFFVisitor {
        private IFFChunkNode root;
        private IFFChunkNode current;

        public Loader() {
        }

        public IFFChunkNode getRoot() {
            return root;
        }

        public void enterGroup(IFFChunk group) throws ParseException, AbortException {
            IFFChunkNode groupNode = new IFFChunkNode(
            group.getID(),
            group.getType(),
            (int) group.getSize(),
            (int) group.getScan(),
            null
            );

            if (current == null) {
                root = current = groupNode;
            } else {
                current.add(groupNode);
                current = groupNode;
            }
        }

        public void leaveGroup(IFFChunk group) throws ParseException, AbortException {
            current = (IFFChunkNode) current.getParent();
        }

        public void visitChunk(IFFChunk group, IFFChunk chunk) throws ParseException, AbortException {
            IFFChunkNode chunkNode = new IFFChunkNode(
            group.getType(),
            chunk.getID(),
            (int) chunk.getSize(),
            (int) chunk.getScan(),
            chunk.getData()
            );
            current.add(chunkNode);
        }
    }

    private void merge(File f1, File f2) throws IOException {
        IFFChunkNode root1, root2;

        Loader loader = new Loader();
        InputStream in = new FileInputStream(f1);
        try {
            IFFParser iff = new IFFParser();
            iff.parse(in, loader);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        } catch (AbortException e) {
            throw new IOException(e.getMessage());
        } finally {
            in.close();
        }

        root1 = loader.getRoot();

        loader = new Loader();
        in = new FileInputStream(f2);
        try {
            IFFParser iff = new IFFParser();
            iff.parse(in, loader);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        } catch (AbortException e) {
            throw new IOException(e.getMessage());
        } finally {
            in.close();
        }

        root2 = loader.getRoot();

        root1.mergeFrom(root2);


        File file3 = new File(file1.getParentFile(), "merged"+file1.getName());

        DataOutputStream out = new DataOutputStream(new FileOutputStream(file3));
        try {
            root1.write(out);
            out.flush();
        } finally {
           out.close();
        }


    }


    private void initComponents() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        mergeMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();

        setTitle("ANIM Merger");
        fileMenu.setText("File");
        mergeMenuItem.setText("Merge...");
        mergeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                merge(evt);
            }
        });

        fileMenu.add(mergeMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        pack();
    }

    private void merge(java.awt.event.ActionEvent evt) {

        if (chooser == null) chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (file1 != null) chooser.setSelectedFile(file1);
        if (chooser.showDialog(this, "Open File 1") == JFileChooser.APPROVE_OPTION) {
            file1 = chooser.getSelectedFile();
            if (file2 != null) chooser.setSelectedFile(file2);
            if (chooser.showDialog(this, "Open File 2") == JFileChooser.APPROVE_OPTION) {
                file2 = chooser.getSelectedFile();
                new Worker() {
                    @Override
                    public Object construct() {
                        try {
                            merge(file1, file2);
                            return null;
                        } catch (Throwable t) {
                            return t;
                        }
                    }
                    @Override
                    public void finished() {
                        Object result = getValue();
                        if (result instanceof Throwable) {
                            ((Throwable) result).printStackTrace();
                            JOptionPane.showMessageDialog(AnimMerger.this, "<html>Error merging files<br>"+result, "AnimMerger", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(AnimMerger.this, "Done", "AnimMerger", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }.start();
            }
        }

    }



    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }


    public static void main(String args[]) {
        JFrame f = new AnimMerger();
        f.setSize(400, 300);
        f.setVisible(true);
    }


    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenuBar menuBar;
    private JMenuItem mergeMenuItem;


}
