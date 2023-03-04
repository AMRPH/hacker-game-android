package com.xlab13.playhacker.activities;

import static com.xlab13.playhacker.Config.PLAYER_AVATAR;
import static com.xlab13.playhacker.Config.btc_rub;
import static com.xlab13.playhacker.Config.gifts;
import static com.xlab13.playhacker.Config.globalChat;
import static com.xlab13.playhacker.Config.isGiftReceived;
import static com.xlab13.playhacker.Config.mDataController;
import static com.xlab13.playhacker.Config.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.CarsSubscription;
import com.example.GetCarsQuery;
import com.example.GetGiftsQuery;
import com.example.GetGirlItemsQuery;
import com.example.GetHardwareQuery;
import com.example.GetHealthQuery;
import com.example.GetHousingQuery;
import com.example.GetJobsQuery;
import com.example.GetLongtermJobsQuery;
import com.example.GetMessagesQuery;
import com.example.GetNewsQuery;
import com.example.GetProfileQuery;
import com.example.GetQuoteQuery;
import com.example.GetSoftwareQuery;
import com.example.GirlsSubscription;
import com.example.HardwareSubscription;
import com.example.HealthSubscription;
import com.example.HousingSubscription;
import com.example.JobSubscription;
import com.example.ListenPGQuery;
import com.example.LongtermJobSubscription;
import com.example.NewsSubscription;
import com.example.SignInQuery;
import com.example.SoftwareSubscription;
import com.example.type.Symbol;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.xlab13.playhacker.Config;
import com.xlab13.playhacker.adapters.shop.HardwareAdapter;
import com.xlab13.playhacker.games.game2048.Activity2048;
import com.xlab13.playhacker.test.TestActivity;
import com.xlab13.playhacker.utils.ChatItem;
import com.xlab13.playhacker.utils.DataController;
import com.xlab13.playhacker.utils.GameItem;
import com.xlab13.playhacker.utils.Profile;
import com.xlab13.playhacker.network.NetworkService;
import com.xlab13.playhacker.R;
import com.xlab13.playhacker.services.MusicService;
import com.xlab13.playhacker.tools;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class InitializationActivity extends Activity {
    Context context;

    ApolloClient authClient;
    ApolloClient gameClient;

    public static tools mTools;

    private AppUpdateManager appUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_initialization);

        startService(new Intent(this, MusicService.class));

        authClient = NetworkService.newInstance().getAuthClient();

        mTools = tools.getInstance(getApplicationContext());

        mDataController = DataController.getDataController();

        checkUpdate();

        init();
    }

    private void init(){
        user = new Profile();
        globalChat = new ArrayList<>();
        btc_rub = 0;
        isGiftReceived = true;
        gifts = new ArrayList<>();

        SharedPreferences sPref = getSharedPreferences("game", Context.MODE_PRIVATE);
        String username = sPref.getString("username", "");
        String password = sPref.getString("password", "");

        if (username.isEmpty() || password.isEmpty()){
            startActivity(new Intent(context, AuthActivity.class));
        } else {
            SignInQuery signInQuery = SignInQuery.builder()
                    .username(username)
                    .password(password)
                    .build();

            authClient.query(signInQuery).enqueue(new ApolloCall.Callback<SignInQuery.Data>() {
                @Override
                public void onResponse(@NonNull Response<SignInQuery.Data> response) {
                    if (!response.hasErrors()){
                        NetworkService.getInstance().setBearerToken(response.getData().signIn().token());
                        gameClient = NetworkService.getInstance().getGameClientWithToken();

                        listenPG();

                        getAvatar(username);

                        getGifts();

                        getGlobalChat();

                        getQuote();

                        getHealth();
                        getAlco();
                        getMood();
                        subscribeHealth();

                        getJob();
                        getGames();
                        getJobLong();
                        subscribeJob();
                        subscribeJobLongterm();

                        getHardware();
                        getSoftware();
                        subscribeHardware();
                        subscribeSoftware();

                        getNews();
                        subscribeNews();

                        getHouse();
                        getCar();
                        getGirl();
                        subscribeHouse();
                        subscribeCar();
                        subscribeGirl();

                        getProfile();

                    } else {
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.remove("username");
                        ed.remove("password");
                        ed.commit();

                        startActivity(new Intent(context, AuthActivity.class));
                    }

                }

                @Override
                public void onFailure(@NonNull ApolloException e) {
                    mTools.debug("signInQuery");
                    mTools.debug(e.getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            });
        }
    }

    private void listenPG(){
        ListenPGQuery listenPGQuery = ListenPGQuery.builder().build();

        gameClient.query(listenPGQuery).enqueue(new ApolloCall.Callback<ListenPGQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<ListenPGQuery.Data> response) {
                if (response.hasErrors()){
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("listenPG");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getProfile(){
        GetProfileQuery getProfileQuery = GetProfileQuery.builder().build();

        gameClient.query(getProfileQuery).enqueue(new ApolloCall.Callback<GetProfileQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetProfileQuery.Data> response) {
                if (!response.hasErrors()){
                    GetProfileQuery.GetProfile profile = response.getData().getProfile();

                    user.user_id = profile.user_id();
                    user.username = profile.username();
                    user.alco_balance = profile.alco_balance();
                    user.alco_liters_use = profile.alco_liters_use();
                    user.car = profile.car();
                    user.clan_id = profile.clan_id();
                    user.clan = profile.clan();
                    user.experience_points = profile.experience_points();
                    user.next_xp = profile.next_xp();
                    user.friends = profile.friends();
                    user.friendship_requests = profile.friendship_requests();
                    user.girl_appearance = profile.girl_appearance();
                    user.girl_clothes = profile.girl_clothes();
                    user.girl_jewelry = profile.girl_jewelry();
                    user.girl_leisure = profile.girl_leisure();
                    user.girl_level = profile.girl_level();
                    user.girl_sport = profile.girl_sport();
                    user.health_points = profile.health_points();
                    user.house = profile.house();
                    user.installed_software = profile.installed_software();
                    if (profile.job_end() == null) user.job_end = null;
                    else user.job_end = profile.job_end().longValue();
                    user.level = profile.level();
                    user.money_btc = profile.money_btc();
                    user.money_rub = profile.money_rub();
                    user.mood = profile.mood();
                    user.moves = profile.moves();
                    user.hardware_level = profile.hardware_level();
                    user.pc_cooling = profile.pc_cooling();
                    user.pc_cpu = profile.pc_cpu();
                    user.pc_drive = profile.pc_drive();
                    user.pc_network = profile.pc_network();
                    user.pc_motherboard = profile.pc_motherboard();
                    user.pc_power = profile.pc_power();
                    user.pc_ram = profile.pc_ram();
                    user.pc_gpu = profile.pc_gpu();
                    user.rating = profile.rating();
                    user.read_news = profile.read_news();
                    user.work_hack_balance = profile.work_hack_balance();

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(context, MainActivity.class));
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getProfile");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getQuote(){
        GetQuoteQuery getQuoteQuery = GetQuoteQuery.builder().symbol(Symbol.BTCRUB).build();

        gameClient.query(getQuoteQuery).enqueue(new ApolloCall.Callback<GetQuoteQuery.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<GetQuoteQuery.Data> response) {
                        if (!response.hasErrors()){
                            btc_rub = Integer.parseInt(response.getData().getQuote().price());

                        } else {
                            mTools.startErrorConnectionActivity(context);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ApolloException e) {
                        mTools.debug("getQuote");
                        mTools.debug(e.getMessage());
                        mTools.startErrorConnectionActivity(context);
                    }
                });
    }

    private void getAvatar(String username){
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(NetworkService.avatarUrl + username)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                PLAYER_AVATAR = mTools.getRoundedCornerBitmap(mTools.resourcesToBitmap(R.drawable.icon_avatar, 300, 300));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                PLAYER_AVATAR = mTools.getRoundedCornerBitmap(bitmap);
            }
        });
    }

    private void getGlobalChat(){
        GetMessagesQuery getMessagesQuery = GetMessagesQuery.builder().build();

        gameClient.query(getMessagesQuery).enqueue(new ApolloCall.Callback<GetMessagesQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMessagesQuery.Data> response) {
                if (!response.hasErrors()){
                    List<GetMessagesQuery.GetMessage> messages = response.getData().getMessages();


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

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getGlobalChat");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getGifts(){
        GetGiftsQuery getGiftsQuery = GetGiftsQuery.builder().build();

        gameClient.query(getGiftsQuery).enqueue(new ApolloCall.Callback<GetGiftsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetGiftsQuery.Data> response) {
                if (!response.hasErrors()){
                    isGiftReceived = response.getData().getGifts().applied();

                    gifts = response.getData().getGifts().gifts();
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getGifts");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }


    private void getHealth(){
        GetHealthQuery getHealthQuery = GetHealthQuery.builder()
                .productType("health")
                .build();

        gameClient.query(getHealthQuery).enqueue(new ApolloCall.Callback<GetHealthQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetHealthQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setHealthItems(response.getData().getHealth());
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getHealth");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getAlco(){
        GetHealthQuery getHealthQuery = GetHealthQuery.builder()
                .productType("alcohol")
                .build();

        gameClient.query(getHealthQuery).enqueue(new ApolloCall.Callback<GetHealthQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetHealthQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setAlcoItems(response.getData().getHealth());
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getAlco");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getMood(){
        GetHealthQuery getHealthQuery = GetHealthQuery.builder()
                .productType("mood")
                .build();

        gameClient.query(getHealthQuery).enqueue(new ApolloCall.Callback<GetHealthQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetHealthQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setMoodItems(response.getData().getHealth());
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getMood");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void subscribeHealth(){
        gameClient.subscribe(new HealthSubscription()).execute(new ApolloSubscriptionCall.Callback<HealthSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<HealthSubscription.Data> response) {
                mTools.debug("health onResponse");
                if (!response.hasErrors()){
                    List<HealthSubscription.Health> newHealthItems = new ArrayList<>();
                    List<HealthSubscription.Health> newAlcoItems = new ArrayList<>();
                    List<HealthSubscription.Health> newMoodItems = new ArrayList<>();

                    List<GetHealthQuery.GetHealth> healthItems = new ArrayList<>(mDataController.getHealthItems());
                    List<GetHealthQuery.GetHealth> alcoItems = new ArrayList<>(mDataController.getAlcoItems());
                    List<GetHealthQuery.GetHealth> moodItems = new ArrayList<>(mDataController.getMoodItems());

                    for (HealthSubscription.Health item : response.getData().health()){
                        switch (item.type()){
                            case "health":
                                newHealthItems.add(item);
                                break;
                            case "alcohol":
                                newAlcoItems.add(item);
                                break;
                            case "mood":
                                newMoodItems.add(item);
                                break;
                        }
                    }

                    for (int i = 0; i < healthItems.size(); i++){
                        HealthSubscription.Health newItem = newHealthItems.get(i);
                        GetHealthQuery.GetHealth oldItem = healthItems.get(i);
                        GetHealthQuery.GetHealth item = new GetHealthQuery.GetHealth(
                                oldItem.__typename(),
                                newItem.id(),
                                oldItem.title(),
                                oldItem.description(),
                                newItem.type(),
                                oldItem.image_path(),
                                oldItem.user_level(),
                                oldItem.price_rub(),
                                oldItem.health(),
                                oldItem.alcohol(),
                                oldItem.mood(),
                                newItem.status(),
                                newItem.message());
                        healthItems.set(i, item);
                    }

                    for (int i = 0; i < alcoItems.size(); i++){
                        HealthSubscription.Health newItem = newAlcoItems.get(i);
                        GetHealthQuery.GetHealth oldItem = alcoItems.get(i);
                        GetHealthQuery.GetHealth item = new GetHealthQuery.GetHealth(
                                oldItem.__typename(),
                                newItem.id(),
                                oldItem.title(),
                                oldItem.description(),
                                newItem.type(),
                                oldItem.image_path(),
                                oldItem.user_level(),
                                oldItem.price_rub(),
                                oldItem.health(),
                                oldItem.alcohol(),
                                oldItem.mood(),
                                newItem.status(),
                                newItem.message());
                        alcoItems.set(i, item);
                    }

                    for (int i = 0; i < moodItems.size(); i++){
                        HealthSubscription.Health newItem = newMoodItems.get(i);
                        GetHealthQuery.GetHealth oldItem = moodItems.get(i);
                        GetHealthQuery.GetHealth item = new GetHealthQuery.GetHealth(
                                oldItem.__typename(),
                                newItem.id(),
                                oldItem.title(),
                                oldItem.description(),
                                newItem.type(),
                                oldItem.image_path(),
                                oldItem.user_level(),
                                oldItem.price_rub(),
                                oldItem.health(),
                                oldItem.alcohol(),
                                oldItem.mood(),
                                newItem.status(),
                                newItem.message());
                        moodItems.set(i, item);
                    }

                    mDataController.setHealthItems(healthItems);
                    mDataController.setAlcoItems(alcoItems);
                    mDataController.setMoodItems(moodItems);

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("health onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("health onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("health onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("health onConnected");
            }
        });
    }


    private void getJob(){
        GetJobsQuery getJobsQuery = GetJobsQuery.builder().build();

        gameClient.query(getJobsQuery).enqueue(new ApolloCall.Callback<GetJobsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetJobsQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setJobItems(response.getData().getJobs());
                } else {
                    mTools.debug("getJobsQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getJobsQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getGames(){
        List<GameItem> items = new ArrayList<>();
        items.add(new GameItem("2048", R.drawable.icon_2048, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTools.playSound();
                startActivity(new Intent(context, Activity2048.class));
            }
        }));

        mDataController.setGameItems(items);
    }

    private void getJobLong(){
        GetLongtermJobsQuery getLongtermJobsQuery = GetLongtermJobsQuery.builder()
                .build();

        gameClient.query(getLongtermJobsQuery).enqueue(new ApolloCall.Callback<GetLongtermJobsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetLongtermJobsQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setJobLongtermItems(response.getData().getLongtermJobs());
                } else {
                    mTools.debug("getLongtermJobsQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getLongtermJobsQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void subscribeJob(){
        gameClient.subscribe(new JobSubscription()).execute(new ApolloSubscriptionCall.Callback<JobSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<JobSubscription.Data> response) {
                mTools.debug("Job onResponse");
                if (!response.hasErrors()){
                    List<JobSubscription.Job> newHardItems = response.getData().job();

                    List<GetJobsQuery.GetJob> hardItems = new ArrayList<>(mDataController.getJobItems());

                    for (int i = 0; i < hardItems.size(); i++){
                        JobSubscription.Job newItem = newHardItems.get(i);
                        GetJobsQuery.GetJob oldItem = hardItems.get(i);
                        GetJobsQuery.GetJob item = new GetJobsQuery.GetJob(
                                oldItem.__typename(),
                                oldItem.id(),
                                oldItem.title(),
                                oldItem.type(),
                                oldItem.user_level(),
                                oldItem.price_rub(),
                                oldItem.price_btc(),
                                newItem.status(),
                                newItem.message());
                        hardItems.set(i, item);
                    }

                    mDataController.setJobItems(hardItems);

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("Job onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("Job onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("Job onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("Job onConnected");
            }
        });
    }

    private void subscribeJobLongterm(){
        gameClient.subscribe(new LongtermJobSubscription()).execute(new ApolloSubscriptionCall.Callback<LongtermJobSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<LongtermJobSubscription.Data> response) {
                mTools.debug("JobLongterm onResponse");
                if (!response.hasErrors()){
                    List<LongtermJobSubscription.LongtermJob> newHardItems = response.getData().longtermJob();

                    List<GetLongtermJobsQuery.GetLongtermJob> hardItems = new ArrayList<>(mDataController.getJobLongtermItems());

                    for (int i = 0; i < hardItems.size(); i++){
                        LongtermJobSubscription.LongtermJob newItem = newHardItems.get(i);
                        GetLongtermJobsQuery.GetLongtermJob oldItem = hardItems.get(i);
                        GetLongtermJobsQuery.GetLongtermJob item = new GetLongtermJobsQuery.GetLongtermJob(
                                oldItem.__typename(),
                                oldItem.id(),
                                oldItem.title(),
                                oldItem.description(),
                                oldItem.image_path(),
                                oldItem.user_level(),
                                oldItem.price_rub(),
                                oldItem.price_btc(),
                                oldItem.time(),
                                newItem.status(),
                                newItem.message());
                        hardItems.set(i, item);
                    }

                    mDataController.setJobLongtermItems(hardItems);

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("JobLongterm onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("JobLongterm onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("JobLongterm onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("JobLongterm onConnected");
            }
        });
    }


    private void getHardware(){
        GetHardwareQuery getHardwareQuery = GetHardwareQuery.builder().build();

        gameClient.query(getHardwareQuery).enqueue(new ApolloCall.Callback<GetHardwareQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetHardwareQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setHardwareItems(response.getData().getHardware());
                } else {
                    mTools.debug("getHardwareQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getHardwareQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getSoftware(){
        GetSoftwareQuery getSoftwareQuery = GetSoftwareQuery.builder().build();

        gameClient.query(getSoftwareQuery).enqueue(new ApolloCall.Callback<GetSoftwareQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetSoftwareQuery.Data> response) {
                if (!response.hasErrors()){
                    for (GetSoftwareQuery.GetSoftware item : response.getData().getSoftware()){
                    }
                    mDataController.setSoftwareItems(response.getData().getSoftware());
                } else {
                    mTools.debug("getSoftwareQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getSoftwareQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getPerks(){
        //TODO
    }

    private void subscribeHardware(){
        gameClient.subscribe(new HardwareSubscription()).execute(new ApolloSubscriptionCall.Callback<HardwareSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<HardwareSubscription.Data> response) {
                mTools.debug("Hardware onResponse");
                if (!response.hasErrors()){
                    List<HardwareSubscription.Hardware> newHardItems = response.getData().hardware();

                    List<GetHardwareQuery.GetHardware> hardItems = new ArrayList<>(mDataController.getHardwareItems());

                    for (int i = 0; i < hardItems.size(); i++){
                        HardwareSubscription.Hardware newItem = newHardItems.get(i);
                        GetHardwareQuery.GetHardware oldItem = hardItems.get(i);
                        GetHardwareQuery.GetHardware item = new GetHardwareQuery.GetHardware(
                                oldItem.__typename(),
                                oldItem.id(),
                                oldItem.type(),
                                newItem.level(),
                                newItem.title(),
                                oldItem.description(),
                                oldItem.image_path(),
                                newItem.next_price_rub(),
                                newItem.status(),
                                newItem.message());
                        hardItems.set(i, item);
                    }

                    mDataController.setHardwareItems(hardItems);

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("Hardware onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("Hardware onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("Hardware onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("Hardware onConnected");
            }
        });
    }

    private void subscribeSoftware(){
        gameClient.subscribe(new SoftwareSubscription()).execute(new ApolloSubscriptionCall.Callback<SoftwareSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<SoftwareSubscription.Data> response) {
                mTools.debug("Software onResponse");
                if (!response.hasErrors()){
                    List<SoftwareSubscription.Software> newSoftItems = response.getData().software();

                    List<GetSoftwareQuery.GetSoftware> softItems = new ArrayList<>(mDataController.getSoftwareItems());

                    for (int i = 0; i < softItems.size(); i++){
                        SoftwareSubscription.Software newItem = newSoftItems.get(i);
                        GetSoftwareQuery.GetSoftware oldItem = softItems.get(i);
                        GetSoftwareQuery.GetSoftware item = new GetSoftwareQuery.GetSoftware(
                                oldItem.__typename(),
                                newItem.id(),
                                newItem.title(),
                                oldItem.short_description(),
                                oldItem.description(),
                                oldItem.image_path(),
                                newItem.price_rub(),
                                newItem.purchased(),
                                newItem.status(),
                                newItem.message());
                        softItems.set(i, item);
                    }

                    mDataController.setSoftwareItems(softItems);

                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("Software onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("Software onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("Software onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("Software onConnected");
            }
        });
    }

    private void subscribePerks(){
        //TODO
    }


    private void getNews(){
        GetNewsQuery getNewsQuery = GetNewsQuery.builder().build();

        gameClient.query(getNewsQuery).enqueue(new ApolloCall.Callback<GetNewsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetNewsQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setNewItems(response.getData().getNews());
                } else {
                    mTools.debug("getNewsQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getNewsQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getPartners(){
        //TODO
    }

    private void subscribeNews(){
        gameClient.subscribe(new NewsSubscription()).execute(new ApolloSubscriptionCall.Callback<NewsSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<NewsSubscription.Data> response) {
                mTools.debug("News onResponse");
                if (!response.hasErrors()){
                    List<NewsSubscription.New> newNewsItems = response.getData().news();

                    List<GetNewsQuery.GetNew> newsItems = new ArrayList<>(mDataController.getNewItems());

                    for (int i = 0; i < newsItems.size(); i++){
                        NewsSubscription.New newItem = newNewsItems.get(i);
                        GetNewsQuery.GetNew oldItem = newsItems.get(i);
                        GetNewsQuery.GetNew item = new GetNewsQuery.GetNew(
                                oldItem.__typename(),
                                newItem.id(),
                                oldItem.date(),
                                oldItem.title(),
                                oldItem.body(),
                                newItem.read());
                        newsItems.set(i, item);
                    }

                    mDataController.setNewItems(newsItems);
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("News onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("News onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("News onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("News onConnected");
            }
        });
      }

    private void subscribePartners(){
        //TODO
    }


    private void getHouse(){
        GetHousingQuery getHousingQuery = GetHousingQuery.builder().build();

        gameClient.query(getHousingQuery).enqueue(new ApolloCall.Callback<GetHousingQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetHousingQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setHouseItems(response.getData().getHousing());
                } else {
                    mTools.debug("getHousingQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getHousingQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getCar(){
        GetCarsQuery getCarsQuery = GetCarsQuery.builder().build();

        gameClient.query(getCarsQuery).enqueue(new ApolloCall.Callback<GetCarsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCarsQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setCarItems(response.getData().getCars());
                } else {
                    mTools.debug("getCarsQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getCarsQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void getGirl(){
        GetGirlItemsQuery getGirlQuery = GetGirlItemsQuery.builder().build();

        gameClient.query(getGirlQuery).enqueue(new ApolloCall.Callback<GetGirlItemsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetGirlItemsQuery.Data> response) {
                if (!response.hasErrors()){
                    mDataController.setGirlItems(response.getData().getGirlItems());
                } else {
                    mTools.debug("getGirlQuery");
                    mTools.debug(response.getErrors().get(0).getMessage());
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("getGirlQuery");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }
        });
    }

    private void subscribeHouse(){
        gameClient.subscribe(new HousingSubscription()).execute(new ApolloSubscriptionCall.Callback<HousingSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<HousingSubscription.Data> response) {
                mTools.debug("House onResponse");
                if (!response.hasErrors()){
                    List<HousingSubscription.Housing> newHouseItems = response.getData().housing();

                    List<GetHousingQuery.GetHousing> houseItems = new ArrayList<>(mDataController.getHouseItems());

                    for (int i = 0; i < houseItems.size(); i++){
                        HousingSubscription.Housing newItem = newHouseItems.get(i);
                        GetHousingQuery.GetHousing oldItem = houseItems.get(i);
                        GetHousingQuery.GetHousing item = new GetHousingQuery.GetHousing(
                                oldItem.__typename(),
                                oldItem.id(),
                                newItem.level(),
                                newItem.title(),
                                newItem.description(),
                                newItem.image_path(),
                                newItem.price_rub(),
                                newItem.status(),
                                newItem.message());
                        houseItems.set(i, item);
                    }

                    mDataController.setHouseItems(houseItems);
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("House onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("House onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("House onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("House onConnected");
            }
        });
    }

    private void subscribeCar(){
        gameClient.subscribe(new CarsSubscription()).execute(new ApolloSubscriptionCall.Callback<CarsSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<CarsSubscription.Data> response) {
                mTools.debug("Car onResponse");
                if (!response.hasErrors()){
                    List<CarsSubscription.Car> newCarItems = response.getData().cars();

                    List<GetCarsQuery.GetCar> carItems = new ArrayList<>(mDataController.getCarItems());

                    for (int i = 0; i < carItems.size(); i++){
                        CarsSubscription.Car newItem = newCarItems.get(i);
                        GetCarsQuery.GetCar oldItem = carItems.get(i);
                        GetCarsQuery.GetCar item = new GetCarsQuery.GetCar(
                                oldItem.__typename(),
                                oldItem.id(),
                                newItem.level(),
                                newItem.title(),
                                newItem.description(),
                                newItem.image_path(),
                                newItem.price_rub(),
                                newItem.status(),
                                newItem.message());
                        carItems.set(i, item);
                    }

                    mDataController.setCarItems(carItems);
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("Car onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("Car onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("Car onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("Car onConnected");
            }
        });
    }

    private void subscribeGirl(){
        gameClient.subscribe(new GirlsSubscription()).execute(new ApolloSubscriptionCall.Callback<GirlsSubscription.Data>() {
            @Override
            public void onResponse(@NonNull Response<GirlsSubscription.Data> response) {
                mTools.debug("Girl onResponse");
                if (!response.hasErrors()){
                    List<GirlsSubscription.Girl> newGirlItems = response.getData().girls();

                    List<GetGirlItemsQuery.GetGirlItem> girlItems = new ArrayList<>(mDataController.getGirlItems());

                    for (int i = 0; i < girlItems.size(); i++){
                        GirlsSubscription.Girl newGirlItem = newGirlItems.get(i);
                        GetGirlItemsQuery.GetGirlItem oldGirlItem = girlItems.get(i);

                        List<GirlsSubscription.Item> newItems = newGirlItem.items();

                        List<GetGirlItemsQuery.Item> items = new ArrayList<>(oldGirlItem.items());

                        for (int j = 0; j < items.size(); j++){
                            GirlsSubscription.Item nItem = newItems.get(j);
                            GetGirlItemsQuery.Item oItem = items.get(j);

                            GetGirlItemsQuery.Item item = new GetGirlItemsQuery.Item(
                                    oItem.__typename(),
                                    oItem.id(),
                                    oItem.level(),
                                    oItem.title(),
                                    oItem.description(),
                                    oItem.image_path(),
                                    oItem.user_level(),
                                    oItem.price_rub(),
                                    oItem.daily_money(),
                                    oItem.rating(),
                                    nItem.status(),
                                    nItem.message());

                            items.set(j, item);
                        }


                        GetGirlItemsQuery.GetGirlItem item = new GetGirlItemsQuery.GetGirlItem(
                                oldGirlItem.__typename(),
                                oldGirlItem.type(),
                                items);
                        girlItems.set(i, item);
                    }

                    mDataController.setGirlItems(girlItems);
                } else {
                    mTools.startErrorConnectionActivity(context);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                mTools.debug("Girl onFailure");
                mTools.debug(e.getMessage());
                mTools.startErrorConnectionActivity(context);
            }

            @Override
            public void onCompleted() {
                mTools.debug("Girl onCompleted");
            }

            @Override
            public void onTerminated() {
                mTools.debug("Girl onTerminated");
            }

            @Override
            public void onConnected() {
                mTools.debug("Girl onConnected");
            }
        });
    }


    private void checkUpdate(){
        appUpdateManager = AppUpdateManagerFactory.create(context);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            1);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            1);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
