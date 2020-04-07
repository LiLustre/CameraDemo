package com.zmy.camerademo.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by lz on 2018/4/21.
 */

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private Camera mCamera;

    private int displayOrientation;
    private Context context;
    private Camera.Parameters mParams;

    private int previewHeight;
    private int previewWidth;
    private Rect framingRectInPreview;
    private int screenWidth;
    private int screenHeight;
    private boolean previewing = false;

    public CameraManager(Context context) {
        this.context = context;
        Point theScreenResolution = new Point();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getSize(theScreenResolution);
        screenWidth = theScreenResolution.x;
        screenHeight = theScreenResolution.y;
    }

    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
        open();
        if (mCamera == null) {
            return;
        }
        setCameraParams();
        mCamera.setPreviewDisplay(holder);
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
        }
    }

    /**
     * Opens a rear-facing camera with {@link Camera#open(int)}, if one exists, or opens camera 0.
     */
    private void open() {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.w(TAG, "No cameras!");
            return;
        }
        int index = 0;
        while (index < numCameras) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                break;
            }
            index++;
        }

        Camera camera;
        if (index < numCameras) {
            Log.i(TAG, "Opening camera #" + index);
            camera = Camera.open(index);
        } else {
            Log.i(TAG, "No camera facing back; returning camera #0");
            camera = Camera.open(0);
        }
        this.mCamera = camera;
    }

    public synchronized boolean isOpen() {
        return mCamera != null;
    }

    /**
     * 设置相机参数
     */
    public void setCameraParams() {
        if (mCamera == null) {
            Log.e(TAG, "camera is null when setCameraParams");
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        //设置预览窗口大小
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        if (sizes.size() > 0) {
            Point theScreenResolution = new Point();
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            display.getSize(theScreenResolution);
            // Point basetPreviewPoint = CameraUtil.findBestPreviewSizeValue(params, new Point(screenWidth, screenHeight));
            Camera.Size size = CameraUtil.findCloselySize(screenWidth, screenHeight, params.getSupportedPreviewSizes());
            Log.e(TAG + "bastpreviewSize", size.width + ":" + size.height);
            params.setPreviewSize(size.width, size.height);
            previewHeight = size.width;
            previewWidth = size.height;
        }
        //设置对焦模式
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            // 设置连续对焦模式
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.cancelAutoFocus();
        }
        //设置图片质量
        params.setJpegQuality(100);
        //设置相机显示方向
        displayOrientation = CameraUtil.getDisplayOrientation(context);
        mCamera.setDisplayOrientation(displayOrientation);
        params.setRotation(displayOrientation);
        mCamera.setParameters(params);
        mParams = params;
    }

    /**
     * 开启闪光灯
     */
    public void openFlashlight() {
        if (mCamera == null) {
            Log.e(TAG, "camera is null when openTorch");
            return;
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParams);
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        if (mCamera != null && !previewing) {
            //开始预览
            previewing = true;
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null && previewing) {
            previewing = false;
            mCamera.stopPreview();
        }

    }

    /**
     * 关闭闪光灯
     */
    public void closeFlashlight() {
        if (mCamera == null) {
            Log.e(TAG, "camera is null when closeTorch");
            return;
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParams);
    }

    public synchronized void setOneShotPreviewCallback(Camera.PreviewCallback cb) {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(cb);
        }
    }

    public synchronized Camera.Parameters getParameters() {
        return mParams;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;
    }

    public synchronized void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    public synchronized boolean isReleaseCamera() {
        return mCamera == null;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public synchronized Rect getFramingRectInPreview(Rect framingRect, int width, int height) {
        if (framingRectInPreview == null) {
            if (framingRect == null) {
                return null;
            }
            framingRectInPreview = new Rect();
            framingRectInPreview.left = (int) (((framingRect.top + (framingRect.height() / 2)) / (double) screenHeight) * width - height / 2);
            framingRectInPreview.right = framingRectInPreview.left + height;
            if (framingRectInPreview.right > width) {
                framingRectInPreview.right = width;
            }
            framingRectInPreview.top = 0;
            framingRectInPreview.bottom = height;
        }
        return framingRectInPreview;
    }
}
