package com.dailymotion.websdk;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

/**
 * Helper used to easily managed full screen mode.
 * <p/>
 * The methods
 * {@link DMWebVideoFullscreenHelper#onResume()}
 * {@link DMWebVideoFullscreenHelper#onPause()}
 * {@link DMWebVideoFullscreenHelper#onBackPressed()}
 * {@link DMWebVideoFullscreenHelper#onConfigurationChanged(android.content.res.Configuration)}
 * must be linked to the activity life cycle.
 */
public class DMWebVideoFullscreenHelper implements DMWebVideoView.OnFullscreenListener {

    /**
     * Threshold used to process orientation.
     */
    private static final int THRESHOLD = 10;

    /**
     * Activity holding the video player.
     */
    private Activity mActivity;

    /**
     * Dailymotion player.
     */
    private DMWebVideoView mPlayer;

    /**
     * True if the player is in portrait mode.
     */
    private boolean mIsPortrait = false;

    /**
     * True if the player is in landscape mode.
     */
    private boolean mIsLandscape = false;

    /**
     * Orientation listener used to catch rotation event.
     */
    private OrientationEventListener mOrientationEventListener;

    /**
     * Store the fullscreen listener which could be set in addition to the one used by the helper.
     */
    private DMWebVideoView.OnFullscreenListener mFullscreenListener;

    /**
     * Constructor.
     *
     * @param activity activity holding the player.
     * @param player   player.
     */
    public DMWebVideoFullscreenHelper(Activity activity, DMWebVideoView player) {
        mActivity = activity;
        mPlayer = player;

        mPlayer.setOnFullscreenListener(this);

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mIsPortrait = true;

        setUpOrientationListener();

    }

    @Override
    public void onFullscreen(boolean isFullscreen, boolean fromUser) {
        //handle orientation if the fullscreen has been setup from fullscreen button.
        if (fromUser) {
            int orientation = mActivity.getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen) {

                //fullscreen set up but device in portrait : request landscape
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE && !isFullscreen) {

                //fullscreen disable but device in landscape : request portrait
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }

            mOrientationEventListener.enable();
        }

        if (mFullscreenListener != null) {
            mFullscreenListener.onFullscreen(isFullscreen, fromUser);
        }
    }

    /**
     * Must be linked to the onResume of the holding activity.
     */
    public void onResume() {
        if (mOrientationEventListener != null
                && mOrientationEventListener.canDetectOrientation()) {
            if (mActivity.getRequestedOrientation()
                    != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                mOrientationEventListener.enable();
            }
        }
    }

    /**
     * Must be linked to the onPause of the holding activity.
     */
    public void onPause() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    /**
     * Must be linked the onBackPressed of the holding activity.
     *
     * @return true if back pressed has been catch.
     */
    public boolean onBackPressed() {
        if (mPlayer != null && mPlayer.isFullscreen()) {
            mPlayer.setFullscreen(false, true);
            return true;
        }
        return false;
    }

    /**
     * Must be linked to the onConfigurationChanged of the holding activity.
     *
     * @param newConfig new configuration.
     */
    public void onConfigurationChanged(Configuration newConfig) {
        mPlayer.onConfigurationChanged(newConfig);
    }

    /**
     * Register listener to catch fullscreen callback.
     *
     * @param listener listener.
     */
    public void setOnFullscreenListener(DMWebVideoView.OnFullscreenListener listener) {
        mFullscreenListener = listener;
    }

    /**
     * Initialize the local orientation listener.
     */
    private void setUpOrientationListener() {
        mOrientationEventListener
                = new OrientationEventListener(mActivity, SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (!mIsPortrait && !mIsLandscape) {
                    mIsPortrait = isPortrait(orientation);
                    if (!mIsPortrait) {
                        mIsLandscape = isLandscape(orientation);
                    }
                } else {
                    if ((mIsPortrait && isLandscape(orientation))
                            || (mIsLandscape && isPortrait(orientation))) {
                        if (mActivity == null) {
                            this.disable();
                            return;
                        }

                        mActivity.setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        mIsPortrait = false;
                        mIsLandscape = false;
                        this.disable();
                    }
                }
            }
        };
    }

    /**
     * Used to know if the player is displayed in landscape.
     *
     * @param orientation current device orientation.
     * @return true if the player is displayed in landscape.
     */
    private boolean isLandscape(int orientation) {
        return (orientation >= (90 - THRESHOLD) && orientation <= (90 + THRESHOLD)
                || orientation >= (270 - THRESHOLD) && orientation <= (270 + THRESHOLD));
    }

    /**
     * Used to know if the player is displayed in portrait.
     *
     * @param orientation current device orientation.
     * @return true if the player is displayed in portrait.
     */
    private boolean isPortrait(int orientation) {
        return ((orientation >= (360 - THRESHOLD) && orientation <= 360)
                || (orientation >= 0 && orientation <= THRESHOLD))
                ||
                ((orientation >= (180 - THRESHOLD) && orientation <= (180 + THRESHOLD)));
    }

}
