package com.alperen.streammod;

import java.util.regex.Pattern;

import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

public class ScreenStreamer {
	Pipeline pipeline;

	/**
	 * Initializes a ScreenStreamer, this class uses gstreamer to stream desktop
	 * recording over RTP. The video settings are hardcoded. With codec H264 and a
	 * framerate of 30 FPS.
	 *
	 * @param ipaddress String containing the IP address of the receiving client.
	 * @param port      String containing the Port number of the receiving client.
	 * @throws IllegalArgumentException
	 * @throws GstException
	 */
	ScreenStreamer(String ipaddress, String port) {
		if (!ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}

		if (!Gst.isInitialized())
			Gst.init(Version.BASELINE);

		pipeline = (Pipeline) Gst
				.parseLaunch("ximagesrc ! video/x-raw,framerate=30/1 ! timeoverlay ! videoconvert ! x264enc ! "
						+ "video/x-h264,profile=baseline ! h264parse config-interval=-1 ! rtph264pay pt=96 config-interval=-1 ! udpsink host="
						+ ipaddress + " port=" + port + " sync=false");
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

	private static boolean ValidateData(final String ip, final String port) {
		final Pattern IPPATTERN = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return IPPATTERN.matcher(ip).matches() && port.matches("-?(0|[1-9]\\d*)");
	}

}