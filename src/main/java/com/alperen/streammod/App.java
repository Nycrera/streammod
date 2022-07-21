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
        	FFmpegLogCallback.set();
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
        	FFMpegTest.StreamVideo("/home/nycrera/recording.mp4","127.0.0.1","1234");

        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
