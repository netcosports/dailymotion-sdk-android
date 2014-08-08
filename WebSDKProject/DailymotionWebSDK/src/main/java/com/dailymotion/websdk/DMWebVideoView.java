//
//  DMWebVideoView.java
//  Dailymotion
//
//  Created by Guillaume Lachaud on 30/05/13.
//  Copyright (c) 2013 Dailymotion. All rights reserved.
//
package com.dailymotion.websdk;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
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

    /**
     * Timestamp set when current time changed.
     */
    protected long mLastCurrentTimeUpdate;

    /**
     * Last known current time in milli.
     */
    protected long mLastCurrenTime;

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

                    //register listener on html component only available once video has started
                    DMWebVideoView.this.loadUrl(
                            DMJavascriptInterface.REQUEST_REGISTRATION_PAUSE_RESUME_LISTENER);
                    DMWebVideoView.this.loadUrl(
                            DMJavascriptInterface.REQUEST_REGISTRATION_PROGRESS_LISTENER);
                }
            }, 1);
            mIsPlayButtonBind = true;
            mLastCurrentTimeUpdate = System.currentTimeMillis();
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

    @Override
    public void onCurrentTimeChange(long newTime) {
        mLastCurrentTimeUpdate = System.currentTimeMillis();
        mLastCurrenTime = newTime;
    }

    /**
     * Start the player.
     */
    public void play() {
        if (!mIsPlaying) {
            mIsPlaying = true;
            this.loadUrl(DMJavascriptInterface.REQUEST_VIDEO_START);
        }

    }

    /**
     * Pause the player.
     */
    public void pause() {
        if (mIsPlaying) {
            mIsPlaying = false;
            this.loadUrl(DMJavascriptInterface.REQUEST_VIDEO_PAUSE);
        }
    }

    /**
     * Set current time to loaded video.
     *
     * @param time time in millseconds
     */
    public void setCurrentTime(long time) {
        String js = String.format(DMJavascriptInterface.REQUEST_SET_CURRENT_TIME, (float) time / 1000);
        this.loadUrl(js);
    }

    /**
     * Show the player bar.
     */
    public void displayPlayerBar() {
        this.loadUrl(DMJavascriptInterface.REQUEST_SHOW_PLAYER);
    }

    /**
     * Hide the player bar.
     */
    public void hidePlayerBar() {
        this.loadUrl(DMJavascriptInterface.REQUEST_HIDE_PLAYER);
    }

    /**
     * Use to control the auto hiding behavior of the player bar.
     *
     * @param enable true if auto hiding should be enable
     */
    public void enableAutoHiding(boolean enable) {
        if (enable) {
            //should restore auto hiding
            this.loadUrl(DMJavascriptInterface.REQUEST_RESTORE_AUTO_HIDE);

            //hide the player bar if the video is playing
            if (mIsPlaying) {
                hidePlayerBar();
            }
        } else {
            //should disable auto hiding
            this.loadUrl(DMJavascriptInterface.REQUEST_DISABLE_AUTO_HIDE);

            //display the player bar if the video is playing
            if (mIsPlaying) {
                displayPlayerBar();
            }
        }
    }

    /**
     * Display / hide social icons (FB and Twitter)
     *
     * @param enable true if social bar should be displayed.
     */
    public void enableSocialBar(boolean enable) {
        if (enable) {
            this.loadUrl(DMJavascriptInterface.REQUEST_SHOW_SOCIAL_BAR);
        } else {
            this.loadUrl(DMJavascriptInterface.REQUEST_HIDE_SOCIAL_BAR);
        }
    }

    /**
     * Enable and disable mute.
     *
     * @param muteRequest true to mute the player.
     */
    public void mute(boolean muteRequest) {
        this.loadUrl(String.format(DMJavascriptInterface.REQUEST_PLAYER_MUTE, muteRequest));
    }

    public void setOnFullscreenListener(OnFullscreenListener listener) {
        mOnFullscreenListener = listener;
    }

    public void setVideoId(String videoId) {
        setVideoId(videoId, false);
    }

    public void setVideoId(String videoId, boolean autoPlay) {
        mIsAutoPlay = autoPlay;
        mUrlPlaying = String.format(mEmbedUrl, videoId, mAllowAutomaticNativeFullscreen, mIsAutoPlay);
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

    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    /**
     * Called when the video has been loaded.
     */
    protected void onVideoLoaded() {
        //retrieve video data from the embed player
        DMWebVideoView.this.loadUrl(
                DMJavascriptInterface.REQUEST_VIDEO_DATA);

        //register listener on html widget
        DMWebVideoView.this.loadUrl(
                DMJavascriptInterface.REQUEST_REGISTRATION_START_LISTENER);
        DMWebVideoView.this.loadUrl(
                DMJavascriptInterface.REQUEST_AUTO_HIDE_INITIALIZATION);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

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
                DMWebVideoView.this.onVideoLoaded();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
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

    private void setFullscreen(boolean isFullscreen) {
        boolean oldState = mIsFullscreen;
        mIsFullscreen = isFullscreen;

        if (mOnFullscreenListener != null && oldState != isFullscreen) {
            mOnFullscreenListener.onFullscreen(isFullscreen);
        }
    }

    public interface OnFullscreenListener {
        public void onFullscreen(boolean isFullscreen);
    }
}