package jddb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jddb.io.Console;

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
		
		new Console(prop).init();
	}
	
	
	
	public static void usage()
	{
		System.out.println("USAGE: ./JDDB config.properties\n");
	}
}
