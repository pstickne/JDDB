package jddb.node;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;

public class ShardNode extends Node
{
	public static void main(String ...args)
	{
		if( args.length < 1 ) {
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

		new ShardNode(prop).start();
	}

	public ShardNode(Properties prop)
	{
		super(prop);
		
		SERVER = properties.getProperty("server").trim();
		PORT = Integer.parseInt(properties.getProperty("port").trim());
		
		// check for loopback interface
		if( SERVER.equalsIgnoreCase("localhost") || SERVER.equalsIgnoreCase("127.0.0.1") )
			SERVER = null;
	}
	
	@Override
	public void start() 
	{
		super.start();
		
		BufferedReader in = null;
		PrintWriter out = null;
		
		try {
			csock = new Socket(SERVER, PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override public void run() {
				stop();
			}
		}));
		
		try {
			in = new BufferedReader(new InputStreamReader(csock.getInputStream()));
			out = new PrintWriter(csock.getOutputStream(), true);
			
			Console c = System.console();
			while( true )
			{
				String line = c.readLine("> ");
				out.println(line);
				out.flush();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() 
	{
		super.stop();
		
		try {
			if( csock != null )
				csock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void usage()
	{
		System.out.println("USAGE: ./JDDB config.properties\n");
	}
}
