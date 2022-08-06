package com.alperen.streammod;

import java.io.File;

interface ExitCallback {
	void onExit();
}

public class VlcStreamPlayer {
	public boolean running = false;
	ProcessBuilder vlcpb;
	Process VlcProcess;

	VlcStreamPlayer(String ipaddress, String port) throws IllegalArgumentException, Exception {
		if (!Util.ValidateData(ipaddress, port)) {
			throw new IllegalArgumentException();
		}

		File SDPFile = Util.CreateSDPFile("127.0.0.1", "1234");
		vlcpb = new ProcessBuilder("vlc", SDPFile.getAbsoluteFile().toString());
	}

	/**
	 * Start the VLC player. If no stream is caught in 10s, exits.
	 * 
	 * @throws Exception
	 */
	public void Start() throws Exception {
		if (running) {
			throw new Exception("A VLC instance is already working");
		} else {
			VlcProcess = vlcpb.start();
			Runnable runnable = () -> {
				try {
					VlcProcess.waitFor();
					running = false;
				} catch (InterruptedException e) {
					running = false;
					VlcProcess.destroyForcibly();
				} finally {
					callbackNotify.onExit();
				}
			};
			Thread t = new Thread(runnable);
			t.start();
			running = true;
		}
	}

	/**
	 * Stops the VLC player process
	 * 
	 * @throws Exception
	 */
	public void Stop() throws Exception {
		VlcProcess.destroy();
	}

	private ExitCallback callbackNotify;

	public void registerExitCallback(ExitCallback callback) {
		callbackNotify = callback;
	}

}
