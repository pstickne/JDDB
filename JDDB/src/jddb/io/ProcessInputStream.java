package jddb.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class ProcessInputStream extends Thread
{
	private BufferedReader in = null;
	
	public ProcessInputStream(InputStream input)
	{
		in = new BufferedReader(new InputStreamReader(input));
	}
	
	public abstract void onStreamInput();
	
	@Override
	public void run() 
	{
		super.run();
		
	}
}
