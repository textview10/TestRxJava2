package com.zonkey.mobileteach_lib.network.api;

import com.zonkey.mobileteach_lib.bean.AppVersionRequest;
import com.zonkey.mobileteach_lib.bean.AppVersionResponse;
import com.zonkey.mobileteach_lib.bean.FeedBack;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by xu.wang
 * Date on 2017/6/28 11:38
 */

public interface TestApi {

    //智客助手的反馈消息
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("/wisdom/app/insertCustomerBack")
    Call<ResponseBody> commitFeedBack(@Body FeedBack feedBack);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("/wisdom/app/findSysAppVersion")
    Call<ArrayList<AppVersionResponse>> getApkVersion(@Body AppVersionRequest request);

    @GET("/wisdom/app/down")
    Call<ResponseBody> downLoadApk(@Query("path") String fileurl);

    @Streaming
    @GET("/wisdom/app/down?path=/app/MT_Android/MT_Android_1.0.28.apk")
    Call<ResponseBody> downLoadApp();

}
