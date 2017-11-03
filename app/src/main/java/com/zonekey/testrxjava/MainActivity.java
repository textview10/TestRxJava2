package com.zonekey.testrxjava;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.zonekey.testrxjava.api.ApiService;
import com.zonekey.testrxjava.base.BaseActivity;
import com.zonekey.testrxjava.bean.FeedBack;
import com.zonkey.mobileteach_lib.util.LogUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    private String TAG = "MainActivity";
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        testRxNet();  //网络请求
//        testRXZip();  //分别请求A,B两个接口,两个都请求完成后回调结果
//        testAsyNetRequest(); // 先请求A,成功后请求B
//        testFlatmap();        //先请求A,成功后将A结果传给B,B根据A一些参数去请求
//        testInterval();         //测试定时器...
    }

    private void testInterval() {
        // interval 间隔,幕前休息,区间
        // rx 中实现轮训
        //如果需要打断的话, 可以直接使用    mDisposable.dispose();
        mDisposable = Flowable.interval(1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtil.e(TAG, "doOnNext" + aLong);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtil.e(TAG, "subscribe" + aLong);
                    }
                });

    }

    //可以实现 先请求接口1, 从接口1中取得一些参数,再请求接口2
    private void testFlatmap() {
        // flatmap操作符可以将一个发射数据的Observable, 变换为Observables, 然后将它们发射的数据合并后放到一个单独的Observable,
        getObservable1().subscribeOn(Schedulers.io()).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                Log.e(TAG, "observable1" + s);
                return getObservable2();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String str) throws Exception {
                Log.e(TAG, "observable2" + str);
            }
        });
    }


    // ###################################### 类似逻辑先请求A接口,A接口请求成功后,再在请求B接口 ,以次类推##################
    private void testAsyNetRequest() {
        //concat 官方意思是把两个发射器 合并成一个发射器, 可以配合map或者doOnNext用来处理智课黑板教师端,学生端,pc间复杂的验证,交互逻辑;
        //目前我们自定义的TcpUtil ,UdpUtil还没有实现相关Observable(被观察者逻辑),如果黑板有重构需要,会实现这些
        Observable.concat(getObservable1(), getObservable2()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<String>() {      //可以使用Customer,是Observer的简易形式回调比较少
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "收到数据" + "开始订阅");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "收到数据" + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "收到数据" + e.getMessage().toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "收到数据" + "完成");
                    }
                });
    }
    // ###########################################################

    // ######################### 一次适合我们逻辑的标准网络请求的所有代码
    private void testRxNet() {
        FeedBack feedBack = new FeedBack();
        feedBack.setAppid("MT_Android");
        feedBack.setCode("ssdfx");
        feedBack.setContent("Rxjava2 与 rxAndroid 的相关测试消息");
        feedBack.setLoginname("admin");

        ApiService.getFeedBack(feedBack).subscribeOn(Schedulers.io())     //被观察者执行在Io线程,被观察者在这里是指网络请求
                .doOnSubscribe(new Consumer<Disposable>() {         //修改源,暂时不是特别理解为啥叫修改源,应该是在开始阶段修改一些东西吧,一般用来在网络起始显示loading
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        // TODO 显示loading
                        Log.e(TAG, "显示loading");
                    }
                }).observeOn(AndroidSchedulers.mainThread())         //观察者执行在Android的主线程
                .subscribe(new Observer<Response<String>>() {       //Observer指的是观察者, subscribe是指观察者订阅被观察者,两个建立联系
                    @Override
                    public void onSubscribe(Disposable d) {     //订阅成功时会调用
//                        d.dispose();      这句话意思是取消本次请求
                        addDispose(d);      //如果需要取消网络的需求,比如Activity销毁,建议调用BaseActivity里的此行代码,在需要取消处调用Base里的dispose();
                    }

                    @Override
                    public void onNext(Response<String> stringResponse) {   //网络请求成功时调用
                        // TODO 处理请求的结果
                        Log.e(TAG, "网络请求成功" + stringResponse.body().toString());
                        Log.e(TAG, "dialog消失");
                    }

                    @Override
                    public void onError(Throwable e) {          //网络请求失败时调用
                        // TODO 处理请求失败的结果
                        Log.e(TAG, "网络请求失败" + e.getMessage());
                        Log.e(TAG, "dialog消失");
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
    //########################################################################################

    // ############################ 分别从A,B两个接口同时请求数据,所有请求完成或错误后,返回到本地合并使用,如智课助手的双投屏验证逻辑
    private void testRXZip() {  // zip是合并符
        Observable.zip(getObservable1(), getObservable2(), new BiFunction<String, String, Boolean>() {
            @Override
            public Boolean apply(String s1, String s2) throws Exception {
                Log.e(TAG, "收到数据1" + s1);
                Log.e(TAG, "收到数据2" + s2);
                return true;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                Log.e(TAG, "可以刷新Ui吗?" + aBoolean);
            }
        });
    }

    //  #######################################
    private Observable<String> getObservable1() {   //模拟网络请求1
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.e(TAG, "开始请求数据1");
                SystemClock.sleep(5000);
                Log.e(TAG, "数据1请求成功");
                e.onNext("我是数据1");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());
    }   //这是测试,真实请求建议写入ApiService

    private Observable<String> getObservable2() {   //模拟网络请求2
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                Log.e(TAG, "开始请求数据2");
                SystemClock.sleep(3000);
                Log.e(TAG, "数据2请求成功");
                e.onNext("我是数据2");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());
    }   //这是测试,真实请求建议写入ApiService

}
