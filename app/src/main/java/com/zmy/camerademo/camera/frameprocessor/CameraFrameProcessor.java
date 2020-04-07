package com.zmy.camerademo.camera.frameprocessor;

import android.hardware.Camera;

import com.zmy.camerademo.camera.PreviewManager;

import java.util.concurrent.atomic.AtomicBoolean;

public interface CameraFrameProcessor {
    void onStartCallFrame();

    boolean isLoopCallFrame();

    void onFrameCall(byte[] data, Camera camera );
}
