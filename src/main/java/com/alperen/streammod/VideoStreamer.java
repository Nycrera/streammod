package com.alperen.streammod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class VideoStreamer {

	List<StreamClient> clientList = new ArrayList<StreamClient>();
	FFmpegFrameGrabber grabber;
	boolean running = false;
	boolean paused = false;

	VideoStreamer(String filename, String clientip, String clientport) throws java.lang.Exception {
		grabber = new FFmpegFrameGrabber(filename);
		grabber.start();
		AddNewClient(clientip, clientport);
	}

	// This does not check if client is already in the list so thats on you.
	public void AddNewClient(String clientip, String clientport)
			throws org.bytedeco.javacv.FFmpegFrameRecorder.Exception {
		if (!ValidateData(clientip, clientport))
			throw new IllegalArgumentException();
		StreamClient client = new StreamClient(clientip, clientport);

		// TODO: I should probably construct these URIs better with some checks etc.
		// Just
		// concatenating looks kind of messy.
		// Eclipse warns me of a resource leak, but as I close these streams that
		// doesn't really makes sense.
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtp://" + clientip + ":" + clientport,
				grabber.getImageWidth(), grabber.getImageHeight());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setFormat("rtp");
		recorder.setFrameRate(30);
		recorder.setVideoBitrate(8 * 1024 * 1024); // 8MBPS
		recorder.start(); // WARNING: TEST IF THIS WORKS THEN REMOVE THIS COMMENT. THIS MAY BE THE CAUSE
							// OF SOME FUTURE PROBLEMS.
		client.recorder = recorder;

		clientList.add(client);
	}

	// Starts the video streaming thread, also starting the play of video to all
	// clients.
	public void Play() {
		running = true;

		Runnable runnable = () -> { // Streaming Thread
			try {
				Frame frame = null;

				while (running) { // Streaming loop
					if (paused) { // Send the last frame if paused, this way we keep the connection live.
									// Otherwise we would need to pause on the client too.
						for (StreamClient client : clientList) {
							client.recorder.record(frame);
						}
						continue;
					} // else
					frame = customGrabAtFrameRate(); // WHEN WE SEEK THIS DOES NOT CHANGE THE TIME
					if (frame != null) {
						for (StreamClient client : clientList) {
							client.recorder.record(frame);
						}
					} else {
						// Video has finished, if you want to replay you will need to restart the
						// grabber, recorders etc.
						// or create another VideoStreamer.
						Stop();
					}
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}

	// Time in milliseconds
	public void Seek(long time) throws IllegalArgumentException, java.lang.Exception {
		if (time >= 0 && time * 1000 <= grabber.getLengthInTime()) {
			grabber.setTimestamp(time * 1000); // In microseconds 1 (us) => 10^-3 (ms) => 10^-6 (s)
			startTime = 0;
		} else {
			throw new IllegalArgumentException("Seeking time must be within time limits of the video. Video Length:"
					+ (grabber.getLengthInTime() / 1000));
		}
	}

	public void Pause() {
		paused = true;
	}

	public void Resume() {
		startTime = 0;
		paused = false;
	}

	// Stop stream, grabber, recorder, close all.
	public void Stop() {
		try { // I do not expect any exceptions here to be thrown, so I will be catching them
				// here.
			grabber.stop();
			grabber.close();
			for (StreamClient client : clientList) {
				client.recorder.stop();
				client.recorder.close();
			}
			running = false;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean ValidateData(final String ip, final String port) {
		final Pattern IPPATTERN = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return IPPATTERN.matcher(ip).matches() && port.matches("-?(0|[1-9]\\d*)");
	}

	private long startTime = 0;

	private Frame customGrabAtFrameRate() throws Exception, InterruptedException {
		Frame frame = grabber.grab();
		if (frame != null) {
			customWaitForTimestamp(frame);
		}
		return frame;
	}

	private boolean customWaitForTimestamp(Frame frame) throws InterruptedException {
		if (startTime == 0) {
			startTime = System.nanoTime() / 1000 - frame.timestamp;
		} else {
			long delay = frame.timestamp - (System.nanoTime() / 1000 - startTime);
			if (delay > 0) {
				Thread.sleep(delay / 1000, (int) (delay % 1000) * 1000);
				return true;
			}
		}
		return false;
	}
}
