package com.retroquack.kwak123.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilities for StockHawk, exposes all helper methods and classes for use throughout app
 */

public final class Utility {
    private Utility(){}

    private static DecimalFormat dFormat;
    private static DecimalFormat dFormatWithPlus;
    private static DecimalFormat pFormat;
    private static DateFormatter dateFormatter;

    private static EntryComparator entryComparator;

    public static DecimalFormat getDollarFormat() {
        if (dFormat == null) {
            dFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        }
        return dFormat;
    }

    public static DecimalFormat getDollarFormatWithPlus() {
        if (dFormatWithPlus == null) {
            dFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dFormatWithPlus.setPositivePrefix("+$");
        }
        return dFormatWithPlus;
    }

    public static DecimalFormat getPercentageFormat() {
        if (pFormat == null) {
            pFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            pFormat.setMaximumFractionDigits(2);
            pFormat.setMinimumFractionDigits(2);
            pFormat.setPositivePrefix("+");
        }
        return pFormat;
    }

    public static DateFormatter getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new DateFormatter();
        }
        return dateFormatter;
    }

    public static EntryComparator getEntryComparator() {
        if (entryComparator == null) {
            entryComparator = new EntryComparator();
        }
        return entryComparator;
    }
}
