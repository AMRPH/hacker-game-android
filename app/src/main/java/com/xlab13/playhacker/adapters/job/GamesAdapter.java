package com.xlab13.playhacker.adapters.job;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.xlab13.playhacker.R;
import com.xlab13.playhacker.utils.GameItem;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.MinigamesViewHolder> {
    Context context;

    private List<GameItem> items;

    public GamesAdapter(Context context, List<GameItem> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MinigamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        MinigamesViewHolder holder = new MinigamesViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MinigamesViewHolder holder, int position) {
        GameItem item = items.get(position);

        holder.tvText.setText(item.title);

        holder.ivPic.setImageResource(item.resourceID);

        holder.clCont.setOnClickListener(item.clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MinigamesViewHolder extends RecyclerView.ViewHolder{

        TextView tvText;
        ImageView ivPic;
        ConstraintLayout clCont;

        public MinigamesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvJobText);

            clCont = itemView.findViewById(R.id.clJobCont);

            ivPic = itemView.findViewById(R.id.ivJobPic);
        }
    }
}
