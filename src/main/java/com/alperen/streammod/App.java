package com.alperen.streammod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.bytedeco.javacv.FFmpegLogCallback;

public class App 
{

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        try {
        	//GSTTest.StreamDesktop(args); //Stream desktop to RTP Code
        	//FFmpegLogCallback.set();
        	
        	//FFMpegTest.SaveStreamToFile("/home/nycrera/recording.mp4", "High");
            /*Runnable runnable = () -> { 
    			try {
					FFMpegTest.DisplayStream();
				} catch (Exception e) {
					e.printStackTrace();
				} // Show RTP stream code
            };
            Thread t = new Thread(runnable);
            t.start();
            */
        	//FFMpegTest.StreamVideo("/home/nycrera/recording.mp4","127.0.0.1","1234");
        	VideoStreamer vs = new VideoStreamer("/home/nycrera/recording.mp4","127.0.0.1","1234");
        	vs.Play();
        	
        	Runnable runnable = () -> { 
           	 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
           	 System.out.println("use commands e-> exit, s-> seek, p-> pause, r-> resume");
           	 while(true) {
           	 try {
           		 switch(br.readLine()) {
           		 case "s":
               		 vs.Seek(2*1000); // In milliseconds (ms) => 10^-3 (s)
           			 break;
           		 case "e": 
               		 vs.Stop();
           			 break;
           		 case "p":
           			 vs.Pause();
           			 break;
           		 case "r":
           			 vs.Resume();
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
        	
        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
