package com.xlab13.playhacker.activities.chat;

import static com.xlab13.playhacker.Config.globalChat;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.DmAddedSubscription;
import com.example.GetDMQuery;
import com.example.SendDMMutation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.chat.ChatAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatActivity extends Activity {
    Context context;

    private ApolloClient client;

    private String username;
    private int id;


    @BindView(R.id.ivChatAvatar)
    ImageView ivChatAvatar;

    @BindView(R.id.tvChatUsername)
    TextView tvChatUsername;


    @BindView(R.id.btnChatSend)
    Button btnChatSend;

    @BindView(R.id.edChatChat)
    EditText edChat;


    @BindView(R.id.rvChat)
    RecyclerView rvChat;


    LoadingAlertDialog loadDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        context = this;

        username = getIntent().getStringExtra("username");
        id = getIntent().getIntExtra("id", 0);

        client = NetworkService.getInstance().getGameClientWithToken();

        rvChat.setLayoutManager(new LinearLayoutManager(this));

        loadDialog = new LoadingAlertDialog(this);

        getChat();

        subscribeChat();
    }

    @OnClick(R.id.btnChatClose)
    public void onCloseClick(View v) {
        mTools.playSound();
        mTools.closeKeyboard(context);
        finish();
    }

    @OnClick(R.id.btnChatSend)
    void onSendClick(View v){
        mTools.playSound();
        mTools.closeKeyboard(context);

        if (!edChat.getText().toString().replace(" ", "").isEmpty()){
            String message = edChat.getText().toString();
            edChat.setText("");

            SendDMMutation sendDMMutation = SendDMMutation.builder()
                    .id(id)
                    .message(message)
                    .build();

            client.mutate(sendDMMutation).enqueue(new ApolloCall.Callback<SendDMMutation.Data>() {
                @Override
                public void onResponse(@NonNull Response<SendDMMutation.Data> response) {
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

    private void getChat(){
        loadDialog.showDialog();
        GetDMQuery getDMQuery = GetDMQuery.builder().userIdWith(id).build();

        NetworkService.getInstance().getGameClientWithToken()
                .query(getDMQuery).enqueue(new ApolloCall.Callback<GetDMQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetDMQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        tvChatUsername.setText(username);

                        Picasso.with(context)
                                .load(NetworkService.avatarUrl + username)
                                .error(R.drawable.icon_avatar)
                                .placeholder(R.drawable.icon_avatar)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        ivChatAvatar.setImageBitmap(mTools.getRoundedCornerBitmap(bitmap));
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        ivChatAvatar.setImageDrawable(errorDrawable);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                        ivChatAvatar.setImageDrawable(placeHolderDrawable);
                                    }
                                });

                        rvChat.setAdapter(new ChatAdapter(context, reverse(response.getData().getDM())));
                        rvChat.scrollToPosition(rvChat.getAdapter().getItemCount()-1);
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
        });
    }


    public void subscribeChat(){
        client.subscribe(new DmAddedSubscription()).execute(new ApolloSubscriptionCall.Callback<DmAddedSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<DmAddedSubscription.Data> response) {
                if (!response.hasErrors()){
                    List<DmAddedSubscription.DmAdded> messages = new ArrayList<>();

                    for (DmAddedSubscription.DmAdded item : response.getData().dmAdded()){
                        if (item.user_id() == id) messages.add(item);
                    }

                    runOnUiThread(() -> {
                        if (!globalChat.isEmpty()){
                            ChatAdapter chatAdapter = (ChatAdapter) rvChat.getAdapter();
                            chatAdapter.updateAdapter(reverse(messages));
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

    static <T> List<T> reverse(final List<T> list) {
        final List<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return result;
    }
}
