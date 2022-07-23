package com.alperen.streammod;

import java.io.File;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class StreamPlayer {
	private FFmpegFrameGrabber Grabber;
	CanvasFrame Canvas;
	public int Width = 1920;
	public int Height = 1080;
	private boolean Running = false;

	StreamPlayer(String ipaddress, String port) throws IllegalArgumentException, Exception {
		if (!Util.ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}

		File SDPFile = Util.CreateSDPFile("127.0.0.1", "1234");
		Grabber = new FFmpegFrameGrabber(SDPFile.getAbsoluteFile());

		Grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		Grabber.setFrameRate(30);

	}

	public void Start() throws Exception {
		Grabber.setImageWidth(Width);
		Grabber.setImageHeight(Height);
		
		Running = true;
		Grabber.start();
		Canvas = new CanvasFrame("Stream", CanvasFrame.getDefaultGamma() / Grabber.getGamma());

		RunFFMpegThread();
	}
	
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
