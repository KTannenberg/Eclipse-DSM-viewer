package com.dsmviewer.ui.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dtangler.core.dsm.Dsm;
import org.dtangler.core.dsm.DsmRow;

/**
 * 
 * @author <a href="mailto:klaus.tannnenberg@gmail.com">Grigorov Aleksey</a>
 * 
 */
public class DSMModel {
    private Row[] rows;
    private Label[] labels;

    public Row[] getRows() {
        return rows;
    }

    public Label[] getLabels() {
        return labels;
    }

    public void createModel(final Dsm dsm)
    {
        SortedSet<String> naturalOrder = new TreeSet<String>();
        LinkedList<String> tanglerOrder = new LinkedList<String>();
        for (DsmRow row : dsm.getRows())
        {
            String curPath = row.getDependee().getFullyQualifiedName();
            curPath = curPath.substring(curPath.lastIndexOf(':') + 1).trim();
            tanglerOrder.add(curPath);
            while (curPath.length() > 1) {
                naturalOrder.add(curPath);
                int index = curPath.lastIndexOf('.');
                if (index >= 0) {
                    curPath = curPath.substring(0, index);
                } else {
                    break;
                }
            }
        }
        
        rows = new Row[naturalOrder.size()];
        labels = new Label[naturalOrder.size()];

        int pos = 0;
        for (String cur : naturalOrder) {
            String shortname = (cur.lastIndexOf('.') >= 0) ? cur.substring(cur.lastIndexOf('.') + 1) : cur;
            labels[pos] = new Label(pos, 0, cur, shortname, true);
            pos++;
        }
        naturalOrder.clear();
        
        buildTree(labels, 0, labels.length - 1, -1);

        int[] active = new int[tanglerOrder.size()];
        for (int n = 0; n < labels.length; n++) {
            rows[n] = new Row(n, new int[rows.length]);
            int mapping = tanglerOrder.indexOf(labels[n].fullname);
            if(mapping != -1) {
                active[mapping] = n;
            }
        }
        for (int n = 0; n < active.length; n++) {
            for (int m = 0; m < active.length; m++) {
                int element = dsm.getRows().get(n).getCells().get(m).getDependencyWeight();
                rows[active[n]].getRow()[active[m]] =  element;
            }
        }
        tanglerOrder.clear();

        buildDSM(rows, labels, 0, labels.length - 1);

        @SuppressWarnings("unused")
        int n = 0; // debug anchor
    }

    private void buildTree(final Label[] array, final int index, final int last, final int parent) {
        if (index >= array.length) {
            if (parent < array.length) {
                buildTree(array, parent + 2, last, parent + 1);
            }
            return;
        }
        else if (parent == -1) {
            for (Label item : array) {
                item.fold = 0;
            }
            buildTree(array, index + 1, last, index);
            return;
        }
        else {
            if (array[index].fullname.startsWith(array[parent].fullname + ".")) {
                array[parent].fold++;
                buildTree(array, index + 1, last, parent);
            }
            else {
                buildTree(array, parent + 2, last, parent + 1);
            }
        }
    }

    private void buildDSM(final Row[] rows, final Label[] labels, final int stIdx, final int edIdx) {
        
    }

    protected class Label {
        private int number;
        private int fold;
        private boolean folded;
        private String fullname;
        private String shortname;

        public Label(final int number, final int fold, final String fullname,
                final String shortname, final boolean folded) {
            this.number = number;
            this.folded = folded;
            this.fold = fold;
            this.fullname = fullname;
            this.shortname = shortname;
        }

        public int getNumber() {
            return number;
        }

        public int getFold() {
            return fold;
        }

        public String getFullname() {
            return fullname;
        }

        public String getShortname() {
            return shortname;
        }

        public boolean isFolded() {
            return folded;
        }

        public void setFolded(final boolean folded) {
            this.folded = folded;
        }
        
        public String toString()
        {
            return number + ": " + fullname;
        }
    }

    protected class Row {
        int number;
        private int[] row;

        public Row(final int number, final int[] row)
        {
            this.number = number;
            this.row = row;
        }

        public int[] getRow() {
            return row;
        }
    }
}
