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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.util.List;

public class DMWebVideoView extends WebView implements DMJavascriptInterface.DMJavascriptInterfaceListener {

    public static final String UNDERSCORE = "_";
    protected WebSettings mWebSettings;
    protected WebChromeClient mChromeClient;
    protected VideoView mCustomVideoView;
    protected WebChromeClient.CustomViewCallback mViewCallback;

    protected final String mEmbedUrl = "http://www.dailymotion.com/embed/video/%s?html=1&fullscreen=%s&autoPlay=%s";
    protected final String mExtraUA = "; DailymotionEmbedSDK 1.0";
    protected FrameLayout mVideoLayout;
    protected boolean mIsFullscreen = false;
    protected FrameLayout mRootLayout;
    protected boolean mAllowAutomaticNativeFullscreen = false;
    protected boolean mIsAutoPlay = false;
    protected OnFullscreenListener mOnFullscreenListener;
    protected String mUrlPlaying;
    /**
     * Video model used to retrieve data related to the current loaded video.
     */
    protected DMWebVideoModel mDmWebVideoModel;

    /**
     * True is the loaded video is being played.
     */
    protected boolean mIsPlaying;

    /**
     * True when play button is bind.
     */
    protected boolean mIsPlayButtonBind;

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

    @Override
    public void onVideoDataRetrieved(DMWebVideoModel data) {
        Log.d("DEBUG===", "onVideoDataRetrieved : " + data.toString());
        mDmWebVideoModel = data;
    }

    @Override
    public void onVideoStart() {
        /**
         * Should always register this listener after the first start since the javascript method
         * attached to the play button and the video screen frame are reset.
         *
         * Should be run on UI thread since onVideoStart is called form javascript.
         */
        if (!mIsPlayButtonBind) {
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DMWebVideoView.this.loadUrl(
                            DMJavascriptInterface.REQUEST_REGISTRATION_PAUSE_RESUME_LISTENER);
                }
            }, 1);
            mIsPlayButtonBind = true;
        }

        mIsPlaying = true;
    }

    @Override
    public void onVideoResume() {
        mIsPlaying = true;
    }

    @Override
    public void onVideoPause() {
        mIsPlaying = false;
    }

    /**
     * Start the player.
     */
    public void play() {
        if (!mIsPlaying) {
            Log.d("DEBUG===", "play");
            mIsPlaying = true;
            this.loadUrl(DMJavascriptInterface.REQUEST_VIDEO_START);
        }

    }

    /**
     * Pause the player.
     */
    public void pause() {
        if (mIsPlaying) {
            Log.d("DEBUG===", "pause");
            mIsPlaying = false;
            this.loadUrl(DMJavascriptInterface.REQUEST_VIDEO_PAUSE);
        }
    }

    /**
     * Move seek bar to the given position.
     *
     * @param time time in seconds.
     */
    public void seek(int time) {
        String js = String.format(DMJavascriptInterface.REQUEST_MOVE_SEEK_BAR, time);
        Log.d("DEBUG===", "seek : time : " + js);
        this.loadUrl(js);
    }

    public void setOnFullscreenListener(OnFullscreenListener listener) {
        mOnFullscreenListener = listener;
    }

    private void init() {

        mIsPlaying = false;
        mIsPlayButtonBind = false;

        //The topmost layout of the window where the actual VideoView will be added to
        mRootLayout = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();

        mWebSettings = getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setUserAgentString(mWebSettings.getUserAgentString() + mExtraUA);

        mChromeClient = new WebChromeClient() {

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
                if (view instanceof FrameLayout) {
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView) {//We are in 2.3
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
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("DEBUG===", "onConsoleMessage : " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
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

        //add javascript interface used to retrieve information from javascript
        this.addJavascriptInterface(new DMJavascriptInterface(this), DMJavascriptInterface.INTERFACE_NAME);

        //set web client to catch loading event
        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                //retrieve video data from the embed player
                DMWebVideoView.this.loadUrl(DMJavascriptInterface.REQUEST_VIDEO_DATA);
                DMWebVideoView.this.loadUrl(DMJavascriptInterface.REQUEST_REGISTRATION_START_LISTENER);
            }
        });
    }

    public void setVideoId(String videoId) {
        setVideoId(videoId, false);
    }

    public void setVideoId(String videoId, boolean autoPlay) {
        mIsAutoPlay = autoPlay;
        mUrlPlaying = String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, mIsAutoPlay);
        Log.d("DEBUG===", "load video : " + mUrlPlaying);
        loadUrl(mUrlPlaying);
    }

    public void setVideoEmbedUrl(String url) {
        mUrlPlaying = url;
        loadUrl(mUrlPlaying);
    }

    public void setVideoUrl(String url) {
        setVideoId(getVideoIdFromUrl(url));
    }

    public void setVideoUrl(String url, boolean autoPlay) {
        setVideoId(getVideoIdFromUrl(url), autoPlay);
    }

    public static String getVideoIdFromUrl(String url) {
        Uri uri = Uri.parse(url);
        List<String> segments = uri.getPathSegments();
        if (segments != null && segments.size() != 0) {
            String lastSegment = segments.get(segments.size() - 1);
            String[] splits = lastSegment.split(UNDERSCORE);
            if (splits.length != 0)
                return splits[0];
        }
        return null;
    }

    public void hideVideoView() {
        if (isFullscreen()) {
            if (mCustomVideoView != null) {
                mCustomVideoView.stopPlayback();
            }
            mRootLayout.removeView(mVideoLayout);
            mViewCallback.onCustomViewHidden();
            mChromeClient.onHideCustomView();
            ((Activity) getContext()).setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            setFullscreen(false);
        }


    }

    private void setupVideoLayout(View video) {

        /**
         * As we don't want the touch events to be processed by the underlying WebView, we do not set the WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE flag
         * But then we have to handle directly back press in our View to exit fullscreen.
         * Otherwise the back button will be handled by the topmost Window, id-est the player controller
         */
        mVideoLayout = new FrameLayout(getContext()) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    hideVideoView();
                    return true;
                }

                return super.dispatchKeyEvent(event);
            }
        };

        mVideoLayout.setBackgroundResource(R.color.black);
        mVideoLayout.addView(video);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        mRootLayout.addView(mVideoLayout, lp);
        setFullscreen(true);
    }

    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    private void setFullscreen(boolean isFullscreen) {
        boolean oldState = mIsFullscreen;
        mIsFullscreen = isFullscreen;

        if (mOnFullscreenListener != null && oldState != isFullscreen) {
            mOnFullscreenListener.onFullscreen(isFullscreen);
        }
    }

    public void handleBackPress(Activity activity) {
        if (isFullscreen()) {
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

    public void setAllowAutomaticNativeFullscreen(boolean allowAutomaticNativeFullscreen) {
        mAllowAutomaticNativeFullscreen = allowAutomaticNativeFullscreen;
    }

    public boolean isAutoPlaying() {
        return mIsAutoPlay;
    }

    public void setAutoPlaying(boolean autoPlay) {
        mIsAutoPlay = autoPlay;
    }
}