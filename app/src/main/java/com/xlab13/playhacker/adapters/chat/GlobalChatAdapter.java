package com.xlab13.playhacker.adapters.chat;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.utils.ChatItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GlobalChatAdapter extends RecyclerView.Adapter<GlobalChatAdapter.ChatViewHolder> {
    Context context;

    private final int MY_MESSAGE = 1;
    private final int OTHERS_MESSAGE = 0;

    private List<ChatItem> messages;

    public GlobalChatAdapter(Context context, List<ChatItem> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType){
            case MY_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_global_chat_my, parent, false);
                break;
            case OTHERS_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_global_chat_others, parent, false);
                break;
        }
        v.setOnClickListener((v1 -> {
            mTools.closeKeyboard(context);
        }));

        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem message = messages.get(position);

        holder.tvName.setText(message.username);

        holder.tvClan.setText(message.clan);

        holder.tvText.setText(message.message);
        holder.tvTime.setText(getTime(message.sent_at));
    }

    public void updateAdapter(List<ChatItem> newMessages){
        messages = newMessages;

        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).user_id == Config.user.user_id) {
            return MY_MESSAGE;
        }
        return OTHERS_MESSAGE;
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String getTime(long utcTime){
        Date time = new Date(utcTime);

        DateFormat dateUTCFormat = new SimpleDateFormat("HH:mm");
        dateUTCFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateUTCFormat.format(time);
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvTime, tvText, tvClan;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGlobalChatName);
            tvClan = itemView.findViewById(R.id.tvGlobalChatClan);
            tvTime = itemView.findViewById(R.id.tvGlobalChatTime);
            tvText = itemView.findViewById(R.id.tvGlobalChatText);
        }
    }
}
