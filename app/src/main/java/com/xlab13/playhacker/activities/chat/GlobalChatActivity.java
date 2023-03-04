package com.xlab13.playhacker.activities.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.AddMessageMutation;
import com.example.MessageAddedSubscription;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.chat.GlobalChatAdapter;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.ChatItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xlab13.playhacker.Config.globalChat;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import java.util.List;

public class GlobalChatActivity extends Activity {
    Context context;

    private ApolloClient client;


    @BindView(R.id.edGlobalChat)
    EditText edChat;

    @BindView(R.id.rvGlobalChat)
    RecyclerView rvChat;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_chat);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        rvChat.setLayoutManager(new LinearLayoutManager(this));

        rvChat.setAdapter(new GlobalChatAdapter(context, Config.globalChat));
        rvChat.scrollToPosition(rvChat.getAdapter().getItemCount()-1);

        subscribeGlobalChat();
    }


    @OnClick(R.id.btnGlobalChatClose)
    void onCloseClick(View v){
        mTools.playSound();
        finish();
    }


    @OnClick(R.id.btnGlobalSend)
    void onSendClick(View v){
        mTools.playSound();
        mTools.closeKeyboard(context);

        if (!edChat.getText().toString().replace(" ", "").isEmpty()){
            String message = edChat.getText().toString();
            edChat.setText("");

            AddMessageMutation addMessageMutation = AddMessageMutation.builder().message(message).build();

            client.mutate(addMessageMutation).enqueue(new ApolloCall.Callback<AddMessageMutation.Data>() {
                @Override
                public void onResponse(@NonNull Response<AddMessageMutation.Data> response) {
                    mTools.debug("chat onResponse");
                    if (response.hasErrors()) {
                        runOnUiThread(() -> {
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
    }

    @Override
    protected void onResume() {
        mTools.playMusic();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mTools.stopMusic();
        super.onPause();
    }

    public void subscribeGlobalChat(){
        client.subscribe(new MessageAddedSubscription()).execute(new ApolloSubscriptionCall.Callback<MessageAddedSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<MessageAddedSubscription.Data> response) {
                if (!response.hasErrors()){
                    List<MessageAddedSubscription.MessageAdded> messages = response.getData().messageAdded();

                    globalChat.clear();
                    for (int i = messages.size()-1; i >= 0 ; i--){
                        ChatItem message = new ChatItem();

                        message.id = messages.get(i).id();
                        message.sent_at = messages.get(i).sent_at().longValue();
                        message.user_id = messages.get(i).user_id();
                        message.username = messages.get(i).username();
                        message.clan_id = messages.get(i).clan_id();
                        message.clan = messages.get(i).clan();
                        message.message = messages.get(i).message();

                        globalChat.add(message);
                    }


                    runOnUiThread(() -> {
                        if (!globalChat.isEmpty()){
                            GlobalChatAdapter chatAdapter = (GlobalChatAdapter) rvChat.getAdapter();
                            chatAdapter.updateAdapter(globalChat);
                            rvChat.scrollToPosition(chatAdapter.getItemCount()-1);
                        }
                    });

                } else {
                    runOnUiThread(() -> {
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

            @Override
            public void onCompleted() {
            }

            @Override
            public void onTerminated() {
            }

            @Override
            public void onConnected() {
            }
        });
    }
}