package com.zmy.camerademo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zmy.camerademo.camera.PreviewManager;
import com.zmy.camerademo.camera.SurfaceHolderLifecycleCallBack;
import com.zmy.camerademo.camera.frameprocessor.QRCameraFrameProcessorImpl;
import com.zmy.camerademo.camera.frameresult.FrameScanSuccessCallback;
import com.zmy.camerademo.permission.IPermission;
import com.zmy.camerademo.permission.PermissionGroup;
import com.zmy.camerademo.permission.PermissionsUtil;

public class MainActivity extends AppCompatActivity implements IPermission {

    private PreviewManager previewManager;
    private boolean flashlightOpend = false;
    private SurfaceView surfaceView;
    private ViewfinderView viewfinderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceview);
        viewfinderView = findViewById(R.id.finderview);
        previewManager = new PreviewManager(getApplicationContext());
        previewManager.setFrameScanRect(viewfinderView.getRect());
    }


    @Override
    protected void onResume() {
        super.onResume();
        previewManager.onResume(surfaceView);

        previewManager.setSurfaceHolderLifecycleCallBack(new SurfaceHolderLifecycleCallBack() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                PermissionsUtil.requestPermission(MainActivity.this, PermissionGroup.CAMERA_PERMISSIONS, PermissionGroup.CAMERA_REQUEST_CODE, MainActivity.this);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        QRCameraFrameProcessorImpl qrCameraFrameProcessor = new QRCameraFrameProcessorImpl(previewManager);
        previewManager.setCameraFrameProcessor(qrCameraFrameProcessor);
        previewManager.setFrameScanSuccessCallback(new FrameScanSuccessCallback() {
            @Override
            public void onScanFrameDecodeResult(String string) {
                Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        previewManager.onPause();
    }

    @Override
    public void onPermissionsGranted() {
        buildCamera();
    }

    @Override
    public void onLessThanMarshmallow() {
        buildCamera();
    }

    @Override
    public void onPermissionsGrantedAfterReq(int requestCode, String[] perms) {
        buildCamera();
    }

    @Override
    public void onPermissionsDeniedAfterReq(int requestCode, String[] perms) {
        showNoPremissionTip();
    }

    @Override
    public void onPermissionsDeniedAfterReqNoLongerAsk(int requestCode, String[] perms) {
        showNoPremissionTip();
    }

    private void showNoPremissionTip() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("请开启相机权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //  SystemSettingUtil.openApplicationDetailSettings(QrCodeActivity.this);
                    }
                })
                .create().show();
    }

    private void buildCamera() {
        try {
            previewManager.createCamera(surfaceView);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    previewManager.startCallFrame();
                }
            }, 500);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtil.requestPermissionsResult(this, PermissionGroup.CAMERA_REQUEST_CODE, permissions, grantResults, this);
    }
}
