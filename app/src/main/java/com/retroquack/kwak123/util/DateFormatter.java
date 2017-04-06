package com.retroquack.kwak123.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;

final class DateFormatter implements IAxisValueFormatter {
    private static DateFormat format;

    DateFormatter() {
        format = DateFormat.getDateInstance(DateFormat.SHORT);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return format.format(value);
    }
}
