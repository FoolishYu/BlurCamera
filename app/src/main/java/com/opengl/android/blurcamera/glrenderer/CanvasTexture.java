
package com.opengl.android.blurcamera.glrenderer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

// CanvasTexture is a texture whose content is the drawing on a Canvas.
// The subclasses should override onDraw() to drawExt on the bitmap.
// By default CanvasTexture is not opaque.
abstract class CanvasTexture extends UploadedTexture {
    protected Canvas mCanvas;
    private final Config mConfig;

    public CanvasTexture(int width, int height) {
        mConfig = Config.ARGB_8888;
        setSize(width, height);
        setOpaque(false);
    }

    @SuppressLint("WrongCall")
    @Override
    protected Bitmap onGetBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, mConfig);
        mCanvas = new Canvas(bitmap);
        onDraw(mCanvas, bitmap);
        return bitmap;
    }

    @Override
    protected void onFreeBitmap(Bitmap bitmap) {
        if (!inFinalizer()) {
            bitmap.recycle();
        }
    }

    abstract protected void onDraw(Canvas canvas, Bitmap backing);
}
