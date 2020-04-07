package com.zmy.camerademo.camera.frameprocessor;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.zmy.camerademo.BaseApplication;
import com.zmy.camerademo.camera.CamereHandlerMsgIDs;
import com.zmy.camerademo.camera.DecodeFormatManager;
import com.zmy.camerademo.camera.PreviewManager;
import com.zmy.camerademo.camera.ValueUtil;
import com.zmy.camerademo.threadpoll.QRCodeDecodeThreadPool;
import com.zmy.camerademo.util.DisplayUtil;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

public class QRCameraFrameProcessorImpl implements CameraFrameProcessor {
    private final Handler decodeHandler;
    private MultiFormatReader multiFormatReader;
    private final String TAG = QRCameraFrameProcessorImpl.class.getSimpleName();
    private PreviewManager previewManager;
    private Rect framingRectInPreview;

    public QRCameraFrameProcessorImpl(PreviewManager previewManager) {
        decodeHandler = previewManager.getDecodeHandler();
        initFormatReader();
        this.previewManager = previewManager;
    }

    private void initFormatReader() {
        multiFormatReader = new MultiFormatReader();
        Hashtable hints = new Hashtable();
        //识别二维码
        hints.put(DecodeHintType.POSSIBLE_FORMATS, DecodeFormatManager.QR_CODE_FORMATS);
        // hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);//设置尽量识别
        multiFormatReader.setHints(hints);
    }

    @Override
    public void onStartCallFrame() {
        QRCodeDecodeThreadPool.buildThreadPool();
    }

    @Override
    public boolean isLoopCallFrame() {
        return true;
    }

    @Override
    public void onFrameCall(final byte[] data, final Camera camera ) {
        QRCodeDecodeThreadPool.addTask(new Runnable() {
            @Override
            public void run() {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size cameraPreviewSize = parameters.getPreviewSize();
                int previewWidth = cameraPreviewSize.width;
                int previewHeight = cameraPreviewSize.height;
                //扫描条形码
                PlanarYUVLuminanceSource source = buildLuminanceSource(data, previewWidth, previewHeight);
                if (source != null) {
                    Log.i(TAG, "扫描中。。。");
                    BinaryBitmap binaryBitmapbitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                    if (binaryBitmapbitmap == null) {
                        binaryBitmapbitmap = new BinaryBitmap(new HybridBinarizer(source));
                    }
                    try {
                        Hashtable hints = new Hashtable();
                        //识别二維碼
                        hints.put(DecodeHintType.POSSIBLE_FORMATS, DecodeFormatManager.QR_CODE_FORMATS);
                        Result rawResult = multiFormatReader.decode(binaryBitmapbitmap, hints);
                        if (rawResult != null) {
                            final String result = ResultParser.parseResult(rawResult).getDisplayResult().toString().trim();
                            synchronized (QRCameraFrameProcessorImpl.this) {
                                if (ValueUtil.isStringValid(result) && previewManager.getIsStartCallFrame()) {
                                    previewManager.stopCallFrame();
                                    QRCodeDecodeThreadPool.shutdown();
                                    Log.i("QRCodeCameraPreview", "扫描结果" + result);
                                    if (decodeHandler != null) {
                                        Message message = Message.obtain();
                                        message.what = CamereHandlerMsgIDs.SCAN_SUCCESS;
                                        message.obj = result;
                                        decodeHandler.sendMessage(message);
                                    }
                                }
                            }
                        }

                    } catch (ReaderException re) {
                        Log.e(TAG, "ReaderException: " + re.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "Exception: " + e.getMessage());
                    } finally {
                        multiFormatReader.reset();
                    }
                }
            }
        });
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview(previewManager.getFrameScanRect(), width, height);
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);
    }


    public synchronized Rect getFramingRectInPreview(Rect framingRect, int width, int height) {
        if (framingRectInPreview == null) {
            if (framingRect == null) {
                return null;
            }
            framingRectInPreview = new Rect();
            framingRectInPreview.left = (int) (((framingRect.top + (framingRect.height() / 2)) / (double) DisplayUtil.getScreenHeight(BaseApplication.applicationContext)) * width - height / 2);
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
