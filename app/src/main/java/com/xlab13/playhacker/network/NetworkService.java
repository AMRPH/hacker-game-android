package com.xlab13.playhacker.network;


import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkService {
    private static NetworkService instance;
    private static final String authUrl = "http://tester.psec.pro:81/graphql";
    private static final String gameUrl = "http://tester.psec.pro:82/graphql";
    private static final String game2048Url = "http://tester.psec.pro:83/graphql";
    private static final String webSocketUrl = "ws://tester.psec.pro:82/graphql";

    public static String avatarUrl = "http://tester.psec.pro:82/createavatar/";
    public static String statusUrl = "http://tester.psec.pro/status";
    public static String imgUrl = "http://tester.psec.pro:82/";

    private ApolloClient authClient;
    private ApolloClient gameClient;
    private ApolloClient game2048Client;

    private String bearerToken;

    private NetworkService(){
    }

    public static NetworkService newInstance(){
        instance = new NetworkService();
        return instance;
    }

    public static NetworkService getInstance(){
        if (instance == null){
            instance = new NetworkService();
        }
        return instance;
    }


    public ApolloClient getAuthClient(){
        if (authClient == null){

            authClient = ApolloClient
                    .builder()
                    .serverUrl(authUrl)
                    .build();
        }
        return authClient;
    }

    public ApolloClient getGameClientWithToken(){
        if (isBearerToken()){
            if (gameClient == null){
                OkHttpClient okHttpClient = new OkHttpClient
                        .Builder()
                        .addInterceptor(new Interceptor() {
                            @NonNull
                            @Override
                            public Response intercept(@NonNull Chain chain) throws IOException {
                                Request original = chain.request();

                                Request.Builder builder = original
                                        .newBuilder()
                                        .method(original.method(), original.body())
                                        .addHeader("Authorization", "Bearer " + bearerToken);

                                return chain.proceed(builder.build());
                            }
                        })
                        .build();

                gameClient = ApolloClient
                        .builder()
                        .serverUrl(gameUrl)
                        .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(webSocketUrl, okHttpClient))
                        .okHttpClient(okHttpClient)
                        .build();
            }
            return gameClient;

        } return null;
    }

    public ApolloClient get2048ClientWithToken(){
        if (isBearerToken()){
            if (game2048Client == null){
                OkHttpClient okHttpClient = new OkHttpClient
                        .Builder()
                        .addInterceptor(new Interceptor() {
                            @NonNull
                            @Override
                            public Response intercept(@NonNull Chain chain) throws IOException {
                                Request original = chain.request();

                                Request.Builder builder = original
                                        .newBuilder()
                                        .method(original.method(), original.body())
                                        .addHeader("Authorization", "Bearer " + bearerToken);

                                return chain.proceed(builder.build());
                            }
                        })
                        .build();

                game2048Client = ApolloClient
                        .builder()
                        .serverUrl(game2048Url)
                        .okHttpClient(okHttpClient)
                        .build();
            }
            return game2048Client;

        } return null;
    }


    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public Boolean isBearerToken() {
        return !bearerToken.isEmpty();
    }

    public void clearData(){
        bearerToken = null;
        authClient = null;
        gameClient = null;
        instance = null;
    }
}
