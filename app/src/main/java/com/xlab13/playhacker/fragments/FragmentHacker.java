package com.xlab13.playhacker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.MessageAddedSubscription;
import com.xlab13.playhacker.alerts.GiftAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.activities.chat.GlobalChatActivity;
import com.xlab13.playhacker.alerts.ExchangerAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.utils.ChatItem;

import static com.xlab13.playhacker.Config.globalChat;
import static com.xlab13.playhacker.Config.isGiftReceived;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;
import static com.xlab13.playhacker.Config.PLAYER_AVATAR;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentHacker extends Fragment {


    private ApolloClient client;


    @BindView(R.id.tvGlobalChat)
    TextView tvGlobalChat;

    @BindView(R.id.btnGift)
    ImageView btnGift;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_hacker, null);
        ButterKnife.bind(this, v);

        client = NetworkService.getInstance().getGameClientWithToken();

        if (!globalChat.isEmpty()){
            ChatItem lastMessage = globalChat.get(globalChat.size()-1);
            tvGlobalChat.setText(lastMessage.username + ": " + lastMessage.message);
        }

        if (!isGiftReceived){
            btnGift.setVisibility(View.VISIBLE);
        }

        subscribeGlobalChat();

        return v;
    }

    @OnClick(R.id.btnGlobalChat)
    public void onChatClick(View v){
        mTools.blockButton(v);
        mTools.playSound();
        startActivity(new Intent(getContext(), GlobalChatActivity.class));
    }

    @OnClick(R.id.btnExchanger)
    public void onExchangeClick(View v){
        mTools.blockButton(v);
        mTools.playSound();
        (new ExchangerAlertDialog(getContext())).showDialog();
    }

    @OnClick(R.id.btnGift)
    public void onGiftClick(View v){
        if (!isGiftReceived){
            mTools.blockButton(v);
            mTools.playSound();
            new GiftAlertDialog(getContext()).showDialog();
            btnGift.setVisibility(View.INVISIBLE);
        }
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

                    if (!globalChat.isEmpty()){
                        ChatItem lastMessage = globalChat.get(globalChat.size()-1);

                        getActivity().runOnUiThread(() -> {
                            tvGlobalChat.setText(lastMessage.username + ": " + lastMessage.message);
                        });
                    }


                } else {
                    getActivity().runOnUiThread(() -> {
                        String error = response.getErrors().get(0).getMessage();

                        mTools.showErrorDialog(getContext(), error);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("MessageAddedSubscription");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(getContext());
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

    /*

    @OnClick(R.id.btnMap)
    public void onMapClick(View v){
        mTools.playSound();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else startActivity(new Intent(getContext(), MapActivity.class));
    }
     */


}
