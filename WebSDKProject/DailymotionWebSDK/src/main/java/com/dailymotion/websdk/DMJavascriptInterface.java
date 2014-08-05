package com.dailymotion.websdk;

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
     * Callback interface.
     */
    public interface DMJavascriptInterfaceListener {
        /**
         * Called when video data has been retrieved
         *
         * @param data current loaded video model.
         */
        public void onVideoDataRetrieved(DMWebVideoModel data);
    }
}
