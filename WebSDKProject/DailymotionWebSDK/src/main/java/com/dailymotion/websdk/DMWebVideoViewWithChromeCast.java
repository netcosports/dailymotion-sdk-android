package com.dailymotion.websdk;

import android.content.Context;
import android.util.AttributeSet;

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
    public void onProgressChanged(int progress) {

    }

    @Override
    public void setChromeCastPlayerViewListener(ChromeCastPlayerViewListener listener) {
        mChromeCastPlayerViewListener = listener;
    }

    @Override
    public int getCurrentProgress() {
        return 0;
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
