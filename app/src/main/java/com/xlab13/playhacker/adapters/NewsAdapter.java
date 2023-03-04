package com.xlab13.playhacker.adapters;


import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetNewsQuery;
import com.example.MarkNewsAsReadMutation;
import com.xlab13.playhacker.fragments.info.FragmentNews;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    Context context;

    private List<GetNewsQuery.GetNew> items;

    private FragmentNews fragNews;

    public NewsAdapter(Context context, List<GetNewsQuery.GetNew> items, FragmentNews fragmentNews){
        this.context = context;
        this.items = items;
        this.fragNews = fragmentNews;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        NewsViewHolder holder = new NewsViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        GetNewsQuery.GetNew item = items.get(position);

        holder.tvText.setText(item.title());
        holder.tvTime.setText(getTime(item.date().longValue()));

        if (item.read()) {
            holder.ivNew.setImageResource(R.drawable.icon_news_dark);
            holder.clCont.setBackground(context.getDrawable(R.drawable.style_background_dark));
        }

        holder.clCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();
                fragNews.setData(item.title(), item.body());

                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.flInfoCont, fragNews, "news");
                transaction.addToBackStack(null);
                transaction.commit();

                if (!item.read()){
                    ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                    MarkNewsAsReadMutation markNewsAsReadMutation = MarkNewsAsReadMutation.builder()
                            .id(item.id()).build();

                    client.mutate(markNewsAsReadMutation).enqueue(new ApolloCall.Callback<MarkNewsAsReadMutation.Data>() {
                        @Override
                        public void onResponse(@NonNull Response<MarkNewsAsReadMutation.Data> response) {
                            if (response.hasErrors()) {
                                ((Activity) context).runOnUiThread(() -> {
                                    String error = response.getErrors().get(0).getMessage();

                                    mTools.showErrorDialog(context, error);
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull ApolloException e) {
                            mTools.debug(e.getMessage());
                            mTools.startErrorConnectionActivity(context);
                        }
                    });
                }
            }
        });
    }

    public void updateItems(List<GetNewsQuery.GetNew> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetNewsQuery.GetNew> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetNewsQuery.GetNew> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getTime(long utcTime){
        Date time = new Date(utcTime);

        DateFormat dateUTCFormat = new SimpleDateFormat("HH:mm");
        dateUTCFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateUTCFormat.format(time);
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{

        TextView tvText, tvTime;
        ImageView ivNew;
        ConstraintLayout clCont;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvNewsText);
            tvTime = itemView.findViewById(R.id.tvNewsTime);

            ivNew = itemView.findViewById(R.id.ivNewsPic);

            clCont = itemView.findViewById(R.id.clNewsCont);
        }
    }
}
