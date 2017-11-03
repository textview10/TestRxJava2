package com.zonekey.testrxjava.base;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by xu.wang
 * Date on  2017/11/2 17:55:01.
 *
 * @Desc
 */

public class BaseActivity extends AppCompatActivity {
    private CompositeDisposable composite;

    protected void addDispose(Disposable disposable) {
        if (composite == null) {
            composite = new CompositeDisposable();
        }
        composite.add(disposable);
    }

    protected void dispose() {
        if (composite != null) composite.dispose();
    }
}
