package com.dailymotion.websdk;

/**
 * Model used to encapsulate all data related to video embed in DM web player.
 */
public class DMWebVideoModel {

    /**
     * Video title.
     */
    private String mVideoTitle;

    /**
     * Video thumbnail in icon size 160x120.
     */
    private String mVideoThumbnailIcon;

    /**
     * Video thumbnail in card size 427x240.
     */
    private String mVideoThumbnailCard;

    /**
     * Video thumbnail in large size 936x527.
     */
    private String mVideoThumbnail;

    /**
     * Stream url in High Definition 1080p.
     */
    private String mStreamUrlH264Hd1080p;

    /**
     * Stream url in High Definition.
     */
    private String mStreamUrlH264Hd;

    /**
     * Stream url in High Quality.
     */
    private String mStreamUrlH264Hq;

    /**
     * Stream url in Low Definition.
     */
    private String mStreamUrlH264Ld;

    /**
     * Stream url in HLS format.
     */
    private String mStreamUrlHls;

    /**
     * Default constructor.
     */
    public DMWebVideoModel() {
        this.mVideoTitle = null;
        this.mVideoThumbnailIcon = null;
        this.mVideoThumbnailCard = null;
        this.mVideoThumbnail = null;
        this.mStreamUrlH264Hd1080p = null;
        this.mStreamUrlH264Hd = null;
        this.mStreamUrlH264Hq = null;
        this.mStreamUrlH264Ld = null;
        this.mStreamUrlHls = null;
    }

    public String toString() {
        return "\nvideo title : " + this.mVideoTitle +
                "\nthumbnail icon : " + this.mVideoThumbnailIcon +
                "\nthumbnail card : " + this.mVideoThumbnailCard +
                "\nthumbnail : " + this.mVideoThumbnail +
                "\nstream url h264 HD 1080p : " + this.mStreamUrlH264Hd1080p +
                "\nstream url h264 HD : " + this.mStreamUrlH264Hd +
                "\nstream url h264 HQ : " + this.mStreamUrlH264Hq +
                "\nstream url h264 LD : " + this.mStreamUrlH264Ld +
                "\nstream url h264 Hls: " + this.mStreamUrlHls;
    }

    /**
     * Use to retrieve the high quality of stream available for this video.
     *
     * @return stream url or null if any stream url is available.
     */
    public String getHigherQualityAvailableStreamUrl() {
        if (mStreamUrlH264Hd1080p != null) {
            return mStreamUrlH264Hd1080p;
        } else if (mStreamUrlH264Hd != null) {
            return mStreamUrlH264Hd;
        } else if (mStreamUrlH264Hq != null) {
            return mStreamUrlH264Hq;
        } else if (mStreamUrlH264Ld != null) {
            return mStreamUrlH264Ld;
        } else {
            return null;
        }
    }

    /**
     * Use to retrieve the lower quality of stream available for this video.
     *
     * @return stream url or null if any stream url is available.
     */
    public String getLowerQualityAvailableStreamUrl() {
        if (mStreamUrlH264Ld != null) {
            return mStreamUrlH264Ld;
        } else if (mStreamUrlH264Hq != null) {
            return mStreamUrlH264Hq;
        } else if (mStreamUrlH264Hd != null) {
            return mStreamUrlH264Hd;
        } else if (mStreamUrlH264Hd1080p != null) {
            return mStreamUrlH264Hd1080p;
        } else {
            return null;
        }
    }

    /**
     * *******************************
     * ******* GETTER & SETTER *********
     * *******************************
     */

    public String getStreamUrlHls() {
        return mStreamUrlHls;
    }

    public void setStreamUrlHls(String streamUrlHls) {
        this.mStreamUrlHls = streamUrlHls;
    }

    public String getStreamUrlH264Ld() {
        return mStreamUrlH264Ld;
    }

    public void setStreamUrlH264Ld(String streamUrlH264Ld) {
        this.mStreamUrlH264Ld = streamUrlH264Ld;
    }

    public String getStreamUrlH264Hq() {
        return mStreamUrlH264Hq;
    }

    public void setStreamUrlH264Hq(String streamUrlH264Hq) {
        this.mStreamUrlH264Hq = streamUrlH264Hq;
    }

    public String getStreamUrlH264Hd() {
        return mStreamUrlH264Hd;
    }

    public void setStreamUrlH264Hd(String streamUrlH264Hd) {
        this.mStreamUrlH264Hd = streamUrlH264Hd;
    }

    public String getStreamUrlH264Hd1080p() {
        return mStreamUrlH264Hd1080p;
    }

    public void setStreamUrlH264Hd1080p(String streamUrlH264Hd1080p) {
        this.mStreamUrlH264Hd1080p = streamUrlH264Hd1080p;
    }

    public String getVideoThumbnail() {
        return mVideoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.mVideoThumbnail = videoThumbnail;
    }

    public String getVideoThumbnailCard() {
        return mVideoThumbnailCard;
    }

    public void setVideoThumbnailCard(String videoThumbnailCard) {
        this.mVideoThumbnailCard = videoThumbnailCard;
    }

    public String getVideoThumbnailIcon() {
        return mVideoThumbnailIcon;
    }

    public void setVideoThumbnailIcon(String videoThumbnailIcon) {
        this.mVideoThumbnailIcon = videoThumbnailIcon;
    }

    public String getVideoTitle() {
        return mVideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.mVideoTitle = videoTitle;
    }
}
