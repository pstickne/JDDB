package jddb.io;

import java.io.Console;

/**
 * This class is a wrapper for reading input in from the console on a thread.
 * This way, the currently running application can continue execution while 
 * also taking in commands from the console and the user can specify the 
 * type of action to take based on the input.<br><br>
 * 
 * This class contains an abstract function {@link #onConsoleInput(String)} which 
 * needs to be defined by the user to determine what to do with the input
 * once it arrives.
 * 
 * @author pstickne
 */
public abstract class ProcessConsoleInput extends Thread
{
	private Console in = null;
	
	/**
	 * Creates the threaded console.<br><br>
	 * 
	 * It takes a console parameter as an argument and uses that
	 * to read the user input from.
	 * 
	 * @param console The console to read input from
	 */
	public ProcessConsoleInput(Console console)
	{
		in = console;
	}
	
	/**
	 * This function needs to be manually defined by the user.<br>
	 * When input gets read in from the console, this function will 
	 * get called in response with the text that was entered. 
	 * 
	 * @param cmd The input from the console
	 */
	public abstract void onConsoleInput(String cmd);
	
	
	@Override
	public void run() 
	{
		super.run();
		
		/*
		 * Since this is threaded, we can enter a forever loop and
		 * continually listen for user input from the console.
		 * 
		 * Once the user inputs a command and hits ENTER, we pass that
		 * command to the user defined function onConsoleInput().
		 */
		while( true )
		{
			onConsoleInput(in.readLine("> "));
		}
	}
}
