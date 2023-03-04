package com.xlab13.playhacker.adapters.chat;

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

import com.example.GetDMQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.chat.ChatActivity;
import com.xlab13.playhacker.network.NetworkService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
    Context context;

    private List<GetDMQuery.GetDM> items;

    public ChatsAdapter(Context context, List<GetDMQuery.GetDM> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);

        return new ChatsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        GetDMQuery.GetDM item = items.get(position);

        holder.tvName.setText(item.username());
        holder.tvTime.setText(getTime(item.sent_at().longValue()));

        holder.tvLastMessage.setText(item.username_from() + ": " + item.message());

        holder.clCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private String getTime(long utcTime){
        Date time = new Date(utcTime);

        DateFormat dateUTCFormat = new SimpleDateFormat("HH:mm");
        dateUTCFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateUTCFormat.format(time);
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvLastMessage, tvTime;
        ImageView ivAvatar;
        ConstraintLayout clCont;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDMName);
            tvLastMessage = itemView.findViewById(R.id.tvDMLastMessage);
            tvTime = itemView.findViewById(R.id.tvDMTime);

            ivAvatar = itemView.findViewById(R.id.ivDMAvatar);

            clCont = itemView.findViewById(R.id.clDM);
        }
    }
}