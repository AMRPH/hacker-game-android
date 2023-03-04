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

import com.example.DmAddedSubscription;
import com.example.GetDMQuery;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    Context context;

    private final int MY_MESSAGE = 1;
    private final int OTHERS_MESSAGE = 0;

    private List<GetDMQuery.GetDM> messages;

    public ChatAdapter(Context context, List<GetDMQuery.GetDM> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType){
            case MY_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_my, parent, false);
                break;
            case OTHERS_MESSAGE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_others, parent, false);
                break;
        }

        v.setOnClickListener((v1 -> {
            mTools.closeKeyboard(context);
        }));

        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        GetDMQuery.GetDM message = messages.get(position);

        holder.tvName.setText(message.username_from());
        holder.tvText.setText(message.message());

        holder.tvTime.setText(getTime(message.sent_at().longValue()));
    }

    public void updateAdapter(List<DmAddedSubscription.DmAdded> newMessages){
        messages.clear();

        for (DmAddedSubscription.DmAdded item : newMessages){
            GetDMQuery.GetDM newItem = new GetDMQuery.GetDM(
                    "DM",
                    item.user_id(),
                    item.username(),
                    item.sent_at(),
                    item.user_id_from(),
                    item.username_from(),
                    item.message()
            );

            messages.add(newItem);
        }

        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).user_id_from() == Config.user.user_id) {
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

        TextView tvName, tvTime, tvText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvTime = itemView.findViewById(R.id.tvChatTime);
            tvText = itemView.findViewById(R.id.tvChatText);
        }
    }
}
