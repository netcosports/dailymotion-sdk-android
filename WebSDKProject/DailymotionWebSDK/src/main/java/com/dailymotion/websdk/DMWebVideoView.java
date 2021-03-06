//
//  DMWebVideoView.java
//  Dailymotion
//
//  Created by Guillaume Lachaud on 30/05/13.
//  Copyright (c) 2013 Dailymotion. All rights reserved.
//
package com.dailymotion.websdk;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.util.List;

public class DMWebVideoView extends WebView {

    public static final String UNDERSCORE = "_";
    private WebSettings                         mWebSettings;
    private WebChromeClient                     mChromeClient;
    private VideoView                           mCustomVideoView;
    private WebChromeClient.CustomViewCallback  mViewCallback;

    private final String                        mEmbedUrl = "http://www.dailymotion.com/embed/video/%s?html=1&fullscreen=%s&autoPlay=%s";
    private final String                        mExtraUA = "; DailymotionEmbedSDK 1.0";
    private FrameLayout                         mVideoLayout;
    private boolean                             mIsFullscreen = false;
    private FrameLayout                         mRootLayout;
    private boolean                             mAllowAutomaticNativeFullscreen = false;
    private boolean                             mIsAutoPlay = false;
    private OnFullscreenListener                mOnFullscreenListener;
    private String                              mUrlPlaying;

    public interface OnFullscreenListener {
        public void onFullscreen(boolean isFullscreen);
    }

    public DMWebVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DMWebVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DMWebVideoView(Context context) {
        super(context);
        init();
    }

    public void setOnFullscreenListener(OnFullscreenListener listener) {
        mOnFullscreenListener = listener;
    }

    private void init(){

        //The topmost layout of the window where the actual VideoView will be added to
        mRootLayout = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();

        mWebSettings = getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setUserAgentString(mWebSettings.getUserAgentString() + mExtraUA);

        mChromeClient = new WebChromeClient(){

            /**
             * The view to be displayed while the fullscreen VideoView is buffering
             * @return the progress view
             */
            @Override
            public View getVideoLoadingProgressView() {
                ProgressBar pb = new ProgressBar(getContext());
                pb.setIndeterminate(true);
                return pb;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                ((Activity) getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
                setFullscreen(true);
                mViewCallback = callback;
                if (view instanceof FrameLayout){
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView){//We are in 2.3
                        VideoView video = (VideoView) frame.getFocusedChild();
                        frame.removeView(video);

                        setupVideoLayout(video);

                        mCustomVideoView = video;
                        mCustomVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                            hideVideoView();
                            }
                        });


                    } else {//Handle 4.x

                        setupVideoLayout(view);

                    }
                }
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
            {
                onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
            }
        };


        setWebChromeClient(mChromeClient);
    }

    public void setVideoId(String videoId){
        mUrlPlaying = String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, mIsAutoPlay);
        loadUrl(mUrlPlaying);
    }

    public void setVideoId(String videoId, boolean autoPlay){
        mIsAutoPlay = autoPlay;
        mUrlPlaying = String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, mIsAutoPlay);
        loadUrl(mUrlPlaying);
    }

    public void setVideoEmbedUrl(String url){
        mUrlPlaying = url;
        loadUrl(mUrlPlaying);
    }

    public void setVideoUrl(String url){
        setVideoId(getVideoIdFromUrl(url));
    }

    public void setVideoUrl(String url, boolean autoPlay){
        setVideoId(getVideoIdFromUrl(url), autoPlay);
    }

    public static String getVideoIdFromUrl(String url)
    {
        Uri uri = Uri.parse(url);
        List<String> segments = uri.getPathSegments();
        if(segments != null && segments.size() != 0) {
            String lastSegment = segments.get(segments.size() - 1);
            String[] splits = lastSegment.split(UNDERSCORE);
            if(splits.length != 0)
                return splits[0];
        }
        return null;
    }

    public void hideVideoView(){
        if(isFullscreen()){
            if(mCustomVideoView != null){
                mCustomVideoView.stopPlayback();
            }
            mRootLayout.removeView(mVideoLayout);
            mViewCallback.onCustomViewHidden();
            mChromeClient.onHideCustomView();
            ((Activity) getContext()).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            setFullscreen(false);
        }


    }

    private void setupVideoLayout(View video){

        /**
         * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
         * But then we have to handle directly back press in our View to exit fullscreen.
         * Otherwise the back button will be handled by the topmost Window, id-est the player controller
         */
        mVideoLayout = new FrameLayout(getContext()){

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    hideVideoView();
                    return true;
                }

                return super.dispatchKeyEvent(event);
            }};

        mVideoLayout.setBackgroundResource(R.color.black);
        mVideoLayout.addView(video);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRootLayout.addView(mVideoLayout, lp);
        setFullscreen(true);
    }

    public boolean isFullscreen(){
        return mIsFullscreen;
    }

    private void setFullscreen(boolean isFullscreen){
        boolean oldState = mIsFullscreen;
        mIsFullscreen = isFullscreen;

        if (mOnFullscreenListener != null && oldState != isFullscreen) {
            mOnFullscreenListener.onFullscreen(isFullscreen);
        }
    }

    public void handleBackPress(Activity activity) {
        if(isFullscreen()){
            hideVideoView();
        } else {
            loadUrl("");//Hack to stop video
            activity.finish();
        }
    }

    public void stop() {
        if (mUrlPlaying != null) {
            loadUrl(mUrlPlaying);
        } else {
            loadUrl("");
        }
    }

    public void load() {
        if (mUrlPlaying != null) {
            loadUrl(mUrlPlaying);
        }
    }

    public void setAllowAutomaticNativeFullscreen(boolean allowAutomaticNativeFullscreen){
        mAllowAutomaticNativeFullscreen = allowAutomaticNativeFullscreen;
    }

    public boolean isAutoPlaying(){
        return mIsAutoPlay;
    }

    public void setAutoPlaying(boolean autoPlay){
        mIsAutoPlay = autoPlay;
    }
}