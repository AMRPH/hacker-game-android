package com.xlab13.playhacker.adapters.clan;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GetClanMembersQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.chat.ChatActivity;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

public class ClanMembersAdapter extends RecyclerView.Adapter<ClanMembersAdapter.MembersViewHolder> {
    Context context;

    private List<GetClanMembersQuery.GetClanMember> items;

    public ClanMembersAdapter(Context context, List<GetClanMembersQuery.GetClanMember> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new MembersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        GetClanMembersQuery.GetClanMember item = items.get(position);

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


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MembersViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvLevel;
        ImageView ivAvatar, btnChat;
        ConstraintLayout clCont;

        public MembersViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFriendName);
            tvLevel = itemView.findViewById(R.id.tvFriendLevel);

            btnChat = itemView.findViewById(R.id.btnFriendChat);

            ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);

            clCont = itemView.findViewById(R.id.clFriend);
        }
    }
}