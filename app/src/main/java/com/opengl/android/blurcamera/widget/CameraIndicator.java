package com.opengl.android.blurcamera.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opengl.android.blurcamera.R;

import static com.opengl.android.blurcamera.widget.CameraIndicator.CameraMode.DOUBLE_PHOTO_MODE;
import static com.opengl.android.blurcamera.widget.CameraIndicator.CameraMode.PHOTO_MODE;
import static com.opengl.android.blurcamera.widget.CameraIndicator.CameraMode.VIDEO_MODE;

/**
 * Created by yutao on 2018/6/12.
 * UPDATE
 */

public class CameraIndicator extends LinearLayout implements Animation.AnimationListener{
    private static final String TAG = CameraIndicator.class.getSimpleName();
    private static final int CENTER_TEXT_SIZE = 12;
    private static final int OTHER_TEXT_SIZE = 10;
    private static final int ANIMATION_DURATION = 250;
    private Context mContext;
    private TextView mDoublePhoto;
    private TextView mPhoto;
    private TextView mVideo;
    private static int mCenterColor = 0;
    private static final int mOtherColor = Color.WHITE;

    private static int TXT_COLOR_CENTER_R = Color.red(mCenterColor);
    private static int TXT_COLOR_CENTER_G = Color.green(mCenterColor);
    private static int TXT_COLOR_CENTER_B = Color.blue(mCenterColor);

    private static final int TXT_COLOR_OTHERS_R = Color.red(mOtherColor);
    private static final int TXT_COLOR_OTHERS_G = Color.green(mOtherColor);
    private static final int TXT_COLOR_OTHERS_B = Color.blue(mOtherColor);
    private int mCurrentMode = PHOTO_MODE;

    private int mScreenWidth;
    private int mStep;
    private int mOffset;

    private ObjectAnimator mVideoTextSizeAnimator;
    private ObjectAnimator mPhotoTextSizeAnimator;
    private ObjectAnimator mDoublePhotoTextSizeAnimator;

    private ObjectAnimator mLayoutPositionAnimator;
    // color animation
    Center2OtherAnimation center2OtherAnimation;
    Other2CenterAnimation other2CenterAnimation;

    private AnimatorSet mAnimatorSet;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public CameraIndicator(Context context) {
        super(context);
        init(context);
    }

    public CameraIndicator(Context context, AttributeSet attr) {
        super(context, attr);
        init(context);
    }

    public CameraIndicator(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mVideo = (TextView) findViewById(R.id.video_guide);
        mPhoto = (TextView) findViewById(R.id.photo_guide);
        mDoublePhoto = (TextView) findViewById(R.id.double_phto_guide);

        mVideo.setTextSize(OTHER_TEXT_SIZE);
        mDoublePhoto.setTextSize(OTHER_TEXT_SIZE);
        mPhoto.setTextColor(mCenterColor);
        setX(0);
    }

