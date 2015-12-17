package jddb.io;

import java.io.Console;

public abstract class ProcessConsoleInput extends Thread
{
	private Console in = null;
	
	public ProcessConsoleInput(Console console)
	{
		in = console;
	}
	
	public abstract void onConsoleInput(String cmd);
	
	@Override
	public void run() 
	{
		super.run();
		
		while( true )
		{
			onConsoleInput(in.readLine("> "));
		}
	}
}
