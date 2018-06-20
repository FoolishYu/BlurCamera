/*
 * Copyright (C) 2014-2016 Qiujuer <qiujuer@live.cn>
 * WebSite http://www.qiujuer.net
 * Created 04/28/2015
 * Changed 05/29/2016
 * Version 2.0.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <android/log.h>
#include <android/bitmap.h>
#include "stackblur.h"

#define TAG "net_qiujuer_genius_blur_StackNative"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)

JNIEXPORT void JNICALL
Java_net_qiujuer_genius_blur_StackNative_blurPixels
        (JNIEnv
         *env,
         jclass obj, jintArray
         arrIn,
         jint w, jint
         h,
         jint r
        ) {
    jint *pixels;
    // cpp:
    // pix = (env)->GetIntArrayElements(arrIn, 0);
    pixels = (*env)->GetIntArrayElements(env, arrIn, 0);
    if (pixels == NULL) {
        LOG_D("Input pixels isn't null.");
        return;
    }

    // Start
    pixels = blur_ARGB_8888(pixels, w, h, r);
    // End

    // if return:
    // int size = w * h;
    // jintArray result = env->NewIntArray(size);
    // env->SetIntArrayRegion(result, 0, size, pix);
    // cpp:
    // (env)->ReleaseIntArrayElements(arrIn, pix, 0);
    (*env)->ReleaseIntArrayElements(env, arrIn, pixels, 0);
    // return result;
}

JNIEXPORT void JNICALL
Java_net_qiujuer_genius_blur_StackNative_blurBitmap
        (JNIEnv
         *env,
         jclass obj, jobject
         bitmapIn,
         jint r
        ) {
    AndroidBitmapInfo infoIn;
    void *pixels;

    // Get image info
    if (AndroidBitmap_getInfo(env, bitmapIn, &infoIn) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOG_D("AndroidBitmap_getInfo failed!");
        return;
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        infoIn.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOG_D("Only support ANDROID_BITMAP_FORMAT_RGBA_8888 and ANDROID_BITMAP_FORMAT_RGB_565");
        return;
    }

    // Lock all images
    if (AndroidBitmap_lockPixels(env, bitmapIn, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOG_D("AndroidBitmap_lockPixels failed!");
        return;
    }
    // height width
    int h = infoIn.height;
    int w = infoIn.width;

    // Start
    if (infoIn.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        pixels = blur_ARGB_8888((int *) pixels, w, h, r);
    } else if (infoIn.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        pixels = blur_RGB_565((short *) pixels, w, h, r);
    }

    // End

    // Unlocks everything
    AndroidBitmap_unlockPixels(env, bitmapIn);
}

void YUVtoARBG(JNIEnv * env, jbyteArray yuv420sp, jint width, jint height, jintArray rgbOut)
{
    int             sz;
    int             i;
    int             j;
    int             Y;
    int             Cr = 0;
    int             Cb = 0;
    int             pixPtr = 0;  //Y所占的空间
    int             jDiv2 = 0;  //uv前面行所占的空间
    int             R = 0;
    int             G = 0;
    int             B = 0;
    int             cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jint *rgbData = (jint*) ((*env)->GetPrimitiveArrayCritical(env, rgbOut, 0));
    jbyte* yuv = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);

    for(j = 0; j < h; j++) {
        pixPtr = j * w;     //Y所占的空间
        jDiv2 = j >> 1;    //除以2向下取整
        for(i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if(Y < 0) Y += 255;
            //用位运算判断是奇数还是偶数
            if((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;  //计算 UV的位置  (i>>1)*2就是为了变成偶数  向下取整  因为U的游标都是偶数位
                Cb = yuv[cOff];
                if(Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if(Cr < 0) Cr += 127; else Cr -= 128;
            }

            //ITU-R BT.601 conversion
            //
            //     R = 1.164*(Y-16)+1.596*(Cr-128)
            //     G = 1.164*(Y-16)-0.392*(Cb-128)-0.813*(Cr-128)
            //     B = 1.164*(Y-16)+2.017*(Cb-128)
            //

            Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);    //Y=Y+Y*0.125+0.03125+0.00078
            R = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
            if(R < 0) R = 0; else if(R > 255) R = 255;
            G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
            if(G < 0) G = 0; else if(G > 255) G = 255;
            B = Y + (Cr << 1) + (Cr >> 6);
            if(B < 0) B = 0; else if(B > 255) B = 255;
            rgbData[pixPtr++] = 0xff000000 + (R << 16) + (G << 8) + B;
        }
    }

    (*env)->ReleasePrimitiveArrayCritical(env, rgbOut, rgbData, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);
}

JNIEXPORT void JNICALL
Java_net_qiujuer_genius_blur_StackNative_yuv2rgb(JNIEnv *env, jclass type, jbyteArray yuv_,
                                                 jint width, jint height, jintArray rgba_) {
    YUVtoARBG(env, yuv_, width, height, rgba_);
}