    /**
     * initialize
     */
    private void init(Context context) {
        mContext = context;
        mStep = mContext.getResources().getDimensionPixelSize(R.dimen.module_indicator_width);
        mOffset = mStep / 2;
        mCenterColor = mContext.getResources().getColor(R.color.colorPrimary);
        TXT_COLOR_CENTER_R = Color.red(mCenterColor);
        TXT_COLOR_CENTER_G = Color.green(mCenterColor);
        TXT_COLOR_CENTER_B = Color.blue(mCenterColor);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    /**
     * update to index mode
     * @param index which can be one of  {@link CameraMode} VIDEO_MODE,PHOTO_MODE,DOUBLE_PHOTO_MODE
     */
    public void moveToIndex(int index) {
        Log.e(TAG, "moveToIndex " + "mCurrent " + mCurrentMode + " index " + index);
        if(mCurrentMode == index) return;
        if(mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(ANIMATION_DURATION);
        }
        updateTextSizeAnimation(index);
        updateTextColorAnimation(index);
        updateLayoutPositionAnimation(index);
        mAnimatorSet.playTogether(
                mVideoTextSizeAnimator,
                mPhotoTextSizeAnimator,
                mDoublePhotoTextSizeAnimator,
                mLayoutPositionAnimator
        );
        mAnimatorSet.setInterpolator(mInterpolator);
        mAnimatorSet.start();
        if(center2OtherAnimation != null) center2OtherAnimation.start();
        if(other2CenterAnimation != null) other2CenterAnimation.start();
        mCurrentMode = index;
    }

    /**
     * Update the text size according to the current mode
     * and the next mode which index specify.
     * For the current mode                                 center_text_size -----> other_text_size
     * For the index mode                                   other_text_size  -----> center_text_size
     * For the mode neither current nor the index mode      other_text_size  -----> other_text_size
     * @param index which mode we'll change to.
     */
    private void updateTextSizeAnimation(int index) {
        switch(mCurrentMode) {
            case PHOTO_MODE:
                mPhotoTextSizeAnimator = ObjectAnimator.ofFloat(mPhoto, "TextSize", CENTER_TEXT_SIZE, OTHER_TEXT_SIZE);
                if(DOUBLE_PHOTO_MODE == index) {
                    mDoublePhotoTextSizeAnimator = ObjectAnimator.ofFloat(mDoublePhoto, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                    mVideoTextSizeAnimator = ObjectAnimator.ofFloat(mVideo, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                } else if(VIDEO_MODE == index) {
                    mDoublePhotoTextSizeAnimator = ObjectAnimator.ofFloat(mDoublePhoto, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                    mVideoTextSizeAnimator = ObjectAnimator.ofFloat(mVideo, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                }
                break;
            case VIDEO_MODE:
                mVideoTextSizeAnimator = ObjectAnimator.ofFloat(mVideo, "TextSize", CENTER_TEXT_SIZE, OTHER_TEXT_SIZE);
                if(PHOTO_MODE == index) {
                    mDoublePhotoTextSizeAnimator = ObjectAnimator.ofFloat(mDoublePhoto, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                    mPhotoTextSizeAnimator = ObjectAnimator.ofFloat(mPhoto, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                } else if(DOUBLE_PHOTO_MODE == index) {
                    mDoublePhotoTextSizeAnimator = ObjectAnimator.ofFloat(mDoublePhoto, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                    mPhotoTextSizeAnimator = ObjectAnimator.ofFloat(mPhoto, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                }
                break;
            case DOUBLE_PHOTO_MODE:
                mDoublePhotoTextSizeAnimator = ObjectAnimator.ofFloat(mDoublePhoto, "TextSize", CENTER_TEXT_SIZE, OTHER_TEXT_SIZE);
                if(PHOTO_MODE == index) {
                    mPhotoTextSizeAnimator = ObjectAnimator.ofFloat(mPhoto, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                    mVideoTextSizeAnimator = ObjectAnimator.ofFloat(mVideo, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                } else if(VIDEO_MODE == index) {
                    mVideoTextSizeAnimator = ObjectAnimator.ofFloat(mVideo, "TextSize", OTHER_TEXT_SIZE, CENTER_TEXT_SIZE);
                    mPhotoTextSizeAnimator = ObjectAnimator.ofFloat(mPhoto, "TextSize", OTHER_TEXT_SIZE, OTHER_TEXT_SIZE);
                }
            default:
                break;

        }
    }

    /**
     * update the text color
     * @param index update to the specified mode
     */
    private void updateTextColorAnimation(int index) {
        if(other2CenterAnimation == null) {
            other2CenterAnimation = new Other2CenterAnimation();
        }
        if(center2OtherAnimation == null) {
            center2OtherAnimation = new Center2OtherAnimation();
        }
        switch(mCurrentMode) {
            case PHOTO_MODE:
                center2OtherAnimation.setTextView(mPhoto);
                if(index == DOUBLE_PHOTO_MODE) {
                    other2CenterAnimation.setTextView(mDoublePhoto);
                } else if(index == VIDEO_MODE) {
                    other2CenterAnimation.setTextView(mVideo);
                }
                break;
            case DOUBLE_PHOTO_MODE:
                center2OtherAnimation.setTextView(mDoublePhoto);
                if(index == VIDEO_MODE) {
                    other2CenterAnimation.setTextView(mVideo);
                } else if(index == PHOTO_MODE) {
                    other2CenterAnimation.setTextView(mPhoto);
                }
                break;
            case VIDEO_MODE:
                center2OtherAnimation.setTextView(mVideo);
                if(index == PHOTO_MODE) {
                    other2CenterAnimation.setTextView(mPhoto);
                } else if(index == DOUBLE_PHOTO_MODE) {
                    other2CenterAnimation.setTextView(mDoublePhoto);
                }
                break;
            default:
                break;
        }
    }

    /**
     * update the indicator location to the index mode
     * @param index  which can be one of  {@link CameraMode} VIDEO_MODE,PHOTO_MODE,DOUBLE_PHOTO_MODE
     */
    private void updateLayoutPositionAnimation(int index) {
        Log.e(TAG, "getX " + getX() + " " + (mScreenWidth / 2 - index * mStep - mOffset) + " Offset " + mOffset +" mStep * index " + index * mStep);
        mLayoutPositionAnimator = ObjectAnimator.ofFloat(this, "X", getX(), mScreenWidth / 2 - index * mStep - mOffset);
//        mLayoutPositionAnimator = ObjectAnimator.ofFloat(this, "X", getX(), getX() - (index - mCurrentMode) * mStep);
    }

    private class Center2OtherAnimation extends Animation {
        private TextView mView;

        void setTextView(TextView textView) {
            this.mView = textView;
            this.mView.setAnimation(this);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            int r = (int)(interpolatedTime * TXT_COLOR_OTHERS_R) + (int)((1-interpolatedTime) * TXT_COLOR_CENTER_R);
            int g = (int)(interpolatedTime * TXT_COLOR_OTHERS_G) + (int)((1-interpolatedTime) * TXT_COLOR_CENTER_G);
            int b = (int)(interpolatedTime * TXT_COLOR_OTHERS_B) + (int)((1-interpolatedTime) * TXT_COLOR_CENTER_B);
            int color = Color.rgb(r, g, b);
            mView.setTextColor(color);
        }
    }

    private class Other2CenterAnimation extends Animation {
        private TextView mView;

        void setTextView(TextView textView) {
            this.mView = textView;
            this.mView.setAnimation(this);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            int r = (int)(interpolatedTime * TXT_COLOR_CENTER_R) + (int) ((1-interpolatedTime) * TXT_COLOR_OTHERS_R);
            int g = (int)(interpolatedTime * TXT_COLOR_CENTER_G) + (int) ((1-interpolatedTime) * TXT_COLOR_OTHERS_G);
            int b = (int)(interpolatedTime * TXT_COLOR_CENTER_B) + (int) ((1-interpolatedTime) * TXT_COLOR_OTHERS_B);
            int color = Color.rgb(r, g, b);
            mView.setTextColor(color);
        }
    }

    public interface CameraMode {
        int DOUBLE_PHOTO_MODE = 0;
        int PHOTO_MODE = 1;
        int VIDEO_MODE = 2;

    }

}
