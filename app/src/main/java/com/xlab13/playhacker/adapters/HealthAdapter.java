package com.xlab13.playhacker.adapters;

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

import com.ahmadrosid.svgloader.SvgLoader;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetHealthQuery;
import com.example.UpdateHealthMutation;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.List;


public class HealthAdapter extends RecyclerView.Adapter<HealthAdapter.HealthViewHolder> {
    Context context;

    private List<GetHealthQuery.GetHealth> items;

    public HealthAdapter(Context context, List<GetHealthQuery.GetHealth> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public HealthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health, parent, false);
        HealthViewHolder holder = new HealthViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HealthViewHolder holder, int position) {
        GetHealthQuery.GetHealth item = items.get(position);

        holder.tvText.setText(item.title());

        MyAlertDialog descriptionDialog = new MyAlertDialog(context);
        descriptionDialog.setText(item.description());
        descriptionDialog.addButton("Закрыть", (v) ->{
            descriptionDialog.dismissDialog();
        });
        holder.clCont.setOnClickListener((v) -> descriptionDialog.showDialog());


        holder.tvCost.setText(mTools.reduceNumber(item.price_rub()));

        switch (item.status()){
            case "available":
                holder.clHealthCost.setBackground(context.getDrawable(R.drawable.style_button_light));
                holder.clHealthCost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isCooldown) return;
                        mTools.playSound();
                        Animations.animateCooldown(holder.flAnimCooldown, context);

                        ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                        UpdateHealthMutation updateHealthMutation = UpdateHealthMutation.builder()
                                .id(item.id())
                                .build();

                        client.mutate(updateHealthMutation)
                                .enqueue(new ApolloCall.Callback<UpdateHealthMutation.Data>() {
                                    @Override
                                    public void onResponse(@NonNull Response<UpdateHealthMutation.Data> response) {
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
                holder.clHealthCost.setBackground(context.getDrawable(R.drawable.style_button_dark));
                holder.clHealthCost.setOnClickListener((v -> {
                    mTools.playSound();
                    MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                    myAlertDialog.setText(item.message());
                    myAlertDialog.addButton(context.getString(R.string.close), (v1) -> {
                        myAlertDialog.dismissDialog();
                    });
                    myAlertDialog.showDialog();
                }));
                break;
            case "blocked":
                holder.clHealthCost.setBackground(context.getDrawable(R.drawable.style_button_red));
                holder.clHealthCost.setOnClickListener((v -> {
                    mTools.playSound();
                    MyAlertDialog myAlertDialog = new MyAlertDialog(context);
                    myAlertDialog.setText(item.message());
                    myAlertDialog.addButton(context.getString(R.string.close), (v1) -> {
                        myAlertDialog.dismissDialog();
                    });
                    myAlertDialog.showDialog();
                }));
                break;
        }

        holder.tvHealth.setText(item.health() > 0 ? "+"+item.health() : item.health()+"");
        holder.tvAlco.setText(item.alcohol() > 0 ? "+"+item.alcohol() : item.alcohol()+"");
        holder.tvMood.setText(item.mood() > 0 ? "+"+item.mood() : item.mood()+"");

        SvgLoader.pluck()
                .with((Activity) context)
                .setPlaceHolder(R.drawable.icon_no_image, R.drawable.icon_no_image)
                .load(NetworkService.imgUrl + item.image_path(), holder.ivPic);
    }

    public void updateItems(List<GetHealthQuery.GetHealth> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetHealthQuery.GetHealth> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetHealthQuery.GetHealth> getItems(){
        return items;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class HealthViewHolder extends RecyclerView.ViewHolder{

        TextView tvText, tvCost, tvHealth, tvAlco, tvMood;
        ImageView ivPic;
        ConstraintLayout clCont, clHealthCost;
        FrameLayout flAnimCooldown;

        public HealthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvHealthTitle);
            tvCost = itemView.findViewById(R.id.tvHealthCost);

            tvHealth = itemView.findViewById(R.id.tvHealthHealth);
            tvAlco = itemView.findViewById(R.id.tvHealthAlcohol);
            tvMood = itemView.findViewById(R.id.tvHealthMood);

            clCont = itemView.findViewById(R.id.clHealth);
            clHealthCost = itemView.findViewById(R.id.clHealthCost);

            ivPic = itemView.findViewById(R.id.ivHealth);

            flAnimCooldown = itemView.findViewById(R.id.flHealthAnim);
        }
    }
}
