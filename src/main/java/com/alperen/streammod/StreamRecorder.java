package com.alperen.streammod;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class StreamRecorder {

	public int Width = 1920;
	public int Height = 1080;
	private boolean Running = false;
	private File SDPFile;
	private FFmpegFrameGrabber Grabber;
	private FFmpegFrameRecorder Recorder;
	private String VideoFolder;
	private String VideoName;
	private String SourceName;
	private String Priority;
	private long Period;
	private int VideoPartCounter = 1;

	StreamRecorder(String ipaddress, String port, String folder, String sourcename, String videoname, String priority,
			long periodUs) throws IllegalArgumentException, Exception {
		if (!Util.ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}

		this.VideoFolder = folder;
		this.VideoName = videoname;
		this.SourceName = sourcename;
		this.Priority = priority;
		this.Period = periodUs;

		SDPFile = Util.CreateSDPFile(ipaddress, port);

	}

	public void Start() throws Exception {
		Grabber = new FFmpegFrameGrabber(SDPFile.getAbsoluteFile());
		Grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		Grabber.setFrameRate(30);
		Grabber.setImageWidth(Width);
		Grabber.setImageHeight(Height);
		Running = true;
		Grabber.start();

		Recorder = CreateRecorder(GenerateName());

		RunFFMpegThread();
	}

	private void RunFFMpegThread() {
		Runnable runnable = () -> { // FFMpeg Thread
			try {
				Frame frame = null;
				while (Running) {
					frame = Grabber.grab();
					if (frame != null) {
						if (Grabber.getTimestamp() > (VideoPartCounter * Period)) { // All time here are in
																					// microseconds.
							VideoPartCounter++;
							Recorder.stop();
							Recorder.close();
							
							Recorder = CreateRecorder(GenerateName());
						}
						Recorder.record(frame);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread t = new Thread(runnable);
		t.start();
	}
	
	// Calling this function is crucial so that mp4 file can be finished without any corruption.
	public void Stop() throws Exception {
		Running = false;
		Grabber.stop();
		Grabber.close();
		Recorder.stop();
		Recorder.close();
	}

	private String GenerateName() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		return Paths.get(VideoFolder, String.format("%s_%s_%s.mp4", VideoName, SourceName, dtf.format(now))).toString();
	}

	private FFmpegFrameRecorder CreateRecorder(String filename) throws Exception {
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filename, Grabber.getImageWidth(),
				Grabber.getImageHeight());
		recorder.setFormat("mp4");
		recorder.setFrameRate(30);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

		recorder.setMetadata("comment", "priority=" + Priority);
		recorder.start();

		return recorder;
	}
}
