package com.xlab13.playhacker.adapters.shop;

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
import com.example.GetHardwareQuery;
import com.example.UpdateHardwareMutation;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.Animations;

import java.util.List;

public class HardwareAdapter extends RecyclerView.Adapter<HardwareAdapter.ShopViewHolder> {
    Context context;

    private List<GetHardwareQuery.GetHardware> items;

    public HardwareAdapter(Context context, List<GetHardwareQuery.GetHardware> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hardware, parent, false);
        ShopViewHolder holder = new ShopViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        GetHardwareQuery.GetHardware item = items.get(position);

        holder.tvTitle.setText(item.title());
        holder.tvText.setText(item.description());
        holder.tvLevel.setText(item.level()+"");


        if (item.level() == 10){
            holder.clHardCost.setVisibility(View.INVISIBLE);
        } else {
            holder.tvCost.setText(mTools.reduceNumber(item.next_price_rub()));

            switch (item.status()){
                case "available":
                    holder.clHardCost.setBackground(context.getDrawable(R.drawable.style_button_light));
                    holder.clHardCost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isCooldown) return;
                            mTools.playSound();
                            Animations.animateCooldown(holder.flAnimCooldown, context);

                            ApolloClient client = NetworkService.getInstance().getGameClientWithToken();

                            UpdateHardwareMutation updateHardwareMutation = UpdateHardwareMutation.builder()
                                    .typeHardware(item.type())
                                    .build();

                            client.mutate(updateHardwareMutation)
                                    .enqueue(new ApolloCall.Callback<UpdateHardwareMutation.Data>() {
                                        @Override
                                        public void onResponse(@NonNull Response<UpdateHardwareMutation.Data> response) {
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
                    holder.clHardCost.setBackground(context.getDrawable(R.drawable.style_button_dark));
                    holder.clHardCost.setOnClickListener((v -> {
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
                    holder.clHardCost.setBackground(context.getDrawable(R.drawable.style_button_red));
                    holder.clHardCost.setOnClickListener((v -> {
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

    public void updateItems(List<GetHardwareQuery.GetHardware> items, List<Integer> updatedItems){
        this.items = items;

        for (Integer index : updatedItems) this.notifyItemChanged(index);
    }

    public void updateAllItems(List<GetHardwareQuery.GetHardware> items){
        this.items = items;
        this.notifyDataSetChanged();
    }

    public List<GetHardwareQuery.GetHardware> getItems(){
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ShopViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvText, tvCost, tvLevel;
        ImageView ivPic;
        ConstraintLayout clCont, clHardCost;
        FrameLayout flAnimCooldown;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHardTitle);
            tvText = itemView.findViewById(R.id.tvHardText);
            tvCost = itemView.findViewById(R.id.tvHardCost);
            tvLevel = itemView.findViewById(R.id.tvHardLevel);

            clCont = itemView.findViewById(R.id.clHardCont);
            clHardCost = itemView.findViewById(R.id.clHardCost);

            ivPic = itemView.findViewById(R.id.ivHardPic);

            flAnimCooldown = itemView.findViewById(R.id.flHardAnim);
        }

    }
}
