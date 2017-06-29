package com.teinproductions.tein.gameoflife.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinproductions.tein.gameoflife.R;

import java.util.List;

public class PatternAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE_PATTERN = 0;
    private static final int ITEM_VIEW_TYPE_HEADER = 1;

    private Context context;
    private List<PatternListable> items;
    private OnClickPatternListener listener;

    public PatternAdapter(Context context, OnClickPatternListener listener, List<PatternListable> items) {
        this.context = context;
        this.listener = listener;
        this.items = items;
    }

    public void setData(List<PatternListable> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_PATTERN:
                View view1 = LayoutInflater.from(context).inflate(R.layout.list_item_pattern, parent, false);
                return new PatternViewHolder(view1);
            case ITEM_VIEW_TYPE_HEADER:
                View view2 = LayoutInflater.from(context).inflate(R.layout.list_item_header, parent, false);
                return new HeaderViewHolder(view2);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof RLEPattern ? ITEM_VIEW_TYPE_PATTERN : ITEM_VIEW_TYPE_HEADER;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder)
            ((HeaderViewHolder) holder).tv.setText(((Header) items.get(position)).getText());
        else if (holder instanceof PatternViewHolder)
            ((PatternViewHolder) holder).bind((RLEPattern) items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public interface OnClickPatternListener {
        void onClick(RLEPattern pattern);
        boolean onLongClick(RLEPattern pattern);
    }

    public class PatternViewHolder extends RecyclerView.ViewHolder {
        private TextView name, filename;
        private RLEPattern pattern;

        public PatternViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_textView);
            filename = (TextView) itemView.findViewById(R.id.filename_textView);
            itemView.findViewById(R.id.itemRoot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) listener.onClick(pattern);
                }
            });
            itemView.findViewById(R.id.itemRoot).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) return listener.onLongClick(pattern);
                    return false;
                }
            });
        }

        public void bind(RLEPattern pattern) {
            this.pattern = pattern;
            name.setText(pattern.getName());
            filename.setText(pattern.getFilename());
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.textView);
        }
    }
}
