package com.zmy.camerademo.camera.frameresult;

public interface FrameErrorCallback {
    /**
     * 发生错误时
     */
    void oneShotPreviewFrameError(Exception e);

}
