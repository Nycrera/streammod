package com.alperen.streammod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bytedeco.javacv.FFmpegFrameGrabber;

public class Util {
	public static File CreateSDPFile(final String ip, final String port) throws IOException {
		File TempSdpFile = File.createTempFile("streammod", ".sdp");
		TempSdpFile.deleteOnExit(); // Deletes the temporary file on standard program exit.
		FileWriter writer = new FileWriter(TempSdpFile);
		writer.write("c=IN IP4 " + ip + "\n" + "m=video " + port + " RTP/AVP 96 \n" + "a=rtpmap:96 H264/90000");
		writer.close();
		return TempSdpFile;
	}

	public static boolean ValidateData(final String ip, final String port) {
		final Pattern IPPATTERN = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return IPPATTERN.matcher(ip).matches() && port.matches("-?(0|[1-9]\\d*)");
	}

	public static void PrintMetadata(String filename) throws Exception {
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filename);
		grabber.start();

		Map<String, String> metadataMap = grabber.getMetadata();
		for (Entry<String, String> entry : metadataMap.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue().toString());
		}
		grabber.stop();
		grabber.close();
	}
}
