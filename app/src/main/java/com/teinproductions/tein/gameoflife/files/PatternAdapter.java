package com.teinproductions.tein.gameoflife.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinproductions.tein.gameoflife.R;

import java.util.List;

public class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.ViewHolder> {

    private Context context;
    private List<RLEPattern> patterns;
    private OnClickPatternListener listener;

    public PatternAdapter(Context context, OnClickPatternListener listener, List<RLEPattern> patterns) {
        this.context = context;
        this.listener = listener;
        this.patterns = patterns;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_pattern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(patterns.get(position));
    }

    @Override
    public int getItemCount() {
        return patterns.size();
    }

    public interface OnClickPatternListener {
        void onClick(RLEPattern pattern);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, filename;
        private RLEPattern pattern;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_textView);
            filename = (TextView) itemView.findViewById(R.id.filename_textView);
            itemView.findViewById(R.id.itemRoot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem();
                }
            });
        }

        public void bind(RLEPattern pattern) {
            this.pattern = pattern;
            name.setText(pattern.getName());
            filename.setText(pattern.getFilename());
        }

        public void onClickItem() {
            if (listener != null) listener.onClick(pattern);
        }
    }
}
