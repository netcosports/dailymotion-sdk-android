package com.dailymotion.websdk;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.google.sample.castcompanionlibrary.cast.abstracts.ChromeCastPlayerView;

/**
 * DailyMotion video player based on {@link DMWebVideoView} which can be
 * Chromecasted.
 */
public class DMWebVideoViewWithChromeCast extends DMWebVideoView implements ChromeCastPlayerView {

    /**
     * Listener used to catch player event.
     */
    private ChromeCastPlayerViewListener mChromeCastPlayerViewListener;

    public DMWebVideoViewWithChromeCast(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DMWebVideoViewWithChromeCast(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DMWebVideoViewWithChromeCast(Context context) {
        super(context);
    }

    @Override
    public void onChromeCastReady() {

        //send current DM video to the ChromeCast
        mChromeCastPlayerViewListener.onMediaSelected(
                mDmWebVideoModel.getHigherQualityAvailableStreamUrl(),
                mDmWebVideoModel.getVideoThumbnailCard(),
                mDmWebVideoModel.getVideoTitle()
        );
    }

    @Override
    public void onPlayVideoRequested() {
        this.play();
    }

    @Override
    public void onPauseVideoRequested() {
        this.pause();
    }

    @Override
    public void onSynchronizedProgressRequested(long progress) {
        Log.d("DEBUG===", "onProgressChanges : " + progress);
        this.setCurrentTime(progress);
    }

    @Override
    public void onVideoResume() {
        super.onVideoResume();
        mChromeCastPlayerViewListener.onPlayerPlayClicked();
    }

    @Override
    public void onVideoPause() {
        super.onVideoPause();
        mChromeCastPlayerViewListener.onPlayerPauseClicked();
    }

    @Override
    public void onCurrentTimeChange(long newTime) {
        super.onCurrentTimeChange(newTime);
        mChromeCastPlayerViewListener.onCurrentTimeChanged(newTime);
    }

    @Override
    public void setChromeCastPlayerViewListener(ChromeCastPlayerViewListener listener) {
        mChromeCastPlayerViewListener = listener;
    }

    @Override
    public int getCurrentTimeMillis() {
        int current = 0;
        if (mIsPlaying) {
            current = (int) (System.currentTimeMillis() - mLastCurrentTimeUpdate);
        } else {
            current = (int) mLastCurrenTime;
        }
        Log.d("DEBUG===", "getCurrentTimeMillis : " + current);
        return current;
    }


    @Override
    public void setAlwaysShowController() {

    }

    @Override
    public void setControllerTimeOut(int timeOut) {

    }

    @Override
    public int getDefaultTimeOut() {
        return 0;
    }
}
