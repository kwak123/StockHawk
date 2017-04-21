package com.retroquack.kwak123.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.retroquack.kwak123.R;
import com.retroquack.kwak123.data.Contract;
import com.retroquack.kwak123.data.PrefUtils;
import com.retroquack.kwak123.util.Utility;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVParser;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StocksDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_STOCK_SYMBOL = "stock_symbol";

    private static final String DATA_SET_LABEL = "label";
    private static final String CURRENT_INDEX_KEY = "ci";

    @BindView(R.id.detail_app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.detail_collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.detail_toolbar) Toolbar toolbarView;
    @BindView(R.id.detail_scroll_view) NestedScrollView scrollView;
    @BindView(R.id.stock_name) TextView nameTextView;
    @BindView(R.id.stock_current) TextView currentTextView;
    @BindView(R.id.detail_last_update_view) TextView updateView;
    @BindView(R.id.stock_change) TextView changeTextView;
    @BindView(R.id.stock_history) LineChart historyLineChart;
    @BindView(R.id.selected_date_view) TextView selectedDateView;
    @BindView(R.id.selected_value_view) TextView selectedValueView;
    @BindView(R.id.detail_arrow_left) ImageView leftView;
    @BindView(R.id.detail_arrow_right) ImageView rightView;

    private String symbol;
    private List<Entry> entries;

    private int currentIndex = -1;

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        symbol = getIntent().getStringExtra(KEY_STOCK_SYMBOL);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        leftView.setOnClickListener(leftListener);
        rightView.setOnClickListener(rightListener);

        collapsingToolbarLayout.setOnClickListener(expandListener);
        collapsingToolbarLayout.setTitle(symbol);
        toolbarView.setOnClickListener(expandListener);
        setSupportActionBar(toolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        historyLineChart.setScaleYEnabled(false);
        historyLineChart.setOnChartValueSelectedListener(this);
        customizeChart();

        dollarFormat = Utility.getDollarFormat();
        dollarFormatWithPlus = Utility.getDollarFormatWithPlus();
        percentFormat = Utility.getPercentageFormat();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(CURRENT_INDEX_KEY, currentIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentIndex = savedInstanceState.getInt(CURRENT_INDEX_KEY);
    }

    // Chart utility methods
    private void customizeChart() {
        int prefTextSize = 14;
        int prefBottomOffset = 12;
        int prefValueCount = 5;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int chartWidth = displayMetrics.widthPixels;
        int chartHeight = chartWidth * 11/10;

        XAxis xAxis = historyLineChart.getXAxis();
        YAxis yAxis = historyLineChart.getAxisLeft();

        xAxis.setValueFormatter(Utility.getDateFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(prefValueCount);

        historyLineChart.getAxisRight().setDrawLabels(false);
        historyLineChart.getLegend().setEnabled(false);

        xAxis.setTextColor(getResources().getColor(R.color.text_white));
        yAxis.setTextColor(getResources().getColor(R.color.text_white));

        xAxis.setTextSize(prefTextSize);
        yAxis.setTextSize(prefTextSize);

        historyLineChart.setKeepPositionOnRotation(true);
        historyLineChart.setMinimumHeight(chartHeight);
        historyLineChart.setExtraBottomOffset(prefBottomOffset);
        historyLineChart.setMaxVisibleValueCount(prefValueCount);

        Description desc = new Description();
        desc.setText("");
        historyLineChart.setDescription(desc);
        historyLineChart.invalidate();
    }

    private void updateUI(Cursor cursor){
        if (cursor != null) {
            nameTextView.setText(cursor.getString(Contract.Quote.POSITION_NAME));

            float absoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            String absChange = dollarFormatWithPlus.format(absoluteChange);
            String perChange = percentFormat.format(percentChange/ 100);

            currentTextView.setText(dollarFormat.format(cursor
                    .getFloat(Contract.Quote.POSITION_PRICE)));
            changeTextView.setText(absChange + " (" + perChange + ")");

            String updateViewText = getString(R.string.updated_last) + PrefUtils.getLastUpdate(this);
            updateView.setText(updateViewText);

            LineData data = addData(cursor);
            historyLineChart.setData(data);

            if (currentIndex == -1) {
                currentIndex = entries.size() - 1;
                historyLineChart.highlightValue(entries.get(currentIndex).getX(), 0);
            }
            historyLineChart.invalidate();

        } else {
            Timber.w("Cursor was null?");
            Toast.makeText(this, getString(R.string.error_no_refresh), Toast.LENGTH_SHORT).show();
        }
    }

    private LineData addData(Cursor cursor) {
        String historyRaw = cursor.getString(Contract.Quote.POSITION_HISTORY);
        CSVParser parser = Utility.getParser();

        try {
            String[] strings = parser.parseLine(historyRaw);
            entries = parseStringsToEntries(strings);
            Collections.sort(entries, Utility.getEntryComparator());
        } catch (IOException ex) {
            Timber.w("Trouble parsing history");
            ex.printStackTrace();
        }

        LineDataSet data = new LineDataSet(entries, DATA_SET_LABEL);
        data.setDrawValues(false);
        return new LineData(data);
    }

    private List<Entry> parseStringsToEntries(String[] strings) {
        List<Entry> parsedEntries = new ArrayList<>();
        for (int i = 0; i < strings.length - 1; i += 2) {
            parsedEntries.add(new Entry(Float.parseFloat(strings[i]),
                    Float.parseFloat(strings[i+1])));
        }
        return parsedEntries;
    }

    // ChartValueListener
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        currentIndex = entries.indexOf(e);
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
        DecimalFormat dFormat = Utility.getDollarFormat();

        String date = format.format(e.getX());
        String value = dFormat.format(e.getY());
        selectedDateView.setText(date);
        selectedValueView.setText(value);
    }

    @Override
    public void onNothingSelected() {
        Timber.v("Nothing selected");
    }

    // Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            data.moveToFirst();
            updateUI(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    // Click to expand Listener
    private View.OnClickListener expandListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (appBarLayout.getTop() == 0) {
                appBarLayout.setExpanded(false);
            } else {
                appBarLayout.setExpanded(true);
            }
        }
    };

    // Navigation listeners
    private View.OnClickListener leftListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentIndex > 0) {
                historyLineChart.highlightValue(entries.get(currentIndex - 1).getX(), 0);
            } else {
                Toast.makeText(StocksDetailActivity.this, "Already showing earliest value", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener rightListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentIndex < entries.size() - 1) {
                historyLineChart.highlightValue(entries.get(currentIndex + 1).getX(), 0);
            } else {
                Toast.makeText(StocksDetailActivity.this, "Already showing latest value", Toast.LENGTH_SHORT).show();
            }
        }
    };

}