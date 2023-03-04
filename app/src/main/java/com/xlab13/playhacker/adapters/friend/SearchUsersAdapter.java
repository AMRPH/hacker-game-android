package com.xlab13.playhacker.adapters.friend;

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
import com.example.RequestFriendshipMutation;
import com.example.SearchUsersQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.SearchViewHolder> {
    Context context;

    private List<SearchUsersQuery.SearchUser> items;

    public SearchUsersAdapter(Context context, List<SearchUsersQuery.SearchUser> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);

        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchUsersQuery.SearchUser item = items.get(position);

        holder.tvName.setText(item.username());
        holder.tvLevel.setText(item.level() + " lvl");


        if (item.is_friend_or_requested_friendship()){
            holder.btnAddFriend.setVisibility(View.INVISIBLE);
        } else {
            holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mTools.playSound();
                    RequestFriendshipMutation requestFriendshipMutation = RequestFriendshipMutation
                            .builder()
                            .id(item.user_id())
                            .build();

                    NetworkService.getInstance().getGameClientWithToken()
                            .mutate(requestFriendshipMutation).enqueue(new ApolloCall.Callback<RequestFriendshipMutation.Data>() {
                        @Override
                        public void onResponse(@NonNull Response<RequestFriendshipMutation.Data> response) {
                            if (!response.hasErrors()){
                                ((Activity) context).runOnUiThread(() -> {
                                    holder.btnAddFriend.setVisibility(View.INVISIBLE);
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
            });
        }


        Picasso.with(context)
                .load(NetworkService.avatarUrl + item.username())
                .error(R.drawable.icon_2048)
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


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvLevel;
        ImageView ivAvatar, btnAddFriend;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvLevel = itemView.findViewById(R.id.tvUserLevel);

            btnAddFriend = itemView.findViewById(R.id.btnUserAddFriend);

            ivAvatar = itemView.findViewById(R.id.ivUserAvatar);
        }
    }
}