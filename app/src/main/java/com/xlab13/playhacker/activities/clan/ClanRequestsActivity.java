package com.xlab13.playhacker.activities.clan;

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
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetClanMembershipRequestsQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.clan.ClanRequestAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ClanRequestsActivity extends Activity {
    Context context;

    private ApolloClient client;


    @BindView(R.id.rvClanMemReq)
    RecyclerView rvClanMem;


    LoadingAlertDialog loadDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clan_members_and_request);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        loadDialog = new LoadingAlertDialog(this);

        rvClanMem.setLayoutManager(new LinearLayoutManager(this));

        getRequest();
    }

    private void getRequest(){
        loadDialog.showDialog();
        GetClanMembershipRequestsQuery getClanMembershipRequestsQuery = GetClanMembershipRequestsQuery.builder().build();

        client.query(getClanMembershipRequestsQuery).enqueue(new ApolloCall.Callback<GetClanMembershipRequestsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetClanMembershipRequestsQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvClanMem.setAdapter(new ClanRequestAdapter(context, response.getData().getClanMembershipRequests()));
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

    @OnClick(R.id.btnClanMemReqClose)
    public void onCloseClick(View v){
        mTools.playSound();
        finish();
    }
}


