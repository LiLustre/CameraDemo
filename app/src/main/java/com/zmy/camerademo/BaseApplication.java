package com.zmy.camerademo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

public class BaseApplication extends Application {
    public static Context applicationContext;
    private ArrayList<Activity> activities;

    private String serverVersion;
    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            activities.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            activities.remove(activity);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        initActivityBackRecordStack();

    }



    /**
     * 初始化activity回退栈
     */
    private void initActivityBackRecordStack() {
        activities = new ArrayList<Activity>();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    /**
     * 清理activity回退栈
     */
    public void clearActivityBackRecordStack() {
        for (int i = activities.size() - 1; i >= 0; i--) {
            Activity activity = activities.get(i);
            activity.finish();
        }
        activities.clear();
    }
    private boolean isReleaseVersion() {
        return "".equals(serverVersion);
    }
}
