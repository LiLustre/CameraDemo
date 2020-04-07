package com.zmy.camerademo.camera;

import android.view.SurfaceHolder;

public interface SurfaceHolderLifecycleCallBack {
    void surfaceCreated(SurfaceHolder holder);
    void surfaceChanged(SurfaceHolder holder, int format, int width, int height);
    void surfaceDestroyed(SurfaceHolder holder);
}
