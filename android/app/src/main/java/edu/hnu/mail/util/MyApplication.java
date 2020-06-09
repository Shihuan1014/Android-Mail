package edu.hnu.mail.util;
import android.app.Application;

import com.tencent.smtt.sdk.QbSdk;

/**
 * 自定义MyApplication类继承Application
 * 并重写onCreate方法完成一些初始化加载操作
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        preinitX5WebCore();
    }

    /**
     * 预加载x5内核
     */
    private void preinitX5WebCore() {
        if (!QbSdk.isTbsCoreInited()){
            // 这个函数内是异步执行所以不会阻塞 App 主线程，这个函数内是轻量级执行所以对 App 启动性能没有影响
            QbSdk.initX5Environment(this, null);
        }
    }
}
