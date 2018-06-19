package com.opengl.android.blurcamera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.opengl.android.blurcamera.camera.CameraInstance;
import com.opengl.android.blurcamera.camera.DirectDrawer;
import com.opengl.android.blurcamera.camera.FilterRenderer;
import com.opengl.android.blurcamera.camera.GLBitmap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yutao on 2018/6/14.
 * UPDATE
 */

public class CameraSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener, CameraInstance.CamOpenedCallback {
    public static final String TAG="CameraSurfaceView";
    private boolean mShowBitmap = false;
    private Context mContext;
    private SurfaceTexture mSurface;
    private int mTextureID = -1;
    private DirectDrawer mDirectDrawer;
    private FilterRenderer mFilterDrawer;
    GLBitmap glBitmap;
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        glBitmap = new GLBitmap();
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        this.mContext=context;
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInstance.getInstance().doStopPreview();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated...");
        mTextureID = createTextureID();
        mDirectDrawer = new DirectDrawer(mTextureID);
        //mFilterDrawer=new FilterRenderer(mContext);
        //mTextureID=mFilterDrawer.getmTexture();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        CameraInstance.getInstance().doOpenCamera(null);
        if(!CameraInstance.getInstance().isPreviewing()){
            CameraInstance.getInstance().doStartPreview(mSurface, 1.33f);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Log.i(TAG, "onDrawFrame...");

        mSurface.updateTexImage();
        //mFilterDrawer.drawTexture();

        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mDirectDrawer.draw(mtx);
        if(mShowBitmap) {
            glBitmap.loadGLTexture(gl10, mContext);
            glBitmap.draw(gl10);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    @Override
    public void cameraHasOpened() {

    }
    private int createTextureID()
    {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }

    public void setShowBitmap(boolean show) {
        mShowBitmap = show;
    }
}
