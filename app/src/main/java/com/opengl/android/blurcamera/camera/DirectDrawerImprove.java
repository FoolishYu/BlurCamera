package com.opengl.android.blurcamera.camera;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by yutao on 2018/6/21.
 * UPDATE
 */

public class DirectDrawerImprove {
    private static final int FLOAT_SIZE = 4;
    private static final int STRIDE     = 5 * FLOAT_SIZE;
    private static final int POS_OFFSET = 0;
    private static final int UV_OFFSET  = 3;

    private int mOESProgram;
    private int mProgram;
    private int mTexture;

    private int uvTransformHandle;
    private int aPositionHandle;
    private int aInputTexCoordHandle;

    private FloatBuffer vertexBuffer;
    private static final float[] mVertexData = {
            // X    Y       Z     U    V
            -1.0f, -1.0f, 0.0f, 0.0f,  0.0f,
            1.0f, -1.0f, 0.0f, 1.0f,  0.0f,
            -1.0f,  1.0f, 0.0f, 0.0f,  1.0f,
            1.0f,  1.0f, 0.0f, 1.0f,  1.0f
    };

    private static final String mVertexShaderCode =
                    "uniform mat4 uvTransform;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec4 aInputTextureCoord;" +
                    "varying vec2 vTextureCoord;" +
                    "void main()" +
                    "{"+
                    "gl_Position = aPosition;"+
                    "vTextureCoord = (uvTransform * aInputTextureCoord).xy;" +
                    "}";

    private static final String mExtFragmentShaderCode =
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;"+
                    "varying vec2 vTextureCoord;\n"+
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main()\n" +
                    "{\n"+
                    "gl_FragColor=texture2D(sTexture, vTextureCoord);\n" +
                    "}";

    private static final String mFragmentShaderCode =
                    "precision mediump float;"+
                    "varying vec2 vTextureCoord;\n"+
                    "uniform sampler2D sTexture;\n" +
                    "void main()\n" +
                    "{\n"+
                    "gl_FragColor=texture2D(sTexture, vTextureCoord);\n" +
                    "}";

    public DirectDrawerImprove(int textureId) {
        this.mTexture = textureId;
        mProgram = GLUtil.createProgram(mVertexShaderCode, mFragmentShaderCode);
        mOESProgram = GLUtil.createProgram(mVertexShaderCode, mExtFragmentShaderCode);
        vertexBuffer = ByteBuffer.allocateDirect(mVertexData.length * FLOAT_SIZE)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexBuffer.put(mVertexData);
        vertexBuffer.position(0);
    }

    public void drawExternalOES(float[] matrix) {
        GLES20.glUseProgram(mOESProgram);
        GLUtil.checkGLError("glUseProgram");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLUtil.checkGLError("glActiveTexture");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexture);
        GLUtil.checkGLError("glBindTexture");
        aPositionHandle = GLES20.glGetAttribLocation(mOESProgram, "aPosition");
        GLUtil.checkGLError("glGetAttribLocation");
        vertexBuffer.position(POS_OFFSET);// 0
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLUtil.checkGLError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLUtil.checkGLError("glVertexAttribPointer");
        aInputTexCoordHandle = GLES20.glGetAttribLocation(mOESProgram, "aInputTextureCoord");
        vertexBuffer.position(UV_OFFSET);// 0
        GLES20.glEnableVertexAttribArray(aInputTexCoordHandle);
        GLES20.glVertexAttribPointer(aInputTexCoordHandle, 2, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLUtil.checkGLError("glVertexAttribPointer");

        uvTransformHandle = GLES20.glGetUniformLocation(mProgram, "uvTransform");
        GLES20.glUniformMatrix4fv(uvTransformHandle, 1, false, matrix, 0);
        GLUtil.checkGLError("glDrawArrays 0");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLUtil.checkGLError("glDrawArrays 1");
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionHandle);
        GLES20.glDisableVertexAttribArray(aInputTexCoordHandle);
        GLUtil.checkGLError("glDisableVertexAttribArray");

    }


    public void drawBlurBitmap(float[] matrix, Bitmap bitmap) {

        GLES20.glUseProgram(mProgram);
        GLUtil.checkGLError("drawBlurBitmap -4");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLUtil.checkGLError("drawBlurBitmap -3");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, (mTexture + 1));
        GLUtil.checkGLError("drawBlurBitmap -2");
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        /// 底部两句没有设置的时候周围会出现白条
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLUtil.checkGLError("drawBlurBitmap 0");
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        vertexBuffer.position(POS_OFFSET);// 0
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLUtil.checkGLError("drawBlurBitmap 1");
        aInputTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aInputTextureCoord");
        vertexBuffer.position(UV_OFFSET);// 3
        GLES20.glEnableVertexAttribArray(aInputTexCoordHandle);
        GLES20.glVertexAttribPointer(aInputTexCoordHandle, 2, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);

        uvTransformHandle = GLES20.glGetUniformLocation(mProgram, "uvTransform");
        GLUtil.checkGLError("drawBlurBitmap 2");
        GLES20.glUniformMatrix4fv(uvTransformHandle, 1, false, matrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解除纹理绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionHandle);
        GLES20.glDisableVertexAttribArray(aInputTexCoordHandle);
    }

}
