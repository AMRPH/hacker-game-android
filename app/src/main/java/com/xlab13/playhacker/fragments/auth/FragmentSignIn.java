package com.xlab13.playhacker.fragments.auth;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.SignInQuery;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.InitializationActivity;
import com.xlab13.playhacker.network.NetworkService;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentSignIn extends Fragment {

    @BindView(R.id.etLogin)
    EditText etLogin;

    @BindView(R.id.etPassword)
    EditText etPassword;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_sign_in, null);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.btnSignIn)
    public void onClick(View v){
        mTools.playSound();
        String username = etLogin.getText().toString();
        String password = etPassword.getText().toString();

        signIn(username, password);
    }


    private void signIn(String username, String password) {
        ApolloClient authClient = NetworkService.getInstance().getAuthClient();

        SignInQuery signInQuery = SignInQuery.builder()
                .username(username)
                .password(password)
                .build();

        authClient.query(signInQuery).enqueue(new ApolloCall.Callback<SignInQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<SignInQuery.Data> response) {
                if (!response.hasErrors()) {
                    NetworkService.getInstance().setBearerToken(response.getData().signIn().token());


                    SharedPreferences sPref = getContext().getSharedPreferences("game", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("username", username);
                    ed.putString("password", password);
                    ed.commit();

                    startActivity(new Intent(getContext(), InitializationActivity.class));
                    getActivity().finish();
                } else {
                    getActivity().runOnUiThread(() -> {
                        String error = response.getErrors().get(0).getMessage();

                        mTools.showErrorDialog(getContext(), error);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.startErrorConnectionActivity(getContext());
            }
        });
    }
}
