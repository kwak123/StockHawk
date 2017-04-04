package com.retroquack.kwak123.util;

import com.github.mikephil.charting.data.Entry;

import java.util.Comparator;

public final class EntryComparator implements Comparator<Entry> {

    private static EntryComparator entryComparator;

    private EntryComparator(){}

    public static EntryComparator getInstance() {
        if (entryComparator == null) {
            entryComparator = new EntryComparator();
        }
        return entryComparator;
    }

    @Override
    public int compare(Entry o1, Entry o2) {
        float f1 = o1.getX();
        float f2 = o2.getX();
        if (f1 < f2) return -1;
        if (f1 > f2) return 1;
        return 0;
    }
}
