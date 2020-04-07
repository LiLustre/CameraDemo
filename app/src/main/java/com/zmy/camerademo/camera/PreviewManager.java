package com.zmy.camerademo.camera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.zmy.camerademo.camera.frameprocessor.CameraFrameProcessor;
import com.zmy.camerademo.camera.frameresult.FrameErrorCallback;
import com.zmy.camerademo.camera.frameresult.FramePhotoCallback;
import com.zmy.camerademo.camera.frameresult.FrameScanSuccessCallback;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class PreviewManager implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final String TAG = PreviewManager.class.getSimpleName();
    private Context context;
    private boolean hasSurface = false;
    private DecodeHandler decodeHandler;
    public AtomicBoolean isStartCallFrame = new AtomicBoolean(false);
    private SurfaceView surfaceView;
    private CameraManager cameraManager;
    private SurfaceHolderLifecycleCallBack surfaceHolderLifecycleCallBack;
    private CameraFrameProcessor cameraFrameProcessor;
    private FrameErrorCallback frameErrorCallback;
    private FramePhotoCallback framePhotoCallback;
    private FrameScanSuccessCallback frameScanSuccessCallback;
    private Rect frameScanRect;

    public PreviewManager(Context context) {
        this.cameraManager = new CameraManager(context);
        this.context = context;
        decodeHandler = new DecodeHandler(Looper.myLooper(),this);
    }

    /**
     * 设置帧预览回调处理者
     *
     * @param cameraFrameProcessor
     */
    public void setCameraFrameProcessor(CameraFrameProcessor cameraFrameProcessor) {
        this.cameraFrameProcessor = cameraFrameProcessor;
    }

    public void setFrameErrorCallback(FrameErrorCallback frameErrorCallback) {
        this.frameErrorCallback = frameErrorCallback;
    }

    public void setFramePhotoCallback(FramePhotoCallback framePhotoCallback) {
        this.framePhotoCallback = framePhotoCallback;
    }

    public void setFrameScanSuccessCallback(FrameScanSuccessCallback frameScanSuccessCallback) {
        this.frameScanSuccessCallback = frameScanSuccessCallback;
    }

    public DecodeHandler getDecodeHandler() {
        return decodeHandler;
    }

    /**
     * 设置SurfaceHolder生命周期回调
     *
     * @param surfaceHolderLifecycleCallBack
     */
    public void setSurfaceHolderLifecycleCallBack(SurfaceHolderLifecycleCallBack surfaceHolderLifecycleCallBack) {
        this.surfaceHolderLifecycleCallBack = surfaceHolderLifecycleCallBack;
    }

    public void onResume(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        if (surfaceView == null) {
            return;
        }
        isStartCallFrame.set(false);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.removeCallback(this);
        surfaceHolder.addCallback(this);

    }

    public void onPause() {
        if (surfaceView == null) {
            return;
        }
        isStartCallFrame.set(false);
        if (!cameraManager.isReleaseCamera()) {
            cameraManager.setOneShotPreviewCallback(null);
        }

        cameraManager.stopPreview();
        cameraManager.closeDriver();
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
    }

    public void setFrameScanRect(Rect frameScanRect) {
        this.frameScanRect = frameScanRect;
    }

    public Rect getFrameScanRect() {
        return frameScanRect;
    }

    public synchronized void startCallFrame() {
        if (isStartCallFrame.get()) {
            return;
        }
        if (cameraFrameProcessor != null) {
            cameraFrameProcessor.onStartCallFrame();
        }
        isStartCallFrame.set(true);
        setOneShotPreview();
    }

    /**
     * 停止扫描识别
     */
    public void stopCallFrame() {
        if (isStartCallFrame.get()) {
            isStartCallFrame.set(false);
            if (!cameraManager.isReleaseCamera()) {
                cameraManager.setOneShotPreviewCallback(null);
            }
        }
    }

    public boolean getIsStartCallFrame() {
        return isStartCallFrame.get();
    }

    private void setOneShotPreview() {
        if (isStartCallFrame.get()) {
            try {
                if (!cameraManager.isReleaseCamera()) {
                    //接收下一次预览回调
                    Log.e(TAG, "setOneShotPreview: ");
                    cameraManager.setOneShotPreviewCallback(this);
                } else {
                    isStartCallFrame.set(false);
                    Log.e(TAG, "camera has release when setOneShotPreview");
                    this.frameErrorCallback.oneShotPreviewFrameError(null);
                }
            } catch (Exception e) {
                isStartCallFrame.set(false);
                Log.e(TAG, e.getMessage());
                if (this.frameErrorCallback != null) {
                    this.frameErrorCallback.oneShotPreviewFrameError(e);
                }
            }
        }
    }

    /**
     * 开启闪光灯
     */
    public void openFlashlight() {
        if (cameraManager == null) {
            return;
        }
        cameraManager.openFlashlight();
    }

    /**
     * 关闭闪光灯
     */
    public void colseFlashlight() {
        if (cameraManager == null) {
            return;
        }
        cameraManager.closeFlashlight();
    }

    /**
     * @param surfaceView 创建相机
     */
    public void createCamera(SurfaceView surfaceView) {
        if (!hasSurface) {
            hasSurface = true;
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            initCamera(surfaceHolder);
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            cameraManager.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated: ");
        if (this.surfaceHolderLifecycleCallBack != null) {
            this.surfaceHolderLifecycleCallBack.surfaceCreated(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged: ");
        if (this.surfaceHolderLifecycleCallBack != null) {
            this.surfaceHolderLifecycleCallBack.surfaceChanged(holder, format, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        Log.e(TAG, "surfaceDestroyed: ");
        if (this.surfaceHolderLifecycleCallBack != null) {
            this.surfaceHolderLifecycleCallBack.surfaceDestroyed(holder);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isStartCallFrame.get()) {
            Log.i(TAG, "开始帧回调。。。");
            if (cameraFrameProcessor != null && cameraFrameProcessor.isLoopCallFrame()) {
                setOneShotPreview();
            }
            if (cameraFrameProcessor != null) {
                cameraFrameProcessor.onFrameCall(data, camera, isStartCallFrame);
            }
        }
    }

    public static class DecodeHandler extends Handler {
        private WeakReference<PreviewManager> previewManager;
        public DecodeHandler(Looper looper, PreviewManager previewManager) {
            super(looper);
            this.previewManager = new WeakReference<PreviewManager>(previewManager);

        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CamereHandlerMsgIDs.SCAN_SUCCESS:
                    if (this.previewManager!=null&&this.previewManager.get()!=null&this.previewManager.get().frameScanSuccessCallback != null) {
                        this.previewManager.get().frameScanSuccessCallback.onScanFrameDecodeResult((String) msg.obj);
                    }

            }
        }
    }
}
