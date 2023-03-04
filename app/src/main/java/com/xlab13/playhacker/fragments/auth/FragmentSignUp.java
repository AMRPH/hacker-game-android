package com.xlab13.playhacker.fragments.auth;

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
import com.example.SignUpMutation;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.activities.InitializationActivity;
import com.xlab13.playhacker.network.NetworkService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class FragmentSignUp extends Fragment {


    @BindView(R.id.etLogin)
    EditText etLogin;

    @BindView(R.id.etPassword)
    EditText etPassword;

    @BindView(R.id.etPasswordRepeat)
    EditText etPasswordRepeat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_sign_up, null);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.btnSignUp)
    public void onClick(View v){
        mTools.playSound();
        String username = etLogin.getText().toString().replace(" ", "");
        String password = etPassword.getText().toString().replace(" ", "");
        String passwordRepeat = etPasswordRepeat.getText().toString().replace(" ", "");

        if (!password.equals(passwordRepeat) ) {
            MyAlertDialog dialog = new MyAlertDialog(getContext());
            dialog.setText(String.valueOf(R.string.pass_dont_match));
            dialog.addButton(getString(R.string.close), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTools.playSound();
                    dialog.dismissDialog();
                }
            });
            dialog.showDialog();

        } else signUp(username, password);
    }

    private void signUp(String username, String password){
        ApolloClient authClient = NetworkService.getInstance().getAuthClient();


        SignUpMutation signUpMutation = SignUpMutation.builder()
                .username(username)
                .password(password)
                .build();

        authClient.mutate(signUpMutation)
                .enqueue(new ApolloCall.Callback<SignUpMutation.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<SignUpMutation.Data> response) {
                        if (!response.hasErrors()){
                            NetworkService.getInstance().setBearerToken(response.getData().signUp().token());


                            SharedPreferences sPref = getContext().getSharedPreferences("game", Context.MODE_PRIVATE);
                            SharedPreferences.Editor ed = sPref.edit();
                            ed.putString("username", username);
                            ed.putString("password", password);
                            ed.commit();

                            startActivity(new Intent(getContext(), InitializationActivity.class));
                            getActivity().finish();
                        } else {
                            String error = response.getErrors().get(0).getMessage();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    MyAlertDialog alertDialog = new MyAlertDialog(getContext());
                                    alertDialog.setText(error);
                                    alertDialog.addButton(getString(R.string.ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismissDialog();
                                        }
                                    });
                                    alertDialog.showDialog();
                                }
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
