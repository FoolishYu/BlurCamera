package com.opengl.android.blurcamera.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by yutao on 2018/6/14.
 * UPDATE
 */

public class FilterRenderer {
    public static final String TAG="FilterRender";
    private int mTexture=-1;
    private Context mContext;
    private final float vertices[] = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            -1f,0f,
            0f,0f,
            -1f,1f,
            0f,1f,

            0f,0f,
            1f,0f,
            0f,1f,
            1f,1f,

    };
    private final float texturevertices[]={
            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f,

            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f,

            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f,
    };
    private FloatBuffer verticesbBuf;
    private FloatBuffer textureVerticesBuf;

    private String vertexShader;
    private String fragmentShader;
    private int aPositionHandle;
    private int uTextureHandle;
    private int aTexPositionHandle;
    private int program;

    private EffectContext effectContext;
    private Effect effect;
    private int mEffectTexture;
    private Camera.Size mPreviewSize;
    private void applyEffectForTexture(){
        effect=effectContext.getFactory().createEffect(EffectFactory.EFFECT_BRIGHTNESS);
        //effect.setParameter("scale", .5f);;
        effect.setParameter("brightness", 4.0f);
        effect.apply(mTexture,CameraInstance.getInstance().getmPreviewSize().width,CameraInstance.getInstance().getmPreviewSize().height,mEffectTexture);
    }
    private void initFloatBuffer(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesbBuf=byteBuffer.asFloatBuffer();
        verticesbBuf.put(vertices);
        verticesbBuf.position(0);

        byteBuffer=ByteBuffer.allocateDirect(texturevertices.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureVerticesBuf=byteBuffer.asFloatBuffer();
        textureVerticesBuf.put(texturevertices);
        textureVerticesBuf.position(0);
    }
    public void drawTexture(){
        // GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glUseProgram(program);
        //GLES20.glDisable(GLES20.GL_BLEND);
        //applyEffectForTexture();
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        aTexPositionHandle = GLES20.glGetAttribLocation(program, "aTexPosition");
        uTextureHandle = GLES20.glGetUniformLocation(program, "uTexture");

        GLES20.glVertexAttribPointer(aPositionHandle,2,GLES20.GL_FLOAT,false,0,verticesbBuf);
        GLES20.glEnableVertexAttribArray(aPositionHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mTexture);
        GLES20.glUniform1i(uTextureHandle,0);

        GLES20.glVertexAttribPointer(aTexPositionHandle, 2, GLES20.GL_FLOAT, false, 0, textureVerticesBuf);
        GLES20.glEnableVertexAttribArray(aTexPositionHandle);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        for(int index=0;index<3;index++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, index*4, 4);
        }
        GLES20.glDisableVertexAttribArray(aTexPositionHandle);
        GLES20.glDisableVertexAttribArray(aPositionHandle);
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
    public FilterRenderer(Context context){
        this.mTexture=createTextureID();
        this.mContext=context;
        effectContext=EffectContext.createWithCurrentGlContext();
        mEffectTexture=createTextureID();
        initFloatBuffer();
        mPreviewSize=CameraInstance.getInstance().getmPreviewSize();
        vertexShader= GLUtils.loadFromAssetsFile("vertexshader.vs",mContext.getAssets());
        fragmentShader=GLUtils.loadFromAssetsFile("fragmentshader.vs",mContext.getAssets());
        program=GLUtils.createProgram(vertexShader,fragmentShader);
    }
    public int getmTexture(){return mTexture;}
}

