package com.xlab13.playhacker.activities.clan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetMyClanQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xlab13.playhacker.Config.user;
import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class ClanActivity extends Activity {
    Context context;

    private ApolloClient client;


    @BindView(R.id.ivClanAvatar)
    ImageView ivClanAvatar;

    @BindView(R.id.btnClanRequests)
    TextView btnClanRequest;


    @BindView(R.id.tvClanName)
    TextView tvClanName;

    @BindView(R.id.tvClanRating)
    TextView tvClanRating;

    @BindView(R.id.tvClanMembers)
    TextView tvClanMembers;


    LoadingAlertDialog loadDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clan);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        loadDialog = new LoadingAlertDialog(this);

        getMyClan();
    }

    private void getMyClan(){
        loadDialog.showDialog();

        GetMyClanQuery getMyClanQuery = GetMyClanQuery.builder().build();

        client.query(getMyClanQuery).enqueue(new ApolloCall.Callback<GetMyClanQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMyClanQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        GetMyClanQuery.GetMyClan clan = response.getData().getMyClan();

                        tvClanName.setText(clan.title());
                        tvClanRating.setText(getString(R.string.rating) + ": " + clan.rating());

                        tvClanMembers.setText(getString(R.string.members) + ": " + clan.members_qty());

                        if(user.user_id == clan.leader_id()){
                            btnClanRequest.setOnClickListener((v -> {
                                Intent intent = new Intent(context, ClanRequestsActivity.class);
                                startActivity(intent);
                            }));
                        } else {
                            btnClanRequest.setBackground(getDrawable(R.drawable.style_button_dark));
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
        });


    }

    @OnClick(R.id.btnClanClose)
    public void onCloseClick(View v){
        mTools.playSound();
        finish();
    }

    @OnClick(R.id.btnClanChat)
    public void onChatClick(View v){
        mTools.playSound();
        Intent intent = new Intent(this, ClanChatActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnClanLeave)
    public void onLeaveClick(View v){
        mTools.playSound();
        //TODO
        finish();
    }

    @OnClick(R.id.btnClanMembers)
    public void onMembersClick(View v){
        mTools.playSound();
        Intent intent = new Intent(this, ClanMembersActivity.class);
        startActivity(intent);
    }
}
