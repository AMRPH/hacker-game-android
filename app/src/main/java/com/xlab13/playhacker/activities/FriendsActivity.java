package com.xlab13.playhacker.activities;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetFriendsQuery;
import com.example.GetFriendshipRequestsQuery;
import com.example.GetUsersQuery;
import com.example.SearchUsersQuery;
import com.xlab13.playhacker.adapters.friend.SearchUsersAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.adapters.friend.FriendsAdapter;
import com.xlab13.playhacker.adapters.friend.FriendRequestAdapter;
import com.xlab13.playhacker.adapters.friend.UsersAdapter;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendsActivity extends Activity {
    Context context;

    private ApolloClient client;


    @BindView(R.id.btnFriends)
    Button btnFriend;

    @BindView(R.id.btnRequests)
    Button btnRequests;

    @BindView(R.id.btnSearch)
    Button btnSearch;


    @BindView(R.id.etSearchLine)
    EditText etSearchLine;


    @BindView(R.id.rvFriends)
    RecyclerView rvFriend;


    LoadingAlertDialog loadDialog;

    private SearchTextWatcher searchTextWatcher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);
        context = this;

        client = NetworkService.getInstance().getGameClientWithToken();

        loadDialog = new LoadingAlertDialog(this);

        rvFriend.setLayoutManager(new LinearLayoutManager(this));

        hideSearchLine();

        getFriends();

        searchTextWatcher = new SearchTextWatcher();
    }

    @OnClick(R.id.btnFriendsClose)
    public void onCloseClick(View v) {
        mTools.playSound();
        mTools.closeKeyboard(context);
        finish();
    }

    @OnClick(R.id.btnFriends)
    public void onFriendClick(View v) {
        mTools.playSound();
        rvFriend.setAdapter(null);

        btnFriend.setBackground(getDrawable(R.drawable.style_button_dark));
        btnRequests.setBackground(getDrawable(R.drawable.style_button_light));
        btnSearch.setBackground(getDrawable(R.drawable.style_button_light));
        hideSearchLine();

        getFriends();
    }

    @OnClick(R.id.btnRequests)
    public void onRequestsClick(View v) {
        mTools.playSound();
        rvFriend.setAdapter(null);
        mTools.closeKeyboard(context);

        btnRequests.setBackground(getDrawable(R.drawable.style_button_dark));
        btnFriend.setBackground(getDrawable(R.drawable.style_button_light));
        btnSearch.setBackground(getDrawable(R.drawable.style_button_light));
        hideSearchLine();

        getRequests();
    }

    @OnClick(R.id.btnSearch)
    public void onSearchClick(View v) {
        mTools.playSound();
        rvFriend.setAdapter(null);

        btnSearch.setBackground(getDrawable(R.drawable.style_button_dark));
        btnFriend.setBackground(getDrawable(R.drawable.style_button_light));
        btnRequests.setBackground(getDrawable(R.drawable.style_button_light));
        showSearchLine();

        getUsers();
    }

    private void getFriends(){
        loadDialog.showDialog();
        GetFriendsQuery getFriendsQuery = GetFriendsQuery.builder().build();

        client.query(getFriendsQuery).enqueue(new ApolloCall.Callback<GetFriendsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetFriendsQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvFriend.setAdapter(new FriendsAdapter(context, response.getData().getFriends()));
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

    private void getRequests(){
        loadDialog.showDialog();
        GetFriendshipRequestsQuery getFriendshipRequestsQuery = GetFriendshipRequestsQuery.builder().build();

        client.query(getFriendshipRequestsQuery).enqueue(new ApolloCall.Callback<GetFriendshipRequestsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetFriendshipRequestsQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvFriend.setAdapter(new FriendRequestAdapter(context, response.getData().getFriendshipRequests()));
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

    private void getUsers(){
        etSearchLine.addTextChangedListener(searchTextWatcher);
        loadDialog.showDialog();
        GetUsersQuery getUsersQuery = GetUsersQuery.builder().build();

        client.query(getUsersQuery).enqueue(new ApolloCall.Callback<GetUsersQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetUsersQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        rvFriend.setAdapter(new UsersAdapter(context, response.getData().getUsers()));
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


    private void hideSearchLine(){
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, 0);
        lp.topToBottom = R.id.btnFriends;
        lp.endToEnd = 0;
        lp.startToStart = 0;
        final float scale = getResources().getDisplayMetrics().density;
        int margin = (int) (5 * scale + 0.5f);
        lp.setMargins(margin, margin, margin, margin);
        etSearchLine.setLayoutParams(lp);
    }

    private void showSearchLine(){
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topToBottom = R.id.btnFriends;
        lp.endToEnd = 0;
        lp.startToStart = 0;
        final float scale = getResources().getDisplayMetrics().density;
        int margin = (int) (5 * scale + 0.5f);
        lp.setMargins(margin, margin, margin, margin);
        etSearchLine.setLayoutParams(lp);
    }


    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence str, int start, int before, int count) {
            if (!str.equals("")) {

                SearchUsersQuery searchUsersQuery = SearchUsersQuery.builder().query(String.valueOf(str)).build();

                client.query(searchUsersQuery).enqueue(new ApolloCall.Callback<SearchUsersQuery.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<SearchUsersQuery.Data> response) {
                        if (!response.hasErrors()){
                            runOnUiThread(() -> {
                                rvFriend.setAdapter(new SearchUsersAdapter(context, response.getData().searchUsers()));
                                loadDialog.dismissDialog();
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
                getUsers();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
