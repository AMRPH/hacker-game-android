package com.xlab13.playhacker.activities.chat;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetDMQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.chat.ChatsAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatsActivity extends Activity {
    Context context;


    @BindView(R.id.rvChats)
    RecyclerView rvChats;


    LoadingAlertDialog loadDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
        context = this;

        rvChats.setLayoutManager(new LinearLayoutManager(this));

        loadDialog = new LoadingAlertDialog(this);

        getChats();
    }

    @OnClick(R.id.btnChatsClose)
    public void onCloseClick(View v) {
        mTools.playSound();
        finish();
    }

    private void getChats(){
        loadDialog.showDialog();
        GetDMQuery getDMQuery = GetDMQuery.builder().userIdWith(null).build();

        NetworkService.getInstance().getGameClientWithToken()
                .query(getDMQuery).enqueue(new ApolloCall.Callback<GetDMQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetDMQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvChats.setAdapter(new ChatsAdapter(context, response.getData().getDM()));
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
}
