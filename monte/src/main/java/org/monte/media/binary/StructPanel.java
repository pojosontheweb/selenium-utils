
package org.monte.media.binary;


public class StructPanel extends javax.swing.JPanel {


    public StructPanel() {
        initComponents ();
    }

    public void setModel(StructTableModel model) {
        table.setModel(model);
        table.sizeColumnsToFit(-1);
    }


    private void initComponents () {
        setLayout (new java.awt.BorderLayout ());

        scrollPane = new javax.swing.JScrollPane ();

            table = new javax.swing.JTable ();
            table.setRowSelectionAllowed (false);

        scrollPane.setViewportView (table);
        add (scrollPane, "Center");

    }



    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;


}
