package com.retroquack.kwak123.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;

public final class DateFormatter implements IAxisValueFormatter {
    private static DateFormat format;
    private static DateFormatter dateFormatter;

    private DateFormatter() {
        format = DateFormat.getDateInstance(DateFormat.SHORT);
    }

    public static DateFormatter getInstance() {
        if (dateFormatter == null) {
            dateFormatter = new DateFormatter();
        }
        return dateFormatter;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return format.format(value);
    }
}
