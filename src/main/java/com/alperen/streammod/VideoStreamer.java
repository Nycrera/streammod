package com.alperen.streammod;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;

/**
 * <p>
 * Uses FFMpeg to stream a local video file over the RTP Protocol. Supports
 * multiple clients.
 * </p>
 */
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

	/**
	 * <p>
	 * Starts the video streaming thread, also starting the play of video to all
	 * clients.
	 * </p>
	 */
	public void Start() {
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
					frame = customGrabAtFrameRate();
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
			}
			catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		};

		Thread t = new Thread(runnable);
		t.start();
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
		if (time >= 0 && time * 1000 <= grabber.getLengthInTime()) {
			grabber.setTimestamp(time * 1000); // In microseconds 1 (us) => 10^-3 (ms) => 10^-6 (s)
			startTime = 0;
		} else {
			throw new IllegalArgumentException("Seeking time must be within time limits of the video. Video Length:"
					+ (grabber.getLengthInTime() / 1000));
		}
	}

	/**
	 * Pauses the video. This is done by sending the same frame over and over again,
	 * to keep the stream alive.
	 */
	public void Pause() {
		paused = true;
	}

	/**
	 * Resumes a paused video.
	 */
	public void Resume() {
		startTime = 0;
		paused = false;
	}

	/**
	 * Stops the stream.
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
