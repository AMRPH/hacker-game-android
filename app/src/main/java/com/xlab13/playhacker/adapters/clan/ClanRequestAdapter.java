package com.xlab13.playhacker.adapters.clan;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.ApproveClanMembershipRequestMutation;
import com.example.GetClanMembershipRequestsQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

public class ClanRequestAdapter extends RecyclerView.Adapter<ClanRequestAdapter.RequestViewHolder> {
    Context context;

    private List<GetClanMembershipRequestsQuery.GetClanMembershipRequest> items;

    public ClanRequestAdapter(Context context, List<GetClanMembershipRequestsQuery.GetClanMembershipRequest> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        GetClanMembershipRequestsQuery.GetClanMembershipRequest item = items.get(position);

        holder.tvName.setText(item.username());
        holder.tvLevel.setText(item.level() + " lvl");

        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();
                approveRequest(holder, item.user_id(), true);
            }
        });

        holder.btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();
                approveRequest(holder, item.user_id(), false);
            }
        });

        Picasso.with(context)
                .load(NetworkService.avatarUrl + item.username())
                .error(R.drawable.icon_avatar)
                .placeholder(R.drawable.icon_avatar)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        holder.ivAvatar.setImageBitmap(mTools.getRoundedCornerBitmap(bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        holder.ivAvatar.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        holder.ivAvatar.setImageDrawable(placeHolderDrawable);
                    }
                });
    }

    private void approveRequest(RequestViewHolder holder, int id, boolean approve){
        ApproveClanMembershipRequestMutation requestMutation = ApproveClanMembershipRequestMutation
                .builder()
                .id(id)
                .approve(approve)
                .build();

        NetworkService.getInstance().getGameClientWithToken()
                .mutate(requestMutation).enqueue(new ApolloCall.Callback<ApproveClanMembershipRequestMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<ApproveClanMembershipRequestMutation.Data> response) {
                if (!response.hasErrors()){
                    ((Activity) context).runOnUiThread(() -> {
                        holder.btnAdd.setVisibility(View.INVISIBLE);
                        holder.btnDecline.setVisibility(View.INVISIBLE);
                    });

                } else {
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


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvLevel;
        ImageView ivAvatar, btnAdd, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRequestName);
            tvLevel = itemView.findViewById(R.id.tvRequestLevel);

            btnAdd = itemView.findViewById(R.id.btnRequestAdd);
            btnDecline = itemView.findViewById(R.id.btnRequestDecline);

            ivAvatar = itemView.findViewById(R.id.ivRequestAvatar);
        }
    }
}