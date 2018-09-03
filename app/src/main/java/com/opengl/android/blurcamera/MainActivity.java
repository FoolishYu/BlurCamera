package com.opengl.android.blurcamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.opengl.android.blurcamera.camera.CameraInstance;
import com.opengl.android.blurcamera.widget.CameraIndicator;
import com.opengl.android.blurcamera.widget.CameraSurfaceView;

import java.util.List;

import static com.opengl.android.blurcamera.widget.CameraIndicator.CameraMode.DOUBLE_PHOTO_MODE;
import static com.opengl.android.blurcamera.widget.CameraIndicator.CameraMode.VIDEO_MODE;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, CameraSurfaceView.SurfaceCallback{
    private static final String TAG = MainActivity.class.getSimpleName();
    private GestureDetector mGestureDetector;
    private static final int CAMERA_REQUEST_CODE = 1000;
    private int mCurrentMode = CameraIndicator.CameraMode.PHOTO_MODE;
    private CameraIndicator mIndicator;
    private CameraSurfaceView mCameraSurfaceView;
    private boolean mShowRGB;
    private boolean mChangedCamera = false;
    /// 预览frameAvailable开始后才可以切摄像头
    private boolean mCanChangedCamera = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCanChangedCamera) {
                    //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    mShowRGB = !mShowRGB;
                    mChangedCamera = true;
                    mCameraSurfaceView.setShowBitmap(mShowRGB);
                    mCanChangedCamera = false;
                }
            }
        });

        mGestureDetector = new GestureDetector(this, this);
        ConstraintLayout mainContent = (ConstraintLayout) findViewById(R.id.cl_content);
        mainContent.setOnTouchListener(new CLOnTouchListener());
        mIndicator = (CameraIndicator) findViewById(R.id.moduleguide);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCameraSurfaceView != null) {
            mCameraSurfaceView.onResume();
        }
//        mCameraSurfaceView.bringToFront();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCameraSurfaceView != null) {
            mCameraSurfaceView.onPause();
        }
    }

    @Override
    public void surfaceCreated() {

    }

    @Override
    public void frameAvailable() {
        Log.e(TAG, "frameAvailable");
        mCanChangedCamera = true;
        if(mChangedCamera) {
            /// 如果是更改摄像头，则进行显示模糊
            mShowRGB = !mShowRGB;
            mCameraSurfaceView.setShowBitmap(mShowRGB);
            Camera.Size curSize = CameraInstance.getInstance().getmPreviewSize();
            Log.e(TAG, "current previewSize " + curSize.width + "x" + curSize.height);
            List<Camera.Size> lists = CameraInstance.getInstance().getSupportedPreviewSize();
            for(Camera.Size item:lists) {
                Log.e(TAG, "previewSize " + item.width + "x" + item.height);
            }
        } else {
            mChangedCamera = !mChangedCamera;
        }
    }

    private class CLOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e(TAG, "velocityX " + velocityX +" " + velocityY );
        if(velocityX > 0) {
            mCurrentMode--;
            mCurrentMode = mCurrentMode < DOUBLE_PHOTO_MODE ? DOUBLE_PHOTO_MODE : mCurrentMode;
        } else if(velocityX < 0) {
            mCurrentMode++;
            mCurrentMode = mCurrentMode > VIDEO_MODE ? VIDEO_MODE : mCurrentMode;
        }
        mIndicator.moveToIndex(mCurrentMode);
        return true;
    }


}
