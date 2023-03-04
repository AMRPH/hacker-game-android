package com.xlab13.playhacker.games.game2048;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.MoveMutation;
import com.example.NewGameQuery;
import com.example.type.Direction;
import com.xlab13.playhacker.adapters.friend.FriendsAdapter;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;
import java.util.Objects;

public class Activity2048 extends AppCompatActivity {
    Context context;

     String sessionId;

     ApolloClient client;

    LoadingAlertDialog loadDialog;

    private View2048 view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        client = NetworkService.getInstance().get2048ClientWithToken();
        mTools.debug(NetworkService.getInstance().getBearerToken());

        loadDialog = new LoadingAlertDialog(this);

        newGame();
    }

    public void newGame(){
        loadDialog.showDialog();
        NewGameQuery newGameQuery = NewGameQuery.builder().build();

        client.query(newGameQuery).enqueue(new ApolloCall.Callback<NewGameQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<NewGameQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    runOnUiThread(() -> {
                        NewGameQuery.NewGame game = response.getData().newGame();

                        sessionId = game.sessionId();

                        view = new View2048(context);
                        view.newGame(game.field(), game.scores(), game.status());
                        setContentView(view);
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
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else {
            Direction dir;
            switch (keyCode){
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    dir = Direction.DOWN;
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    dir = Direction.UP;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    dir = Direction.LEFT;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    dir = Direction.RIGHT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + keyCode);
            }
            view.move(dir);
        }
        return super.onKeyDown(keyCode, event);
    }
}
