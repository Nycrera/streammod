package com.alperen.streammod;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

/**
 * <p>
 * Uses FFMpeg to play a video/audio stream over the RTP Protocol. It is important to
 * set Width and Height properties matching with the incoming stream. Defaults
 * to 1920x1080.
 * </p>
 */
public class StreamPlayer {
	private FFmpegFrameGrabber Grabber;
	private CanvasFrame Canvas;
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


		Grabber = new FFmpegFrameGrabber("rtp://"+ipaddress+":"+port);

		Grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		Grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		Grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
		
		Grabber.setFrameRate(30);

	}

	/**
	 * Start the player. It's usually better if player is ready before the stream
	 * begins. But not mandatory. It might take a while for stream to be actually played, because of a keyframe being awaited.
	 * 
	 * @throws Exception
	 */
	public void Start() throws Exception {
		Running = true;
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
				Grabber.start();
				Canvas = new CanvasFrame("Stream", CanvasFrame.getDefaultGamma() / Grabber.getGamma());
                final SourceDataLine soundLine;
				final AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);

		        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		        soundLine = (SourceDataLine) AudioSystem.getLine(info);
		        soundLine.open(audioFormat);
		        soundLine.start();
		        
				
				Frame frame = null;
				while (Running) {
					frame = Grabber.grab();
					if(frame != null) {
					if (frame.image != null) {
						Canvas.showImage(frame);
					}
					if(frame.samples != null) {
                        final ShortBuffer channelSamplesShortBuffer = (ShortBuffer) frame.samples[0];
                        channelSamplesShortBuffer.rewind();

                        final ByteBuffer outBuffer = ByteBuffer.allocate(channelSamplesShortBuffer.capacity() * 2);

                        for (int i = 0; i < channelSamplesShortBuffer.capacity(); i++) {
                            short val = channelSamplesShortBuffer.get(i);
                            outBuffer.putShort(val);
                        }
                        soundLine.write(outBuffer.array(), 0, outBuffer.capacity());
                        outBuffer.clear();

					}
				}else{
					// possible that the stream has ended.
					
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