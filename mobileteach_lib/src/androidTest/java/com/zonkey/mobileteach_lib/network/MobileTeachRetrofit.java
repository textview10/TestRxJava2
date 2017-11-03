package com.zonkey.mobileteach_lib.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xu.wang
 * Date on 2017/6/28 11:40
 */

public class MobileTeachRetrofit {

    private MobileTeachRetrofit() {
    }

    public static <T> T createRetrofit(final Class<T> clazz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://120.77.253.101:8091")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

}
