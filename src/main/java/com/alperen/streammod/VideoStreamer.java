package com.alperen.streammod;

import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

/**
 * <p>
 * Uses Gstreamer to stream a local video file over the RTP Protocol.
 * </p>
 */
public class VideoStreamer {
	public boolean enableVAAPI = false;
	public boolean reEncode = true;

	private Pipeline pipeline;

	VideoStreamer(String filename, String clientip, String clientport) throws java.lang.Exception {
		if (!Util.ValidateData(clientip, clientport))
			throw new IllegalArgumentException();
		//GLib.setEnv("GST_DEBUG", "4", true);
		if (!Gst.isInitialized())
			Gst.init(Version.of(1, 20));
		if (!enableVAAPI) {
			pipeline = (Pipeline) Gst.parseLaunch("filesrc name=fsrc ! decodebin ! x264enc ! "
					+ "video/x-h264,profile=baseline ! h264parse config-interval=-1 ! mpegtsmux name=m ! rtpmp2tpay ! udpsink host="
					+ clientip + " port=" + clientport);
		} else {
			pipeline = (Pipeline) Gst.parseLaunch(
					"filesrc name=fsrc ! decodebin ! vaapih264enc ! queue ! h264parse config-interval=-1 ! "
							+ "mpegtsmux name=m ! rtpmp2tpay ! udpsink host=" + clientip + " port=" + clientport
							+ " sync=false fsrc. ! audioconvert ! fdkaacenc ! m.");
		}
		Element fileSource = pipeline.getElementByName("fsrc");
		fileSource.set("location", filename);
	}
	/**
	 * <p>
	 * Starts the video streaming
	 * </p>
	 */
	public void Start() {
	pipeline.play();
	}
	
	/**
	 * Pauses the video.
	 */
	public void Pause() {
		pipeline.pause();
	}
	/**
	 * Seeks to given time.
	 * 
	 * @param time Time to seek in the video, in milliseconds.
	 * 
	 * @throws IllegalArgumentException
	 * 
	 * @throws Exception
	 */
	public void Seek(long time) throws IllegalArgumentException, Exception {
		pipeline.seek(time, TimeUnit.MILLISECONDS);
	}

	/**
	 * Resumes a paused video.
	 */
	public void Resume() {
		pipeline.play();
	}
	
	public void Stop() {
		pipeline.stop();
	}
}
