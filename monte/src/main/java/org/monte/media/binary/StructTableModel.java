
package org.monte.media.binary;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;


public class StructTableModel extends AbstractTableModel {
    protected Vector data;
    protected StructParser.TypedefDeclaration typedef;
    private final char[] HEX = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
    
    
    public StructTableModel(StructParser.TypedefDeclaration typedef, Vector data) {
        this.typedef = typedef;
        this.data = data;
    }
    
    @Override
    public int getRowCount() {
        return data.size();
    }
    @Override
    public int getColumnCount() {
        return 2;
    }
    @Override
    public Object getValueAt(int row, int column) {
        Value elem = (Value) data.elementAt(row);
        Object value;
        if (column == 0) {

            int p = elem.qualifiedIdentifier.indexOf('.');
            String identifier = (p == -1) ? elem.qualifiedIdentifier : elem.qualifiedIdentifier.substring(p + 1);
            value = identifierToString(((elem.index == null) ? identifier : identifier+elem.index).toString());
        } else {
            value = elem.value;
        }
        return value;
    }
    
    public static String identifierToString(String s) {
        StringBuilder b = new StringBuilder();
        boolean wasUpperCase = true;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                if (!wasUpperCase) {
                    b.append(' ');
                }
                wasUpperCase = true;
            } else {
                wasUpperCase = false;
            }
            b.append(s.charAt(i));

        }
        return b.toString();
    }
    
    @Override
    public String getColumnName(int column) {
        return (column == 0) ? "Name" : "Value";
    }
    
    public static class Value {
        public String qualifiedIdentifier;
        public Object declaration;
        public String index;
        public Object value;
        public int intValue;
        public Value() {
            
        }
        public Value(String qualfiedIdentifier, String index, Object declaration, Object value, int intValue) {
            this.qualifiedIdentifier = qualifiedIdentifier;
            this.index = index;
            this.declaration = declaration;
            this.value = value;
            this.intValue=intValue;
        }
    }
    
    @Override
    public String toString() {
        return (typedef != null) ? typedef.toString() : super.toString();
    }
}

