package com.alperen.streammod;

import org.bytedeco.javacv.FFmpegFrameRecorder;

public class StreamClient {
	public String ip, port;
	public FFmpegFrameRecorder recorder;
	
	StreamClient(String ip, String port) {
		this.ip = ip;
		this.port = port;
	}
}