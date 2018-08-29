package com.opengl.android.blurcamera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.opengl.android.blurcamera.MainActivity;
import com.opengl.android.blurcamera.R;
import com.opengl.android.blurcamera.camera.CameraInstance;
import com.opengl.android.blurcamera.camera.DirectDrawerImprove;
import com.opengl.android.blurcamera.camera.GLBitmap;
import com.opengl.android.blurcamera.camera.GLUtil;

import net.qiujuer.genius.blur.StackBlur;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yutao on 2018/6/14.
 * UPDATE
 */

public class CameraSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener, CameraInstance.CamOpenedCallback {
    public static final String TAG="CameraSurfaceView";
    private boolean mShowBitmap = false;
    private boolean front = true;
    private Context mContext;
    private SurfaceTexture mSurface;
    private int frameNum = 0;
    private static final int dropFrameCount = 6;
    private SurfaceCallback mSurfaceCallback;
    //private DirectDrawer mDirectDrawer;
    private DirectDrawerImprove mDirectDrawer;
    private Bitmap bitmap;
    GLBitmap glBitmap;
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        glBitmap = new GLBitmap();
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        this.mContext=context;
        mSurfaceCallback = (MainActivity)context;
        bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.rb);
    }

    public interface SurfaceCallback {
        void surfaceCreated();
        void frameAvailable();
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInstance.getInstance().doStopCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        int mTextureID = createTextureID();
        Log.i(TAG, "onSurfaceCreated..." + mTextureID);
        mDirectDrawer = new DirectDrawerImprove(mTextureID);

        GLUtil.checkGLError("onSurfaceCreated 1");
//        if(front) {
//            mDirectDrawer.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
//        } else {
//            mDirectDrawer.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
//        }
        //mFilterDrawer=new FilterRenderer(mContext);
        //mTextureID=mFilterDrawer.getmTexture();
        mSurface = new SurfaceTexture(mTextureID);

        GLUtil.checkGLError("onSurfaceCreated 2");
        mSurface.setOnFrameAvailableListener(this);
        GLUtil.checkGLError("onSurfaceCreated 3");
        CameraInstance.getInstance().doOpenCamera(mContext, null, front);
        if(!CameraInstance.getInstance().isPreviewing()){
            CameraInstance.getInstance().doStartPreview(mSurface, 1.33f);
        }

        GLUtil.checkGLError("onSurfaceCreated 4");
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        GLUtil.checkGLError("onSurfaceChanged 4");
    }

    private float[]  oldMtx = null;
    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLUtil.checkGLError("glClearColor");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLUtil.checkGLError("glClear");
        Log.i(TAG, "onDrawFrame...");

        mSurface.updateTexImage();
        //mFilterDrawer.drawTexture();

        GLUtil.checkGLError("onDrawFrame 1 ");
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);

        GLUtil.checkGLError("onDrawFrame 2");
        //Log.e(TAG, "transformMatrix :" + Arrays.toString(mtx));
        mDirectDrawer.drawExternalOES(mtx);

        GLUtil.checkGLError("onDrawFrame 3");

        if(oldMtx == null) {
            oldMtx = mtx;
        } else if(!Arrays.equals(oldMtx, mtx)) {
            if(frameNum == -1) {
                oldMtx = mtx;
            }
        }

        GLUtil.checkGLError("onDrawFram 4e");
        if(mShowBitmap) {
            mDirectDrawer.drawBlurBitmap(oldMtx, bitmap);
        }

        GLUtil.checkGLError("onDrawFrame 5");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
        if (frameNum >= 0 && frameNum < dropFrameCount) {
            frameNum++;
        } else if (frameNum == dropFrameCount) {
//            if(front) {
//                mDirectDrawer.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
//            } else {
//                mDirectDrawer.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
//            }
            mSurfaceCallback.frameAvailable();
            frameNum = -1;
        }
    }


    @Override
    public void cameraHasOpened() {

    }
    private int createTextureID()
    {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLUtil.checkGLError("glGenTextures");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLUtil.checkGLError("glBindTexture");
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return texture[0];
    }


    public void setShowBitmap(boolean show) {
        mShowBitmap = show;
        if(mShowBitmap) {

            byte[] yuv = CameraInstance.getInstance().getPreviewData();
            bitmap = StackBlur.blurYuv(yuv, CameraInstance.getInstance().getmPreviewSize().width, CameraInstance.getInstance().getmPreviewSize().height, 8);
            try {
                front = !front;

                CameraInstance.getInstance().doStopCamera();
                CameraInstance.getInstance().doOpenCamera(mContext,null, front);
                frameNum = 0;
                CameraInstance.getInstance().doStartPreview(mSurface, 1.33f);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
            requestRender();
        }
    }
}
