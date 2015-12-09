package jddb;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main 
{
	public static void main(String ...args)
	{
		if( args.length < 1 )
		{
			usage();
			return;
		}
		
		InputStream in = null;
		Properties prop = new Properties();
		
		try {
			in = new FileInputStream(args[0]);
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("");
		Console console = System.console();
		if( console == null ) {
			System.err.println("No console has been detected, sorry.");
			System.err.println("The program will now exit.\n");
			System.exit(1);
		}
		
		String cmd = "";
		while(true)
		{
			cmd = console.readLine("> ");
			
			if( cmd.equals("exit") )
				break;
			
			else if( cmd.contains("USE ") )
			{
				
			}
		}
	}
	
	
	
	public static void usage()
	{
		System.out.println("USAGE: ./JDDB config.properties\n");
	}
}
