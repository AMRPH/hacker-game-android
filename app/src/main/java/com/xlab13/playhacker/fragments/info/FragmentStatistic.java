package com.xlab13.playhacker.fragments.info;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.GetStatsQuery;
import com.xlab13.playhacker.alerts.LoadingAlertDialog;
import com.xlab13.playhacker.alerts.MyAlertDialog;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.network.NetworkService;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;

public class FragmentStatistic extends Fragment {


    private ApolloClient client;


    @BindViews({R.id.tvTopAlco1, R.id.tvTopAlco2, R.id.tvTopAlco3, R.id.tvTopAlco4, R.id.tvTopAlco5})
    List<TextView> listViewAlco;

    @BindViews({R.id.tvTopLevel1, R.id.tvTopLevel2, R.id.tvTopLevel3, R.id.tvTopLevel4, R.id.tvTopLevel5})
    List<TextView> listViewLevel;


    LoadingAlertDialog loadDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statistic, null);
        ButterKnife.bind(this, v);

        client = NetworkService.getInstance().getGameClientWithToken();

        loadDialog = new LoadingAlertDialog(getContext());
        loadDialog.showDialog();

        GetStatsQuery getStatsQuery = GetStatsQuery.builder().build();

        client.query(getStatsQuery).enqueue(new ApolloCall.Callback<GetStatsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetStatsQuery.Data> response) {
                loadDialog.dismissDialog();
                if (!response.hasErrors()){
                    List<GetStatsQuery.Level> levels = response.getData().getStats().level();
                    List<GetStatsQuery.Alco> alcos = response.getData().getStats().alco();

                    getActivity().runOnUiThread(() -> {
                        for (int i = 0; i < 5; i++){
                            GetStatsQuery.Level level = levels.get(i);
                            listViewLevel.get(i).setText(level.username() + ": " + level.level());


                            GetStatsQuery.Alco alco = alcos.get(i);
                            listViewAlco.get(i).setText(alco.username() + ": " + alco.alco_liters_use());
                        }
                    });

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

        return v;
    }
}
