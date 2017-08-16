package com.pstickney.jddb.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class is a wrapper for reading input in from a socket on a thread.
 * This way the currently running application can continue execution 
 * while also still listening for input coming in on the socket.<br><br>
 * 
 * This class contains an abstract function {@link #onStreamInput(String, boolean)} 
 * which needs to be defined by the user to determine what to do with the
 * input once it arrives.
 * 
 * @author pstickne
 */
public abstract class ProcessSocketInput extends Thread
{
	private BufferedReader in = null;
	
	/**
	 * Creates the threaded socket input.<br><br>
	 * 
	 * This takes a socket as an argument to read input from.
	 * 
	 * @param socket The socket to read input from
	 */
	public ProcessSocketInput(Socket socket)
	{
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function needs to be manually defined by the user.<br>
	 * When input gets read in from the socket, this function will 
	 * get called in response with the text that was read. 
	 * 
	 * @param input The input from the socket
	 */
	public abstract void onStreamInput(String input, boolean isError);
	
	
	@Override
	public void run() 
	{
		super.run();
		
		/*
		 * Since this is threaded, we can enter a forever loop and continually
		 * listen for incoming input from the socket.
		 * 
		 * Once the input has been read from the socket, we pass it as a string
		 * to the onStreamInput() function.
		 */
		try {
			while( true ) 
			{
				onStreamInput(in.readLine(), false);
			}
		} catch (SocketException e) {
			onStreamInput(e.toString(), true);
		} catch (IOException e) {
			onStreamInput(e.toString(), true);
		}
	}
}
