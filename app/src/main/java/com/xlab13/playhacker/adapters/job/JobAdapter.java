package com.xlab13.playhacker.adapters.job;

import static com.xlab13.playhacker.Config.isCooldown;
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
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.DoJobMutation;
import com.example.GetHealthQuery;
import com.example.GetJobsQuery;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.WorkViewHolder> {
    Context context;

    private List<GetJobsQuery.GetJob> items;

    public JobAdapter(Context context, List<GetJobsQuery.GetJob> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        WorkViewHolder holder = new WorkViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        GetJobsQuery.GetJob item = items.get(position);

        holder.tvText.setText(item.title());

        switch (item.status()) {
            case "available":
                holder.clCont.setBackground(context.getDrawable(R.drawable.style_background));
                if (Objects.equals(item.type(), "white")){
                    holder.ivPic.setImageResource(R.drawable.icon_menu_work);
                }  else holder.ivPic.setImageResource(R.drawable.icon_menu_hack);

                holder.clCont.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isCooldown) return;
                        mTools.playSound();
                        Animations.animateCooldown(holder.flAnimCooldown, context);

                        ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                        DoJobMutation doJobMutation = DoJobMutation.builder()
                                .id(item.id())
                                .build();

                        client.mutate(doJobMutation)
                                .enqueue(new ApolloCall.Callback<DoJobMutation.Data>() {
                                    @Override
                                    public void onResponse(@NonNull Response<DoJobMutation.Data> response) {
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
                });
                break;
            case "notAvailable":
                holder.clCont.setBackground(context.getDrawable(R.drawable.style_background_dark));
                if (Objects.equals(item.type(), "white")){ holder.ivPic.setImageResource(R.drawable.icon_menu_work);
                }  else holder.ivPic.setImageResource(R.drawable.icon_menu_hack);

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
                if (Objects.equals(item.type(), "white")){ holder.ivPic.setImageResource(R.drawable.icon_menu_work_red);
                }  else holder.ivPic.setImageResource(R.drawable.icon_menu_hack_red);

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
    }

    public void updateItems(List<GetJobsQuery.GetJob> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetJobsQuery.GetJob> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetJobsQuery.GetJob> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class WorkViewHolder extends RecyclerView.ViewHolder{

        TextView tvText;
        ImageView ivPic;
        ConstraintLayout clCont;
        FrameLayout flAnimCooldown;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvJobText);

            clCont = itemView.findViewById(R.id.clJobCont);

            ivPic = itemView.findViewById(R.id.ivJobPic);

            flAnimCooldown = itemView.findViewById(R.id.flJobAnim);
        }
    }
}
