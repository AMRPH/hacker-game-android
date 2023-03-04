package com.xlab13.playhacker.adapters.friend;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetFriendsQuery;
import com.example.RemoveFromFriendsMutation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.chat.ChatActivity;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    Context context;

    private List<GetFriendsQuery.GetFriend> items;

    public FriendsAdapter(Context context, List<GetFriendsQuery.GetFriend> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        v.setOnClickListener((v1 -> {
            mTools.closeKeyboard(context);
        }));
        return new FriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        GetFriendsQuery.GetFriend item = items.get(position);

        holder.tvName.setText(item.username());
        holder.tvLevel.setText(item.level() + " lvl");

        holder.btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("username", item.username());
                intent.putExtra("id", item.user_id());

                context.startActivity(intent);
            }
        });

        holder.btnKick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();

                removeFromFriend(holder, item.user_id());
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

    private void removeFromFriend(FriendViewHolder holder, int id){
        RemoveFromFriendsMutation removeMutation = RemoveFromFriendsMutation.builder().friendId(id).build();

        NetworkService.getInstance().getGameClientWithToken()
                .mutate(removeMutation).enqueue(new ApolloCall.Callback<RemoveFromFriendsMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<RemoveFromFriendsMutation.Data> response) {
                if (!response.hasErrors()){
                    ((Activity) context).runOnUiThread(() -> {
                        holder.btnKick.setVisibility(View.INVISIBLE);
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

    public class FriendViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvLevel;
        ImageView ivAvatar, btnChat, btnKick;
        ConstraintLayout clCont;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFriendName);
            tvLevel = itemView.findViewById(R.id.tvFriendLevel);

            btnChat = itemView.findViewById(R.id.btnFriendChat);
            btnKick = itemView.findViewById(R.id.btnFriendKick);

            ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            clCont = itemView.findViewById(R.id.clFriend);
        }
    }
}