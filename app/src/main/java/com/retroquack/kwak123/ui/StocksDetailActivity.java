package com.retroquack.kwak123.ui;

import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.retroquack.kwak123.R;
import com.retroquack.kwak123.data.Contract;
import com.retroquack.kwak123.util.Utility;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVParser;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StocksDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_STOCK_SYMBOL = "stock_symbol";

    @BindView(R.id.detail_app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.detail_collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.detail_toolbar) Toolbar toolbarView;
    @BindView(R.id.detail_scroll_view)
    NestedScrollView scrollView;
    @BindView(R.id.stock_name) TextView nameTextView;
    @BindView(R.id.stock_current) TextView currentTextView;
    @BindView(R.id.stock_change) TextView changeTextView;
    @BindView(R.id.stock_history) LineChart historyLineChart;

    private String symbol;
    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentFormat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        symbol = getIntent().getStringExtra(KEY_STOCK_SYMBOL);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        collapsingToolbarLayout.setOnClickListener(expandListener);
        toolbarView.setOnClickListener(expandListener);

        setSupportActionBar(toolbarView);
        getSupportActionBar().setTitle(symbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        customizeChart();

        dollarFormat = Utility.getDollarFormat();
        dollarFormatWithPlus = Utility.getDollarFormatWithPlus();
        percentFormat = Utility.getPercentageFormat();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void customizeChart() {
        int prefTextSize = 14;
        int prefBottomOffset = 12;
        int prefValueCount = 4;

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

            if (absoluteChange > 0) {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String absChange = dollarFormatWithPlus.format(absoluteChange);
            String perChange = percentFormat.format(percentChange/ 100);

            currentTextView.setText(dollarFormat.format(cursor
                    .getFloat(Contract.Quote.POSITION_PRICE)));
            changeTextView.setText(absChange + " (" + perChange + ")");
            historyLineChart.setData(addData(cursor));
            historyLineChart.invalidate();
        } else {
            Timber.w("Error updating UI?");
        }
    }

    private LineData addData(Cursor cursor) {
        String historyRaw = cursor.getString(Contract.Quote.POSITION_HISTORY);
        CSVParser parser = new CSVParser();
        List<Entry> entries = new ArrayList<>();

        try {
            String[] strings = parser.parseLine(historyRaw);
            entries = parseStringsToEntries(strings);
            Collections.sort(entries, Utility.getEntryComparator());
        } catch (IOException ex) {
            Timber.w("Trouble parsing history");
            ex.printStackTrace();
        }

        LineDataSet data = new LineDataSet(entries, "label");
        data.setDrawValues(false);
        return new LineData(data);
    }

    private List<Entry> parseStringsToEntries(String[] strings) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < strings.length - 1; i += 2) {
            entries.add(new Entry(Float.parseFloat(strings[i]),
                    Float.parseFloat(strings[i+1])));
        }
        return entries;
    }

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
}
