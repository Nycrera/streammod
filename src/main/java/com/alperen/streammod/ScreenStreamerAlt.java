package com.alperen.streammod;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegFrameRecorder.Exception;
import org.bytedeco.javacv.Frame;

public class ScreenStreamerAlt {
	List<StreamClient> clientList = new ArrayList<StreamClient>();
	FFmpegFrameGrabber grabber;
	public int Width = 1920;
	public int Height = 1080;
	public int DisplayId = 0;
	boolean running = false;

	/**
	 * Initializes a ScreenStreamer
	 *
	 * @param ipaddress String containing the IP address of the receiving client.
	 * @param port      String containing the Port number of the receiving client.
	 * @throws Exception
	 * @throws java.lang.Exception
	 * @throws IllegalArgumentException
	 * @throws GstException
	 */
	ScreenStreamerAlt(String ipaddress, String port) throws java.lang.Exception {
		grabber = new FFmpegFrameGrabber(":"+ DisplayId + ".0");
		grabber.setFormat("x11grab");
		grabber.setFrameRate(30);
		grabber.setImageWidth(Width);
		grabber.setImageHeight(Height);
		grabber.start();
		AddNewClient(ipaddress, port);
	}

	/**
	 * Start the streaming.
	 */
	public void Start() {
		running = true;

		Runnable runnable = () -> { // Streaming Thread
			try {
				Frame frame = null;
				while (running) { // Streaming loop
					frame = grabber.grabAtFrameRate(); // WHEN WE SEEK THIS DOES NOT CHANGE THE TIME
					if (frame != null) {
						for (StreamClient client : clientList) {
							client.recorder.record(frame);
						}
					}
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}

	/**
	 * Stop the streaming.
	 */
	public void Stop() {
		try { // I do not expect any exceptions here to be thrown, so I will be catching them
				// here.
			running = false;
			grabber.stop();
			grabber.close();
			for (StreamClient client : clientList) {
				client.recorder.stop();
				client.recorder.close();
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Adds a new client to the list. Using this method you can stream this video to
	 * multiple clients at the same time. This does not check if client is already
	 * in the list so thats on you.
	 * </p>
	 */
	public void AddNewClient(String clientip, String clientport)
			throws org.bytedeco.javacv.FFmpegFrameRecorder.Exception {
		if (!Util.ValidateData(clientip, clientport))
			throw new IllegalArgumentException();
		StreamClient client = new StreamClient(clientip, clientport);

		// Eclipse warns me of a resource leak, but as I close these streams that
		// doesn't really makes sense.
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtp://" + clientip + ":" + clientport,
				grabber.getImageWidth(), grabber.getImageHeight());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat("rtp");
		recorder.setFrameRate(30);
		recorder.setVideoBitrate(8 * 1024 * 1024); // 8MBPS
		recorder.start();

		client.recorder = recorder;

		clientList.add(client);
	}
}
