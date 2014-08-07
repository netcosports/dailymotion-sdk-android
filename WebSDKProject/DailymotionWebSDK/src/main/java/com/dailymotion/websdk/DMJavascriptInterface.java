package com.dailymotion.websdk;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Interface used to retrieve data from embed DM video.
 */
public class DMJavascriptInterface {

    /**
     * Name used to identify this interface in javascript code.
     */
    public static final String INTERFACE_NAME = "DMJavascriptInterface";

    /**
     * Javascript var containing video title.
     */
    public static final String VAR_VIDEO_TITLE = "info.title";

    /**
     * Javascript var containing video thumbnail in icon size.
     */
    public static final String VAR_THUMBNAIL_ICON = "info.thumbnail_medium_url";

    /**
     * Javascript var containing video thumbnail in icon card.
     */
    public static final String VAR_THUMBNAIL_CARD = "info.thumbnail_large_url";

    /**
     * Javascript var containing video thumbnail.
     */
    public static final String VAR_THUMBNAIL = "info.thumbnail_url";

    /**
     * Javascript var containing video stream url in HD 1080p.
     */
    public static final String VAR_STREAM_URL_H264_HD_1080p = "info.stream_h264_hd1080_url";

    /**
     * Javascript var containing video stream url in HD.
     */
    public static final String VAR_STREAM_URL_H264_HD = "info.stream_h264_hd_url";

    /**
     * Javascript var containing video stream url in HQ.
     */
    public static final String VAR_STREAM_URL_H264_HQ = "info.stream_h264_hq_url";

    /**
     * Javascript var containing video stream url in LD.
     */
    public static final String VAR_STREAM_URL_H264_LD = "info.stream_h264_ld_url";

    /**
     * Javascript var containing video stream url in HLS format.
     */
    public static final String VAR_STREAM_URL_HLS = "info.stream_hls_url";

    /**
     * Javascript var containing video player src.
     */
    public static final String VAR_PLAYER_SRC = "player.implem.backend.player.src";

    /**
     * Javascript var containing video player current time.
     */
    public static final String VAR_PLAYER_CURRENT_TIME = "player.implem.backend.player.currentTime";

    /**
     * Javascript function used to start the player.
     */
    public static final String FUNCT_PLAYER_PLAY = "player.implem.play()";

    /**
     * Javascript function used to pause the player.
     */
    public static final String FUNCT_PLAYER_PAUSE = "player.implem.pause()";

    /**
     * Javascript function used to know if player is paused or not.
     */
    public static final String FUNCT_PLAYER_IS_PAUSED = "player.implem.paused()";

    /**
     * Javascript function used to auto hide the player on idle.
     */
    public static final String FUNCT_PLAYER_AUTO_HIDE = "setTimeout";

    /**
     * Javascript function used to set current time.
     */
    public static final String FUNCT_PLAYER_SET_CURRENT_TIME = "player.implem.seek(%s)";

    /**
     * Javascript function used to mute the player.
     */
    public static final String FUNCT_PLAYER_MUTE = "player.implem.muted(%b)";

    /**
     * Html division for the start screen.
     */
    public static final String DIV_START_SCREEN = "startscreen";

    /**
     * Html division for the entire video frame.
     */
    public static final String DIV_VIDEO_FRAME = "controls";

    /**
     * Html class for play button div.
     */
    public static final String CLASS_PLAY_BUTTON = "play.button";

    /**
     * Html class for progress bar
     */
    public static final String CLASS_PLAYER_PROGRESS_BAR = "progress-bar-interaction";

    /**
     * Html class for the whole player bar.
     */
    public static final String CLASS_PLAYER_BAR = "bar";

    /**
     * Javascript request which will retrieve video data.
     */
    public static final String REQUEST_VIDEO_DATA = "javascript:" + INTERFACE_NAME +
            ".retrieveVideoData("
            + VAR_VIDEO_TITLE + ","
            + VAR_THUMBNAIL_ICON + ","
            + VAR_THUMBNAIL_CARD + ","
            + VAR_THUMBNAIL + ","
            + VAR_STREAM_URL_H264_HD_1080p + ","
            + VAR_STREAM_URL_H264_HD + ","
            + VAR_STREAM_URL_H264_HQ + ","
            + VAR_STREAM_URL_H264_LD + ","
            + VAR_STREAM_URL_HLS
            + ");";

