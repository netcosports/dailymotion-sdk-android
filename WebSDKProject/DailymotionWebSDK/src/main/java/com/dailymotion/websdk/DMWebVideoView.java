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
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

public class DMWebVideoView extends WebView implements DMJavascriptInterface.DMJavascriptInterfaceListener {

    public static final String UNDERSCORE = "_";
    protected WebSettings mWebSettings;
    protected WebChromeClient mChromeClient;

    protected final String mEmbedUrl = "http://www.dailymotion.com/embed/video/%s?html=1&fullscreen=%s&autoPlay=%s";
    protected final String mExtraUA = "; DailymotionEmbedSDK 1.0";
    protected boolean mIsFullscreen = false;
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

    /**
     * Top most layout used to display the full screen mode.
     */
    private FrameLayout mRootLayout;

    /**
     * Layout params used to add the fullscreen layout.
     */
    private FrameLayout.LayoutParams mFullscreenParams;

    /**
     * Layout that holds the video player in normal mode.
     */
    private RelativeLayout mRealLayout;

    /**
     * Real layout params used to restore the state of the player when leaving fullscreen mode.
     */
    private RelativeLayout.LayoutParams mRealParams;

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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mRealLayout == null) {

            /**
             * Init layout actually holding the player to be able to restore
             * it when leaving fullscreen mode.
             */
            initHoldingLayout();
        }
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
                    DMWebVideoView.this.loadUrl(
                            DMJavascriptInterface.REQUEST_REGISTRATION_FULLSCREEN_TOGGLE_LISTENER);
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

    @Override
    public void onFullscreenButtonToggle() {
        this.post(new Runnable() {
            @Override
            public void run() {
                setFullscreen(!mIsFullscreen, true);
            }
        });
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
     * Display / hide fullscreen button.
     *
     * @param enable true if fullscreen button should be displayed.
     */
    public void enableFullscreenButton(boolean enable) {
        if (enable) {
            this.loadUrl(DMJavascriptInterface.REQUEST_SHOW_FULLSCREEN_BUTTON);
        } else {
            this.loadUrl(DMJavascriptInterface.REQUEST_HIDE_FULLSCREEN_BUTTON);
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

    /**
     * Register listener to catch fullscreen callback.
     *
     * @param listener listener.
     */
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
     * Should be linked to the
     * {@link android.app.Activity#onConfigurationChanged(android.content.res.Configuration)}
     * to display fullscreen mode in landscape orientation.
     *
     * @param newConfig new configuration.
     */
    public void onConfigurationChanged(Configuration newConfig) {
        int orientation = newConfig.orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE && !isFullscreen()) {
            setFullscreen(true, false);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen()) {
            setFullscreen(false, false);
        }
    }

    /**
     * Display the player in fullscreen mode.
     *
     * @param enable   true if the player should be displayed in fullscreen, false to restore it.
     * @param fromUser true if the fullscreen mode is request by the user, false if it's requested from onConfigurationChange.
     */
    public void setFullscreen(boolean enable, boolean fromUser) {

        if (enable && !isFullscreen()) {

            //enable full screen mode if requested and not yet displayed.
            mRealParams = ((RelativeLayout.LayoutParams) this.getLayoutParams());
            mRealLayout.removeView(this);
            mRootLayout.addView(this, mFullscreenParams);

            if (mOnFullscreenListener != null) {
                mOnFullscreenListener.onFullscreen(true, fromUser);
            }
        } else if (!enable && isFullscreen()) {

            //disable fullscreen if requested and fullscreen mode displayed.
            mRootLayout.removeView(this);
            mRealLayout.addView(this, mRealParams);

            if (mOnFullscreenListener != null) {
                mOnFullscreenListener.onFullscreen(false, fromUser);
            }
        }

        mIsFullscreen = enable;

        this.requestLayout();
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

    /**
     * Initialize the Dailymotion web video player view.
     */
    private void init() {

        mIsPlaying = false;
        mIsPlayButtonBind = false;
        mIsFullscreen = false;

        //init top most layout for fullscreen mode
        initRootLayout();

        //init layout which will holds the player in fullscreen mode
        initFullscreenLayoutParams();

        //init webview that holding the player.
        initWebComponent();
    }

    /**
     * Init root layout which will holds the fullscreen layout in fullscreen mode.
     * The topmost layout of the window where the actual VideoView will be added to.
     */
    private void initRootLayout() {
        mRootLayout = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
    }

    /**
     * Init parent layout which actually holds the player.
     * <p/>
     * Currently, only RelativeLayout are available since constrain should be enough to restore
     * the player when leaving the fullscreen mode.
     * <p/>
     * TODO handle LinearLayout by storing the child position.
     */
    private void initHoldingLayout() {

        ViewParent parent = this.getParent();

        if (parent != null) {
            if (!(parent instanceof ViewGroup) || !(parent instanceof RelativeLayout)) {
                throw new IllegalStateException("DMWebVideoView must be holds by a relative layout.");
            } else {
                mRealLayout = ((RelativeLayout) parent);
            }
        }
    }

    /**
     * Init the layout params used to display fullscreen player.
     */
    private void initFullscreenLayoutParams() {
        //init layout params
        mFullscreenParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mFullscreenParams.gravity = Gravity.CENTER;
    }

    /**
     * Init webview and websettings to be able to display th dailymotion player.
     * <p/>
     * Initialize also the javascript interface which will map player button to
     * native events and native callbacks.
     */
    private void initWebComponent() {
        mWebSettings = getSettings();
        mWebSettings.setJavaScriptEnabled(true);
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
                view.loadUrl(url);
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                getContext().startActivity(i);
                return true;
            }
        });
    }

    /**
     * Listener used to catch fullscreen event.
     */
    public interface OnFullscreenListener {
        /**
         * Called when full screen mode is displayed.
         *
         * @param isFullscreen true if fullscreen is up, false otherwise.
         * @param fromUser     true if the fullscreen mode has been set up after user touched
         *                     fullscreen button.
         */
        public void onFullscreen(boolean isFullscreen, boolean fromUser);
    }
}