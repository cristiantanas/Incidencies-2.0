package org.uab.android.incidencies.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import org.uab.android.incidencies.rss.RSSReader;
import org.uab.android.incidencies.utils.DailyLog;

public class Server {
	private static final int PORT = 8089;
	private ServerSocket serverSocket = null;
	private Timer timer = null;
	private TimerTask updateTask = null;
	private TimerTask rssUpdateTask;

	public Server(int port) {		
		listenSocket(port);
	}
	
	private void listenSocket(int port) {
		try { 
			serverSocket = new ServerSocket(port);
			DailyLog.i("Server started. Listening on " + serverSocket);
			
			updateTask = new TTLUpdateTask();
			timer = new Timer();
			timer.schedule(updateTask, TTLUpdateTask.UPDATE_TASK_DELAY, 
					TTLUpdateTask.UPDATE_TASK_PERIOD);
			DailyLog.i("Incidence TTL update task started with period = " +
					(TTLUpdateTask.UPDATE_TASK_PERIOD / 60000) + " minutes");
			
			rssUpdateTask = new RSSReader();
			new Timer().schedule(rssUpdateTask, RSSReader.UPDATE_TASK_DELAY, 
					RSSReader.UPDATE_TASK_PERIOD);
		} 
		catch (IOException e) {
			System.err.println("Could not listen on port " + port);
			System.exit(-1);
		}
		
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				DailyLog.i("Connection received from " + clientSocket);
				
				new ServerThread(clientSocket);
			} catch (IOException e) {
				DailyLog.e("Connection establishment failed on port " + port);
			} 
		}
	}
		
	protected void finalize() {
		try { 
			timer.cancel();
			serverSocket.close(); 
		}
		catch (IOException e) {
			System.err.println("Could not close socket!");
			System.exit(-1);
		}
	}
	
	public static void main(String args[]) {
		int port = Server.PORT;
		if (args.length == 2) { 
			if (args[0].equalsIgnoreCase("-p")) { port = Integer.parseInt(args[1]); }
		} 
		
		new Server(port);
	}
}
