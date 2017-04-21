package com.retroquack.kwak123.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.retroquack.kwak123.R;
import com.retroquack.kwak123.data.Contract;
import com.retroquack.kwak123.data.PrefUtils;
import com.retroquack.kwak123.util.Utility;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    private static final int FOOTER = 1;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = Utility.getDollarFormat();
        dollarFormatWithPlus = Utility.getDollarFormatWithPlus();
        percentageFormat = Utility.getPercentageFormat();
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item;
        if (viewType == FOOTER) {
            item = LayoutInflater.from(context).inflate(R.layout.list_item_footer, parent, false);
            return new FooterViewHolder(item);
        }
        item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            String lastUpdate = context.getString(R.string.updated_last) + PrefUtils.getLastUpdate(context);
            footerViewHolder.lastUpdate.setText(lastUpdate);
        } else {
            StockViewHolder stockViewHolder = (StockViewHolder) holder;
            cursor.moveToPosition(position);

            stockViewHolder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
            stockViewHolder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));


            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                stockViewHolder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                stockViewHolder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(context)
                    .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                stockViewHolder.change.setText(change);
            } else {
                stockViewHolder.change.setText(percentage);
            }
        }

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount() + 1;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < cursor.getCount()) {
            return super.getItemViewType(position);
        } else {
            return FOOTER;
        }
    }

    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));

        }


    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.last_update_view) TextView lastUpdate;

        FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
