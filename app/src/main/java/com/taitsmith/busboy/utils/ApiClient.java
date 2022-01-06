package com.taitsmith.busboy.utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    static Retrofit acTransitRetrofit;
    static Retrofit googleMapsRetrofit;

    public static Retrofit getAcTransitClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        acTransitRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.actransit.org/transit/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return acTransitRetrofit;
    }

    public static Retrofit getMapsClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        googleMapsRetrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return googleMapsRetrofit;
    }
}
