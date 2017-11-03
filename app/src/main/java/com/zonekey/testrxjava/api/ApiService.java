package com.zonekey.testrxjava.api;

import com.alibaba.fastjson.JSONObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.zonekey.testrxjava.bean.FeedBack;

import io.reactivex.Observable;

/**
 * Created by xu.wang
 * Date on  2017/11/2 19:50:49.
 *
 * @Desc Rxjava的 Api类
 */

public class ApiService {

    /**
     * 获得被观察者 ,可以使用任意框架,这里使用的是Okgo,也可以使用Retrofit
     * <p>
     * 强烈建议将获得某个被观察者的代码和请求分开,因为rxjava设计的目的之一就是将数据请求和Ui操作分开
     * <p>
     * 建议像使用Retrofit一样, 建一个ApiService,将所有本段代码放进去
     */
    public static Observable<Response<String>> getFeedBack(FeedBack feedBack) {
        String url = "http://120.77.253.101:8091/wisdom/app/insertCustomerBack";
        return OkGo.<String>post(url).upJson(JSONObject.toJSONString(feedBack)).
                converter(new StringConvert()).adapt(new ObservableResponse<String>());
    }


}
