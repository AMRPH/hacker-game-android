package com.xlab13.playhacker.adapters.shop;

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
import com.example.GetSoftwareQuery;
import com.example.UpdateSoftwareMutation;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.List;

import static com.xlab13.playhacker.Config.isCooldown;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class SoftwareAdapter extends RecyclerView.Adapter<SoftwareAdapter.ShopViewHolder> {
    Context context;

    private List<GetSoftwareQuery.GetSoftware> items;

    public SoftwareAdapter(Context context, List<GetSoftwareQuery.GetSoftware> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_software, parent, false);
        ShopViewHolder holder = new ShopViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        GetSoftwareQuery.GetSoftware item = items.get(position);
        holder.tvTitle.setText(item.title());
        holder.tvText.setText(item.short_description());

        MyAlertDialog descriptionDialog = new MyAlertDialog(context);
        descriptionDialog.setText(item.description());
        descriptionDialog.addButton("Закрыть", (v) ->{
            descriptionDialog.dismissDialog();
        });

        holder.clCont.setOnClickListener((v) -> descriptionDialog.showDialog());


        holder.tvCost.setText(mTools.reduceNumber(item.price_rub()));

        if (item.purchased()){
            holder.clSoftCost.setVisibility(View.INVISIBLE);
        } else {
            switch (item.status()){
                case "available":
                    holder.clSoftCost.setBackground(context.getDrawable(R.drawable.style_button_light));
                    holder.clSoftCost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(holder.flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            UpdateSoftwareMutation updateSoftwareMutation = UpdateSoftwareMutation.builder()
                                    .id(item.id())
                                    .build();

                            client.mutate(updateSoftwareMutation)
                                    .enqueue(new ApolloCall.Callback<UpdateSoftwareMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<UpdateSoftwareMutation.Data> response) {
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
                    holder.clSoftCost.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    holder.clSoftCost.setOnClickListener((v -> {
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
                    holder.clSoftCost.setBackground(context.getDrawable(R.drawable.style_button_red));
                    holder.clSoftCost.setOnClickListener((v -> {
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
        }

        SvgLoader.pluck()
                .with((Activity) context)
                .setPlaceHolder(R.drawable.icon_no_image, R.drawable.icon_no_image)
                .load(NetworkService.imgUrl + item.image_path(), holder.ivPic);
    }

    public void updateItems(List<GetSoftwareQuery.GetSoftware> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetSoftwareQuery.GetSoftware> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetSoftwareQuery.GetSoftware> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ShopViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvText, tvCost;
        ImageView ivPic;
        ConstraintLayout clCont, clSoftCost;
        FrameLayout flAnimCooldown;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSoftTitle);
            tvText = itemView.findViewById(R.id.tvSoftText);
            tvCost = itemView.findViewById(R.id.tvSoftCost);

            clCont = itemView.findViewById(R.id.clSoftCont);
            clSoftCost = itemView.findViewById(R.id.clSoftCost);

            ivPic = itemView.findViewById(R.id.ivSoftPic);

            flAnimCooldown = itemView.findViewById(R.id.flSoftAnim);
        }

    }
}