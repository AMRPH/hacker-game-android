package com.xlab13.playhacker.adapters.job;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmadrosid.svgloader.SvgLoader;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.DoLongtermJobMutation;
import com.example.GetJobsQuery;
import com.example.GetLongtermJobsQuery;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.List;

public class JobLongtermAdapter extends RecyclerView.Adapter<JobLongtermAdapter.WorkViewHolder>{
    Context context;

    private List<GetLongtermJobsQuery.GetLongtermJob> items;

    public JobLongtermAdapter(Context context, List<GetLongtermJobsQuery.GetLongtermJob> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_long, parent, false);
        WorkViewHolder holder = new WorkViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        GetLongtermJobsQuery.GetLongtermJob item = items.get(position);

        SvgLoader.pluck()
                .with((Activity) context)
                .setPlaceHolder(R.drawable.icon_no_image, R.drawable.icon_no_image)
                .load(NetworkService.imgUrl + item.image_path(), holder.ivPic);

        holder.tvText.setText(item.title());
        if(item.price_btc() != null){
            holder.tvBonus.setText("+" + item.price_btc());
            holder.ivBonus.setImageResource(R.drawable.icon_work_bitcoin);
        } else holder.tvBonus.setText("+" + item.price_rub());
       holder.tvMin.setText(item.time()+"");

        switch (item.status()) {
            case "available":
                holder.clCont.setBackground(context.getDrawable(R.drawable.style_background));
                holder.clCont.setOnClickListener((v -> {
                    if (isCooldown) return;
                    mTools.playSound();
                    Animations.animateCooldown(holder.flAnimCooldown, context);

                    ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                    DoLongtermJobMutation doLongtermJobMutation = DoLongtermJobMutation.builder()
                            .id(item.id())
                            .build();

                    client.mutate(doLongtermJobMutation)
                            .enqueue(new ApolloCall.Callback<DoLongtermJobMutation.Data>() {
                                @Override
                                public void onResponse(@NonNull Response<DoLongtermJobMutation.Data> response) {
                                    if (response.hasErrors()) {
                                        String error = response.getErrors().get(0).getMessage();

                                        ((Activity) context).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                MyAlertDialog alertDialog = new MyAlertDialog(context);
                                                alertDialog.setText(error);
                                                alertDialog.addButton(context.getString(R.string.ok), new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                    alertDialog.dismissDialog();
                                                    }
                                                });
                                                alertDialog.showDialog();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull ApolloException e) {
                                    mTools.debug(e.getMessage());
                                    mTools.startErrorConnectionActivity(context);
                                }
                            });
                }));
                break;
            case "notAvailable":
                holder.clCont.setBackground(context.getDrawable(R.drawable.style_background_dark));

                holder.clCont.setOnClickListener((v -> {
                    mTools.playSound();
                    MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                    myAlertDialog.setText(item.message());
                    myAlertDialog.addButton(context.getString(R.string.close), (v1) ->{
                        myAlertDialog.dismissDialog();
                    });
                    myAlertDialog.showDialog();
                }));
                break;
            case "blocked":
                holder.clCont.setBackground(context.getDrawable(R.drawable.style_background_red));

                holder.clCont.setOnClickListener((v -> {
                    mTools.playSound();
                    MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                    myAlertDialog.setText(item.message());
                    myAlertDialog.addButton(context.getString(R.string.close), (v1) ->{
                        myAlertDialog.dismissDialog();
                    });
                    myAlertDialog.showDialog();
                }));
                break;
        }

        if (Config.user.job_end != null){
            holder.clCont.setClickable(false);
            holder.tvTimer.setVisibility(View.VISIBLE);

            mDataController.getJobTimer().observe((LifecycleOwner) context, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    holder.tvTimer.setText(s);
                }
            });
        }
    }

    public void updateItems(List<GetLongtermJobsQuery.GetLongtermJob> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetLongtermJobsQuery.GetLongtermJob> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetLongtermJobsQuery.GetLongtermJob> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class WorkViewHolder extends RecyclerView.ViewHolder{

        TextView tvText, tvTimer, tvMin, tvBonus;
        ImageView ivPic, ivBonus;
        ConstraintLayout clCont;
        FrameLayout flAnimCooldown;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvJobLongText);
            tvTimer = itemView.findViewById(R.id.tvJobLongTimer);
            tvMin = itemView.findViewById(R.id.tvJobLongMin);
            tvBonus = itemView.findViewById(R.id.tvJobLongBonus);
            ivBonus = itemView.findViewById(R.id.ivJobLongBonus);


            clCont = itemView.findViewById(R.id.clJobLongCont);

            ivPic = itemView.findViewById(R.id.ivJobLongPic);

            flAnimCooldown = itemView.findViewById(R.id.flJobLongAnim);
        }
    }
}