    /**
     * Javascript request used to start the video player.
     */
    public static final String REQUEST_VIDEO_START = "javascript:" +
            "if(" + VAR_PLAYER_SRC + "==\"\"){" +
            "   $(\"#" + DIV_START_SCREEN + "\").click();" +
            "}else{" +
            "   " + FUNCT_PLAYER_PLAY + ";" + INTERFACE_NAME + ".onPlayerStart();" +
            "}";

    /**
     * Javascript request used to stop the video player.
     */
    public static final String REQUEST_VIDEO_PAUSE = "javascript:" +
            FUNCT_PLAYER_PAUSE + ";";

    /**
     * Javascript request used to set current time of the video.
     */
    public static final String REQUEST_SET_CURRENT_TIME = "javascript:" +
            VAR_PLAYER_CURRENT_TIME + "=%f;";


    /**
     * Javascript request used to register start listener.
     */
    public static final String REQUEST_REGISTRATION_START_LISTENER = "javascript:" +
            "var playHandler = function() {" +
            "   " + INTERFACE_NAME + ".onPlayerStart();" +
            "};" +
            "var screenHandler = function() {" +
            "   " + INTERFACE_NAME + ".onPlayerStart();" +
            "};" +
            "$(\"." + CLASS_PLAY_BUTTON + "\").bind(\"click\",playHandler);" +
            "$(\"#" + DIV_START_SCREEN + "\").bind(\"click\",screenHandler);";

    /**
     * Javascript request used to register resume/stop listener on play button click and on video
     * frame touch.
     */
    public static final String REQUEST_REGISTRATION_PAUSE_RESUME_LISTENER = "javascript:" +
            "$(\"." + CLASS_PLAY_BUTTON + "\").unbind(\"click\",playHandler);" +
            "$(\"#" + DIV_START_SCREEN + "\").unbind(\"click\",screenHandler);" +
            "$(\"." + CLASS_PLAY_BUTTON + "\").bind(\"click\",function(){" +
            "   if(" + FUNCT_PLAYER_IS_PAUSED + "){" +
            "       " + INTERFACE_NAME + ".onPlayerPause();" +
            "   }else{" +
            "       " + INTERFACE_NAME + ".onPlayerResume();" +
            "   }" +
            "});" +
            "$(\"#" + DIV_VIDEO_FRAME + "\").bind(\"click\",function(){" +
            "   if(" + FUNCT_PLAYER_IS_PAUSED + "){" +
            "       " + INTERFACE_NAME + ".onPlayerPause();" +
            "   }else{" +
            "       " + INTERFACE_NAME + ".onPlayerResume();" +
            "   }" +
            "});";

    /**
     * Javascript request used to register progress bar event in order to catch setting
     * new current time event.
     */
    public static final String REQUEST_REGISTRATION_PROGRESS_LISTENER = "javascript:" +
            "$(\"." + CLASS_PLAYER_PROGRESS_BAR + "\").bind(\"touchend\",function(){" +
            "   " + INTERFACE_NAME + ".onCurrentTimeChange(" + VAR_PLAYER_CURRENT_TIME + "*1000);" +
            "});";

    /**
     * Javascript request used to store auto hide timer for future usage.
     */
    public static final String REQUEST_AUTO_HIDE_INITIALIZATION = "javascript:" +
            "var oldSetTimeout = " + FUNCT_PLAYER_AUTO_HIDE + ";";

    /**
     * Javascript request used to disable player auto hiding.
     */
    public static final String REQUEST_DISABLE_AUTO_HIDE = "javascript:" +
            "" + FUNCT_PLAYER_AUTO_HIDE + " = function(){};";

    /**
     * Javascript request used to restore player auto hiding
     */
    public static final String REQUEST_RESTORE_AUTO_HIDE = "javascript:" +
            "" + FUNCT_PLAYER_AUTO_HIDE + " = oldSetTimeout;";
    ;

    /**
     * Javascript request used to display the video player bar.
     */
    public static final String REQUEST_SHOW_PLAYER = "javascript:" +
            "$(\"." + CLASS_PLAYER_BAR + "\").show();" +
            "$(\"#" + DIV_VIDEO_FRAME + "\").addClass(\"visible\");";

