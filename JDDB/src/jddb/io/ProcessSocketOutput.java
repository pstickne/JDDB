package jddb.io;

import java.io.OutputStream;
import java.io.PrintWriter;

public abstract class ProcessSocketOutput extends Thread
{
	private PrintWriter out = null;
	
	public ProcessSocketOutput(OutputStream stream)
	{
		out = new PrintWriter(stream);
	}
	
	public abstract void onStreamOutput();
	
	@Override
	public void run() 
	{
		super.run();
		
	}
}
