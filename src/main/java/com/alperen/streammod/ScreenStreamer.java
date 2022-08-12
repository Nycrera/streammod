package com.alperen.streammod;

import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;
import org.freedesktop.gstreamer.glib.GLib;

/**
 * <p>
 * Uses gstreamer to stream desktop recording over RTP. The video settings are
 * hardcoded. With codec H264 and a framerate of 30 FPS.
 * </p>
 */
public class ScreenStreamer {
	private Pipeline pipeline;

	/**
	 * Initializes a ScreenStreamer with Gstreamer
	 *
	 * @param ipaddress   String containing the IP address of the receiving client.
	 * @param port        String containing the Port number of the receiving client.
	 * @param enableVAAPI Defines whether the video acceleration is used or not.
	 * @throws IllegalArgumentException
	 * @throws GstException
	 */
	ScreenStreamer(String ipaddress, String port, boolean enableVAAPI) {
		if (!Util.ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}
		//GLib.setEnv("GST_DEBUG", "4", true);
		if (!Gst.isInitialized())
			Gst.init(Version.of(1, 20));
		if (!enableVAAPI) {
			pipeline = (Pipeline) Gst
					.parseLaunch("ximagesrc ! video/x-raw,framerate=30/1 ! timeoverlay ! videoconvert ! x264enc "
							+ " mpegtsmux name=m ! rtpmp2tpay ! udpsink host="
							+ ipaddress + " port=" + port
							+ " sync=false alsasrc device=hw:0 ! audioconvert ! fdkaacenc ! m.");
		} else {
			pipeline = (Pipeline) Gst.parseLaunch(
					"ximagesrc ! video/x-raw,framerate=30/1 ! timeoverlay ! videoconvert ! vaapih264enc ! queue ! h264parse config-interval=-1 ! "
							+ "mpegtsmux name=m ! rtpmp2tpay ! udpsink host=" + ipaddress + " port=" + port
							+ " sync=false alsasrc device=hw:0 ! audioconvert ! fdkaacenc ! m.");
		}
	}

	/**
	 * Start the streaming.
	 */
	public void Start() {
		pipeline.play();
	}

	/**
	 * Stop the streaming.
	 */
	public void Stop() {
		pipeline.stop();
	}
}