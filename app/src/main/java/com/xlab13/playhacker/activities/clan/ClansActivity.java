package com.xlab13.playhacker.activities.clan;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetClansQuery;
import com.example.SearchClansQuery;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.clan.ClansAdapter;
import com.xlab13.playhacker.adapters.clan.SearchClansAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClansActivity extends Activity {
    Context context;

    private ApolloClient client;


    @BindView(R.id.etClans)
    EditText etClans;


    @BindView(R.id.rvClans)
    RecyclerView rvClans;


    LoadingAlertDialog loadDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clans);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        loadDialog = new LoadingAlertDialog(this);

        rvClans.setLayoutManager(new LinearLayoutManager(this));

        etClans.addTextChangedListener(new SearchTextWatcher());

        getClans();
    }
    @OnClick(R.id.btnClansClose)
    public void onCloseClick(View v){
        mTools.playSound();
        mTools.closeKeyboard(context);
        finish();
    }

    private void getClans(){
        loadDialog.showDialog();

        GetClansQuery getClansQuery = GetClansQuery.builder().build();

        client.query(getClansQuery).enqueue(new ApolloCall.Callback<GetClansQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetClansQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvClans.setAdapter(new ClansAdapter(context, response.getData().getClans()));
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

    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence str, int start, int before, int count) {
            if (!str.equals("")) {

                SearchClansQuery searchClansQuery = SearchClansQuery.builder().query(String.valueOf(str)).build();

                client.query(searchClansQuery).enqueue(new ApolloCall.Callback<SearchClansQuery.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<SearchClansQuery.Data> response) {
                        loadDialog.dismissDialog();
                        if (!response.hasErrors()){
                            runOnUiThread(() -> {
                                rvClans.setAdapter(new SearchClansAdapter(context, response.getData().searchClans()));
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
            } else {
                getClans();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
