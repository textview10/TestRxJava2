package com.zonkey.mobileteach_lib;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.fastjson.JSONObject;
import com.zonkey.mobileteach_lib.bean.AppVersionRequest;
import com.zonkey.mobileteach_lib.bean.AppVersionResponse;
import com.zonkey.mobileteach_lib.bean.FeedBack;
import com.zonkey.mobileteach_lib.bean.FeedState;
import com.zonkey.mobileteach_lib.network.MobileTeachRetrofit;
import com.zonkey.mobileteach_lib.network.api.TestApi;
import com.zonkey.mobileteach_lib.util.LogUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.zonkey.mobileteach_lib.test", appContext.getPackageName());
    }

    @Test
    public void testRetrofitPost() {
        final FeedBack feedBack = new FeedBack();
        feedBack.setAppid("MT_Android");
        feedBack.setCode("ssdfx");
        feedBack.setContent("retrofit的测试消息");
        feedBack.setLoginname("admin");
        TestApi testApi = MobileTeachRetrofit.createRetrofit(TestApi.class);
        Call<ResponseBody> response = testApi.commitFeedBack(feedBack);
        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
//                    LogUtil.e("TestApi", response.body().string());
                    FeedState feedState = JSONObject.parseObject(response.body().string(), FeedState.class);
                    Assert.assertEquals(1, feedState.getStatus());
                } catch (IOException e) {
                    LogUtil.i("TestApi", "Exception" + e.toString());
                    Assert.fail();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LogUtil.e("TestApi", "Exception" + t.toString());
                Assert.fail();
            }
        });
    }

    @Test
    public void testGetAppVersion() {
        AppVersionRequest appVersionRequest = new AppVersionRequest();
        appVersionRequest.setAppcode("MT_Android");
        appVersionRequest.setOrders("1");
        TestApi testApi = MobileTeachRetrofit.createRetrofit(TestApi.class);
        Call<ArrayList<AppVersionResponse>> apkVersion = testApi.getApkVersion(appVersionRequest);
        apkVersion.enqueue(new Callback<ArrayList<AppVersionResponse>>() {
            @Override
            public void onResponse(Call<ArrayList<AppVersionResponse>> call, final Response<ArrayList<AppVersionResponse>> response) {
                if (response.body() != null && response.body().get(0) != null)
                    testRetrofitDownloadFile(response.body().get(0).getFileurl());
            }

            @Override
            public void onFailure(Call<ArrayList<AppVersionResponse>> call, Throwable t) {
                LogUtil.e("TAG", "throwable" + t.toString());
                Assert.fail();
            }
        });
    }


    public void testRetrofitDownloadFile(String fileurl) {
        fileurl = "/app/MT_Android/MT_Android_1.0.28.apk";
        TestApi testApi = MobileTeachRetrofit.createRetrofit(TestApi.class);
        Call<ResponseBody> responseBodyCall = testApi.downLoadApp();
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                LogUtil.e("TAG", "response" + response.isSuccessful());
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        downLoadAppOnThread(response);
                    }
                }.start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Assert.fail();
            }
        });
    }

    private void downLoadAppOnThread(Response<ResponseBody> response) {
        InputStream is = response.body().byteStream();
        File file = new File(Environment.getExternalStorageDirectory(), "MobileTeach.apk");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            LogUtil.e("TAG", "创建文件失败" + e.toString());
            Assert.fail();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            int len;
            byte[] buff = new byte[1024 * 3];
            while ((len = bis.read(buff)) != -1) {
                fos.write(buff, 0, len);
                fos.flush();
            }
            fos.close();
            bis.close();
            is.close();
            LogUtil.e("TestApi", "下载完成");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.e("TestApi", e.toString());
            Assert.fail();
        } catch (IOException e) {
            LogUtil.e("TestApi", e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }
}
