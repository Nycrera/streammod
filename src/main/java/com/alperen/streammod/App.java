package com.alperen.streammod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;

public class App implements ExitCallback {

	public static void main(String[] args) {
		FFmpegLogCallback.set(); // Sets FFMpeg to direct its logs.
		
		//FFmpegLogCallback.setLevel(avutil.AV_LOG_ERROR);
		try {
			FFmpegFrameGrabber.tryLoad();
		} catch (org.bytedeco.javacv.FFmpegFrameGrabber.Exception e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Run tests with test numbers! :");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			switch (br.readLine()) {
			// Test 1
			case "1": // 1) Stream Screen recording to 127.0.0.1, receive and display. 
				//StreamPlayer player = new StreamPlayer("127.0.0.1", "1234");
				ScreenStreamerAlt streamer = new ScreenStreamerAlt("127.0.0.1","1234",false);
				//player.Start();
				streamer.Start();
				
				System.out.println("Press enter to stop...");
				br.readLine();
				//player.Stop();
				streamer.Stop();
				break;
				
			case "2": // 2) Stream Screen recording using Gstream to 127.0.0.1, receive and display.
				StreamPlayer player2 = new StreamPlayer("127.0.0.1","1234");
				ScreenStreamer streamer3 = new ScreenStreamer("127.0.0.1","1234",true);
				streamer3.Start();
				player2.Start();
				System.out.println("Press enter to stop...");
				br.readLine();
				player2.Stop();
				streamer3.Stop();
				break;
				
			case "3": // 3) Stream Screen recording to 127.0.0.1, receive and save to video.
				StreamRecorder streamrec = new StreamRecorder("127.0.0.1", "1234", "/home/nycrera/", "Desktop-1",
						"recording2", "High", 30 * 1000 * 1000);
				ScreenStreamer streamer2 = new ScreenStreamer("127.0.0.1", "1234",true);
				streamrec.Start();
				streamer2.Start();
				System.out.println("Press enter to stop...");
				br.readLine();
				streamrec.Stop();
				streamer2.Stop();
				break;

			case "4": // 4) Stream a video on disk, receive and display.
				StreamPlayer player3 = new StreamPlayer("127.0.0.1", "1234");
				VideoStreamerAlt vs = new VideoStreamerAlt("/home/nycrera/recording.mp4", "127.0.0.1", "1234");
				player3.Start();
				vs.Start();
				System.out.println("use commands e-> exit, s-> seek, p-> pause, r-> resume");
				commandloop: while (true) {
					switch (br.readLine()) {
					case "s":
						vs.Seek(2 * 1000); // In milliseconds (ms) => 10^-3 (s)
						break;
					case "e":
						vs.Stop();
						player3.Stop();
						break commandloop;
					case "p":
						vs.Pause();
						break;
					case "r":
						vs.Resume();
						break;
					default:
						System.out.println("use commands e-> exit, s-> seek, p-> pause, r-> resume");
					}
				}
				break;

			default:
				System.out.println("Test case not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onExit() {
		// VLC Process exit.
	}
	
}
