package org.uab.android.eventreporter.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.uab.android.eventreporter.utils.Utils;

public class ServerConnection {
	public static final String SERVER_IP = "158.109.79.13";
//	public static final String SERVER_IP = "192.168.111.218";
	public static final int SERVER_PORT = 8089;
	public static final int CONNECTION_TIMEOUT = 30 * 1000;
	
	private Socket socket = null;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;
	private boolean isConnected = false;
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public void connect() throws IOException {
		socket = new Socket();
		//socket.bind(new InetSocketAddress(45000));
		socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), CONNECTION_TIMEOUT);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		String response = reader.readLine();
		if (Integer.parseInt(response.split("#")[0]) == Utils.CONNECTION_ACCEPTED) {
			isConnected = true;
		}
	}
	
	public void send(String message) throws IOException {
		writer.write(message);
		writer.flush();
	}
	
	public String recv() throws IOException {
		return reader.readLine();
	}
	
	public void close() throws IOException {
		writer.close();
		reader.close();
		socket.close();
	}
}
