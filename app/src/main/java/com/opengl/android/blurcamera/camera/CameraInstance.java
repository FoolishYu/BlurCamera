package com.opengl.android.blurcamera.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by yutao on 2018/6/14.
 * UPDATE
 */

public class CameraInstance {
    private static final String TAG="CameraInstance";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private static CameraInstance mCameraInstance;
    private boolean isPreviewing=false;
    private boolean isOpened=false;
    private boolean isFront = false;
    private Camera.Size mPreviewSize;
    private Camera.Size mPictureSize;
    public interface CamOpenedCallback{
        public void cameraHasOpened();
    }
    public boolean isPreviewing(){
        return isPreviewing;
    }
    public boolean isOpened(){
        return isOpened;
    }
    public Camera.Size getmPreviewSize(){
        if(mCamera != null) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    public List<Camera.Size> getSupportedPreviewSize() {
        if(mCamera != null) {
            return mCamera.getParameters().getSupportedPreviewSizes();
        }
        return null;
    }

    private CameraInstance(){

    }
    public static synchronized CameraInstance getInstance(){
        if(mCameraInstance == null){
            mCameraInstance = new CameraInstance();
        }
        return mCameraInstance;
    }

    public void doOpenCamera(CamOpenedCallback callback, boolean front){
        Log.i(TAG, "doOpenCamera....");
        if(mCamera == null){
            if(front) {
                isFront = front;
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                isFront = front;
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }

            isOpened=true;
            Log.i(TAG, "Camera open over....");
            if(callback != null){
                callback.cameraHasOpened();
            }
        }else{
            Log.i(TAG, "Camera is in open status");
        }


    }

    public void doStartPreview(SurfaceTexture surface, float previewRate){
        Log.i(TAG, "doStartPreview...");
        if(isPreviewing){
            Log.e(TAG,"camera is in previewing state");
            return ;
        }
        if(mCamera != null){
            try {
                mCamera.setPreviewTexture(surface);
                mCamera.setPreviewCallback(mPreviewCallback);
                Camera.Parameters param = mCamera.getParameters();
//                if(isFront) {
//                    param.setPreviewSize(720, 720);
//                } else {
//                    param.setPreviewSize(1920, 1080);
//                }
                mCamera.setParameters(param);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //initCameraParams();
        }

    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera(){
        if(null != mCamera)
        {
            isOpened=false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }
    public void doStopPreview(){
        Log.e(TAG,"doStopPreview....");
        if (isPreviewing && null!=mCamera){
            mCamera.stopPreview();
            isPreviewing=false;
        }else{
            Log.e(TAG,"camera is in not in previewing status");
        }
    }

    public void doTakePicture(){
        if(isPreviewing && (mCamera != null)){
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }
    private void initCameraParams(){
        if(mCamera != null){

            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);

//            mPictureSize = CamParaUtil.getInstance().getPropPictureSize(
//                    mParams.getSupportedPictureSizes(),30, 800);
            //mPictureSize = mCamera. new Size(1920, 1080);
            //mParams.setPictureSize(mPictureSize.width, mPictureSize.height);
//            mPreviewSize = CamParaUtil.getInstance().getPropPreviewSize(
//                    mParams.getSupportedPreviewSizes(), 30, 800);
            mPreviewSize = mCamera .new Size(1920, 1080);
            mParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

            mCamera.setDisplayOrientation(90);

            List<String> focusModes = mParams.getSupportedFocusModes();
            if(focusModes.contains("continuous-picture")){
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(mParams);
            mCamera.startPreview();

            isPreviewing = true;
            mParams = mCamera.getParameters();

        }
    }

    public byte[] getPreviewData() {
        return mPreviewData;
    }
    private byte[] mPreviewData;
    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mPreviewData = data;
        }
    };

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };
    Camera.PictureCallback mRawCallback = new Camera.PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };
    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback ()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if(null != data){
                b = BitmapFactory.decodeByteArray(data, 0, data.length);
                mCamera.stopPreview();
                isPreviewing = false;
            }

            if(null != b)
            {
                // TODO: save the bitmap
                //Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
                //FileUtil.saveBitmap(rotaBitmap);
            }

            mCamera.startPreview();
            isPreviewing = true;
        }
    };
}
