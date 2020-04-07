package com.zmy.camerademo.camera.frameresult;

import android.graphics.Bitmap;

public interface FramePhotoCallback {
    /**
     * 预览回调
     *
     * @param bitmap
     */
    void onPreviewTakePhoto(Bitmap bitmap);
}