    /**
     * Javascript request used to hide the video player bar.
     */
    public static final String REQUEST_HIDE_PLAYER = "javascript:" +
            "$(\"." + CLASS_PLAYER_BAR + "\").hide();";

    /**
     * Javascript request used to mute the video player.
     */
    public static final String REQUEST_PLEYER_MUTE = "javascript:" +
            "" + FUNCT_PLAYER_MUTE + ";";

    /**
     * Log cat
     */
    private static final String TAG = DMJavascriptInterface.class.getName();

    /**
     * Listener for retrieving event.
     */
    private DMJavascriptInterfaceListener mListener;


    /**
     * Default constructor.
     */
    public DMJavascriptInterface(DMJavascriptInterfaceListener listener) {
        mListener = listener;
    }

    /**
     * Called to retrieve video title
     *
     * @param videoTitle          video title
     * @param thumbnailIcon       thumbnail icon size url
     * @param thumbnailCard       thumbnail icon card url
     * @param thumbnail           thumbnail url
     * @param streamUrlH264Hd1080 stream url in HD 1080p
     * @param streamUrlH264Hd     stream url in HD
     * @param streamUrlH264Hq     stream url in HQ
     * @param streamUrlH264Ld     stream url in LD
     * @param streamUrlHls        stream url in HLS
     */
    @JavascriptInterface
    public void retrieveVideoData(String videoTitle,
                                  String thumbnailIcon,
                                  String thumbnailCard,
                                  String thumbnail,
                                  String streamUrlH264Hd1080,
                                  String streamUrlH264Hd,
                                  String streamUrlH264Hq,
                                  String streamUrlH264Ld,
                                  String streamUrlHls) {
        final DMWebVideoModel videoModel = new DMWebVideoModel();
        videoModel.setVideoTitle(videoTitle);
        videoModel.setVideoThumbnailIcon(thumbnailIcon);
        videoModel.setVideoThumbnailCard(thumbnailCard);
        videoModel.setVideoThumbnail(thumbnail);
        videoModel.setStreamUrlH264Hd1080p(streamUrlH264Hd1080);
        videoModel.setStreamUrlH264Hd(streamUrlH264Hd);
        videoModel.setStreamUrlH264Hq(streamUrlH264Hq);
        videoModel.setStreamUrlH264Ld(streamUrlH264Ld);
        videoModel.setStreamUrlHls(streamUrlHls);

        mListener.onVideoDataRetrieved(videoModel);
    }

    /**
     * Called when user clicked on play button or screen to pause the video.
     */
    @JavascriptInterface
    public void onPlayerPause() {
        Log.d("DEBUG===", "onPlayerPause");
        mListener.onVideoPause();
    }

    /**
     * Called when user clicked on play button or screen to resume the video.
     */
    @JavascriptInterface
    public void onPlayerResume() {
        Log.d("DEBUG===", "onPlayerResume");
        mListener.onVideoResume();
    }

    /**
     * Called when user clicked on play button or screen to start the video.
     */
    @JavascriptInterface
    public void onPlayerStart() {
        Log.d("DEBUG===", "onPlayerStart");
        mListener.onVideoStart();
    }

    /**
     * Called when user use th progress bar to change the current time.
     *
     * @param currentTime new current time in millisecond.
     */
    @JavascriptInterface
    public void onCurrentTimeChange(float currentTime) {
        Log.d("DEBUG===", "onPlayerCurrentTimeChange : " + (long) currentTime);
        mListener.onCurrentTimeChange((long) currentTime);
    }

    /**
     * Callback interface.
     */
    public interface DMJavascriptInterfaceListener {
        /**
         * Called from JavaScript when video data has been retrieved.
         *
         * @param data current loaded video model.
         */
        public void onVideoDataRetrieved(DMWebVideoModel data);

        /**
         * Called from JavaScript  when video start the first time.
         */
        public void onVideoStart();

        /**
         * Called from JavaScript when video has been resumed.
         */
        public void onVideoResume();

        /**
         * Called from JavaScript when video has been paused.
         */
        public void onVideoPause();

        /**
         * Called from JavaScript when current time has changed.
         *
         * @param newTime new current time in milli.
         */
        public void onCurrentTimeChange(long newTime);
    }
}
