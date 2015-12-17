package jddb.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public abstract class ProcessSocketInput extends Thread
{
	private BufferedReader in = null;
	
	public ProcessSocketInput(Socket socket)
	{
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void onStreamInput(String input);
	
	@Override
	public void run() 
	{
		super.run();
		
		try {
			while( true ) 
			{
				onStreamInput(in.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
