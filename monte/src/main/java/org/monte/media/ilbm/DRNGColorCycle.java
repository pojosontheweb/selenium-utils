
package org.monte.media.ilbm;

import java.util.Arrays;


public class DRNGColorCycle extends ColorCycle {

    public abstract static class Cell implements Comparable<Cell> {

        protected int cell;
        protected int value;

        public Cell(int cell) {
            this.cell = cell;
        }

        
        public abstract void readValue(int[] rgbs, boolean isHalfbright);

        
        public abstract void writeValue(int[] rgbs, boolean isHalfbright);

        @Override
        public int compareTo(Cell that) {
            return this.cell - that.cell;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Cell) {
                Cell that = (Cell) o;
                return that.cell == this.cell;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return cell;
        }
    }

    
    public static class DColorCell extends Cell {

        private int rgb;

        public DColorCell(int cell, int rgb) {
            super(cell);
            this.rgb = rgb;
        }

        
        @Override
        public void readValue(int[] rgbs, boolean isHalfbright) {
            value = isHalfbright ? rgb & 0x0f0f0f : rgb;
        }

        
        @Override
        public void writeValue(int[] rgbs, boolean isHalfbright) {

        }
    }

    
    public static class DIndexCell extends Cell {

        private int index;

        public DIndexCell(int cell, int index) {
            super(cell);
            this.index = index;
        }

        
        @Override
        public void readValue(int[] rgbs, boolean isHalfbright) {
            value = isHalfbright ? rgbs[index + 32] : rgbs[index];
        }

        
        @Override
        public void writeValue(int[] rgbs, boolean isHalfbright) {
            rgbs[isHalfbright ? index + 32 : index] = value;
        }
    }
    
    private int min;
    
    private int max;
    
    private boolean isEHB;
    
    private Cell[] ic;
    
    private Cell[] cells;
    private boolean isReverse;

    
    public DRNGColorCycle(int rate, int timeScale, int min, int max, boolean isActive, boolean isEHB, Cell[] cells) {
        super(rate, timeScale, isActive);
        this.min = min;
        this.max = max;
        this.isEHB = isEHB;
        this.cells = cells;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    private void interpolateCells(int[] rgbs) {

        ic = new Cell[max - min + 1];
        Arrays.sort(cells);
        for (int i = 0; i < cells.length; i++) {
            ic[cells[i].cell - min] = cells[i];
            cells[i].readValue(rgbs, false);
        }
        int left = cells.length - 1;
        int right = 0;

        for (int i = 0; i < ic.length; i++) {
            if (cells[right].cell == i) {
                left = right;
                right = (right == cells.length - 1) ? 0 : right + 1;
            } else {

                int levels=cells[left].cell<cells[right].cell?cells[right].cell-cells[left].cell:max-cells[left].cell+cells[right].cell+1;
                int blend=cells[right].cell>(i+min)?cells[right].cell-(i+min):max-(i+min)+cells[right].cell+1;
                int lrgb=cells[left].value;
                int rrgb=cells[right].value;
                ic[i]=new DColorCell(i,
                       ( ((lrgb&0xff0000)*blend+(rrgb&0xff0000)*(levels-blend))/levels&0xff0000)|
                       ( ((lrgb&0xff00)*blend+(rrgb&0xff00)*(levels-blend))/levels&0xff00)|
                       ( ((lrgb&0xff)*blend+(rrgb&0xff)*(levels-blend))/levels)
                       );

            }
        }
    }

    @Override
    public void doCycle(int[] rgbs, long time) {
        if (isActive) {
            if (ic == null) {
                interpolateCells(rgbs);
            }

            int shift = (int) ((time * rate / timeScale / 1000) % (ic.length));

            if (isReverse) {
                for (int i = 0; i < ic.length; i++) {
                    ic[i].readValue(rgbs, false);
                }

                for (int j = 0; j < shift; j++) {
                    int tmp = ic[0].value;
                    for (int i = 1; i < ic.length; i++) {
                        ic[i - 1].value = ic[i].value;
                    }
                    ic[ic.length - 1].value = tmp;
                }
                for (int i = 0; i < ic.length; i++) {
                    ic[i].writeValue(rgbs, false);
                }
                if (isEHB) {
                    for (int i = 0; i < ic.length; i++) {
                        ic[i].readValue(rgbs, true);
                    }
                    for (int j = 0; j < shift; j++) {
                        int tmp = ic[0].value;
                        for (int i = 1; i < ic.length; i++) {
                            ic[i - 1].value = ic[i].value;
                        }
                        ic[ic.length - 1].value = tmp;
                    }
                    for (int i = 0; i < ic.length; i++) {
                        ic[i].writeValue(rgbs, true);
                    }
                }
            } else {
                for (int i = 0; i < ic.length; i++) {
                    ic[i].readValue(rgbs, false);
                }

                for (int j = 0; j < shift; j++) {
                    int tmp = ic[ic.length - 1].value;
                    for (int i = ic.length - 1; i > 0; i--) {
                        ic[i].value = ic[i - 1].value;
                    }
                    ic[0].value = tmp;
                }
                for (int i = 0; i < ic.length; i++) {
                    ic[i].writeValue(rgbs, false);
                }
                if (isEHB) {
                    for (int i = 0; i < ic.length; i++) {
                        ic[i].readValue(rgbs, true);
                    }
                    for (int j = 0; j < shift; j++) {
                        int tmp = ic[ic.length - 1].value;
                        for (int i = ic.length - 1; i > 0; i--) {
                            ic[i].value = ic[i - 1].value;
                        }
                        ic[0].value = tmp;
                    }
                    for (int i = 0; i < ic.length; i++) {
                        ic[i].writeValue(rgbs, true);
                    }
                }
            }
        }
    }
}
