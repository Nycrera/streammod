package com.alperen.streammod;

import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Version;

public class GSTTest {
	private static Pipeline pipeline1;
	private static Pipeline pipeline2;
	public static void StreamDesktop(String[] args) {
		Gst.init(Version.BASELINE, "steammod", args);

        /**
         * Use Gst.parseLaunch() to create a pipeline from a GStreamer string
         * definition. This method returns Pipeline when more than one element
         * is specified.
         */
        pipeline1 = (Pipeline) Gst.parseLaunch("udpsrc port=1234 ! application/x-rtp,encoding-name=H264,payload=96 ! rtph264depay ! avdec_h264 ! xvimagesink");
        pipeline2 = (Pipeline) Gst.parseLaunch("ximagesrc ! video/x-raw,framerate=30/1 ! timeoverlay ! videoconvert ! x264enc ! video/x-h264,profile=baseline ! h264parse config-interval=-1 ! rtph264pay pt=96 config-interval=-1 ! udpsink host=127.0.0.1 port=1234 sync=false");
        /**
         * Start the pipeline.
         */
        pipeline2.play(); // Start the stream
        //pipeline1.play(); // Read on the stream and display
        /**
         * GStreamer native threads will not be taken into account by the JVM
         * when deciding whether to shutdown, so we have to keep the main thread
         * alive. Gst.main() will keep the calling thread alive until Gst.quit()
         * is called. Here we use the built-in executor to schedule a quit after
         * 10 seconds.
         */
       // Gst.getExecutor().schedule(Gst::quit, 10, TimeUnit.SECONDS);
        //Gst.main();
	}
}
