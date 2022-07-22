package com.alperen.streammod;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
public class FFMpegTest {
	 public static void DisplayStream() throws Exception {
		 // Maybe we should remove this file after the end of operation.
		 File SDPFile = CreateSDPFile("127.0.0.1","1234");
		 FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(SDPFile.getAbsoluteFile());
		 CanvasFrame canvas = new CanvasFrame("Stream", CanvasFrame.getDefaultGamma()/grabber.getGamma());
		 grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		 grabber.setFrameRate(30);
		 grabber.setImageWidth(1920);
		 grabber.setImageHeight(1080);
		 
         grabber.start();
         Frame frame = null;
         while(true) {
         frame = grabber.grab();
         if(frame != null) {
        	 canvas.showImage(frame);
         }
         }
	 }
	 
	 
	 /**
	 * Saves a RTP Stream to a video file
	 *
	 * @param FileName Full path and the file name of the resulting file.
	 * @param PriorityMetadata a string containing the priority information to be added to metadata
	 */
	 static boolean running = false; 
	 static boolean paused = false;
	 public static void SaveStreamToFile(String FileName, String PriorityMetadata) throws Exception {
		 
		 File SDPFile = CreateSDPFile("127.0.0.1","1234");
		 FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(SDPFile.getAbsoluteFile());
		 grabber.setOption("protocol_whitelist", "rtp,udp,file,crypto");
		 grabber.setFrameRate(30);
		 grabber.setImageWidth(1920);
		 grabber.setImageHeight(1080);
		 FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(FileName,grabber.getImageWidth(),grabber.getImageHeight());

		 recorder.setFormat("mp4");
		 recorder.setFrameRate(30);
		 recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		 
		 recorder.setMetadata("comment", "priority="+PriorityMetadata);
		 
		 running = true;
         grabber.start();
         recorder.start();
         
         Runnable runnable = () -> { 
        	 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        	 System.out.println("Press enter to stop the stream and exit...");
        	 try {
        		 br.readLine();
        		 running = false;
            	 System.out.println("Exiting..");
            	 grabber.stop();
            	 recorder.stop();
            	 recorder.close();
            	 PrintMetadata(FileName);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
         };
         Thread t = new Thread(runnable);
         t.start();
         
         
         Frame frame = null;
         while(running) {
         frame = grabber.grab();
         if(frame != null) {
        	 recorder.record(frame);
         }
         }
	 }
	 
	 public static void StreamVideo(String FileName, String ClientIP, String ClientPort) throws Exception {

		 FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(FileName);
		 grabber.start();
		 System.out.println(grabber.getLengthInTime());
		 // I should probably construct these URIs better with some checks etc. Just concatenating is really messy.
		 FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtp://" + ClientIP + ":" + ClientPort,grabber.getImageWidth(),grabber.getImageHeight());
		 recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		 recorder.setFormat("rtp");
		 recorder.setFrameRate(30);
		 recorder.setVideoBitrate(8 * 1024 *1024); // 8MBPS
		 
		 Runnable runnable = () -> { 
        	 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        	 System.out.println("use commands e-> exit, s-> seek, p-> pause, r-> resume");
        	 while(true) {
        	 try {
        		 switch(br.readLine()) {
        		 case "s":
            		 grabber.setTimestamp(2*1000000); // In microseconds (us) => 10^-6 (s)
        			 break;
        		 case "e": 
            		 running = false;
                	 System.out.println("Exiting..");
                	 grabber.stop();
                	 recorder.stop();
                	 recorder.close();
                	 PrintMetadata(FileName);
        			 break;
        		 case "p":
        			 paused = true;
        			 break;
        		 case "r":
        			 paused = false;
        			 break;
        		 default:
        			 System.out.println("use commands e-> exit, s-> seek, ");
        		 }
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	}
         };
         Thread t = new Thread(runnable);
         t.start();
		 
		 
		 running = true;
		 recorder.start();
		 
		 
         Frame frame = null;
         while(running) {
			if(paused) {
				recorder.record(frame);
				continue;
			}
         frame = grabber.grabAtFrameRate();
         if(frame != null) {
        	 recorder.record(frame);
         }else {
        	 //Video stream has finished.
        	 grabber.stop();
        	 grabber.close();
        	 recorder.stop();
        	 recorder.close();
        	 running = false;
        	 System.out.println("Done streaming..");
         }
         }
	 }
	 
	 public static void PrintMetadata(String FileName) throws Exception {
		 FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(FileName);
		 grabber.start();
		 
		 Map<String,String> metadataMap = grabber.getMetadata();
		 for (Entry<String, String> entry : metadataMap.entrySet()) {
			    System.out.println(entry.getKey() + ":" + entry.getValue().toString());
		 }
		 grabber.stop();
		 grabber.close();
	 }
	 
	 
	 private static File CreateSDPFile(String IPAdress, String Port) throws IOException {
		 File TempSdpFile = File.createTempFile("streammod",".sdp");
		 FileWriter writer = new FileWriter(TempSdpFile);
		 writer.write("c=IN IP4 " + IPAdress + "\n"
		 		+ "m=video " + Port + " RTP/AVP 96 \n"
		 		+ "a=rtpmap:96 H264/90000");
		 writer.close();
		 return TempSdpFile;
	 }
}
