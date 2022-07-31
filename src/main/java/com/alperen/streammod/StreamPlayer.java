package com.alperen.streammod;

import java.io.File;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

/**
 * <p>
 * Uses FFMpeg to play a video stream over the RTP Protocol. It is important to
 * set Width and Height properties matching with the incoming stream. Defaults
 * to 1920x1080.
 * </p>
 */
public class StreamPlayer {
	private FFmpegFrameGrabber Grabber;
	private CanvasFrame Canvas;
	public int Width = 1920;
	public int Height = 1080;
	private boolean Running = false;

	/*
	 * Initializes a StreamPlayer.
	 *
	 * @param ipaddress String containing the IP address of the receiving client.
	 * 
	 * @param port String containing the Port number of the receiving client.
	 * 
	 * @throws IllegalArgumentException
	 * 
	 * @throws Exception
	 */
	StreamPlayer(String ipaddress, String port) throws IllegalArgumentException, Exception {
		if (!Util.ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}

		File SDPFile = Util.CreateSDPFile("127.0.0.1", "1234");
		Grabber = new FFmpegFrameGrabber(SDPFile.getAbsoluteFile());

		Grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		Grabber.setFrameRate(30);

	}

	/**
	 * Start the player. It's usually better if player is ready before the stream
	 * begins. But not mandatory.
	 * 
	 * @throws Exception
	 */
	public void Start() throws Exception {
		Grabber.setImageWidth(Width);
		Grabber.setImageHeight(Height);

		Running = true;
		Grabber.start();
		Canvas = new CanvasFrame("Stream", CanvasFrame.getDefaultGamma() / Grabber.getGamma());

		RunFFMpegThread();
	}

	/**
	 * Stops the player
	 * 
	 * @throws Exception
	 */
	public void Stop() throws Exception {
		Running = false;
		Grabber.stop();
		Grabber.close();
		Canvas.dispose();
	}

	private void RunFFMpegThread() {
		Runnable runnable = () -> { // FFMpeg Thread
			try {
				Frame frame = null;
				while (Running) {
					frame = Grabber.grab();
					if (frame != null) {
						Canvas.showImage(frame);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}