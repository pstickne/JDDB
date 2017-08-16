package com.pstickney.jddb.node;

import com.pstickney.jddb.io.ProcessConsoleInput;
import com.pstickney.jddb.io.ProcessSocketInput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Properties;

public class ClientNode extends Node
{
	private PrintWriter out = null;
	
	/**
	 * Creates a client in the console that can send database commands
	 * to the server.<br><br>
	 * 
	 * Each client can be run from the same or separate machines but should
	 * use their own separate console. The clients can use the same config
	 * properties file to connect to the server.
	 * 
	 * @param prop The configuration settings for the client
	 */
	public ClientNode(Properties prop)
	{
		super(prop);
		
		// Extract the server address and port from the property file
		SERVER = properties.getProperty("server").trim();
		PORT = Integer.parseInt(properties.getProperty("port").trim());

		/*
		 * Here we want to check for the loopback interface.
		 * In order to connect to a socket on the loopback interface which 
		 * is the current machine, we need to pass null as the address argument.
		 * 
		 * Therefore, localhost and 127.0.0.1 must be changed to null if passed.
		 */
		if( SERVER.equalsIgnoreCase("localhost") || SERVER.equalsIgnoreCase("127.0.0.1") )
			SERVER = null;
	}
	
	/**
	 * Calling this method on a {@link #ClientNode(Properties)} will attempt to establish a connection
	 * to the server address and port specified in the config file provided
	 * to this application as a command line parameter.<br><br>
	 * 
	 * Once a connection has been established, a socket input stream listener
	 * will be created to listen for responses back from the server.
	 * 
	 * Also, a local console input listener will be created to read input 
	 * made onto the console by the client.
	 */
	@Override
	public void start() 
	{
		super.start();
		
		/*
		 * Register a new virtual machine shutdown hook.
		 * The JVM will run this code in response to two kinds of events:
		 * 
		 * 		1) 	The program exits normally, when the last non-daemon thread exists
		 * 			or when the exit method is invoked
		 * 		2)	The JVM is terminated in response to a user interrupt, such as 
		 * 			typing ^C or such as when the user logs off or system shuts down.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override public void run() {
				stop();
			}
		}));
		
		System.out.printf("\nAttempting to connect to server at " + (SERVER == null ? "127.0.0.1" : SERVER) + ":" + PORT + "...");
		
		while( true )
		{
			try {
				/*
				 * Create socket connection to SERVER:PORT
				 * If this line throws an error because we can't connect,
				 * we jump down to the ConnectException catch handler and 
				 * do nothing. We then wait 5 seconds and try to connect 
				 * again until a connection can be established.
				 */
				csock = new Socket(SERVER, PORT);
				
				
				/*
				 * Create the output stream back to the server once
				 * the connection has been established.
				 */
				out = new PrintWriter(csock.getOutputStream(), true);
				
				
				//Show to the user the the connection has been established 
				System.out.println("SUCCESSFUL\n");
				
				
				/*
				 * Starts the ProcessInputStream listener.
				 * 
				 * This listens on the socket connected to the server for responses
				 * that are sent back from the server based on the previous request
				 * sent from the client.
				 */
				ProcessSocketInput psi = new ProcessSocketInput(csock) {
					@Override
					public void onStreamInput(String input, boolean isError) {
						if( input == null )
							return;
						
						if( isError )
							System.out.println("Warning: " + input);
						else if( input.equalsIgnoreCase("identify") ) {
							out.println("CLIENT");
							out.flush();
						}
						else
							System.out.println(input);
					}
				};
				psi.start();
				
				
				/*
				 * Starts the ProcessConsoleInput listener.
				 * 
				 * This listens on the current client's console and sends the input
				 * to the server for processing.
				 */
				ProcessConsoleInput pci = new ProcessConsoleInput(System.console()) {
					@Override
					public void onConsoleInput(String cmd) {
						out.println(cmd);
						out.flush();
					}
				};
				pci.start();
				
				
				/*
				 * We now have a connection to the server and have started the input listener,
				 * so now we can break out of the loop and continue with client input.
				 */
				break;
				
			} catch (ConnectException e) {
				// Do nothing here because the server is not up.
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			// Sleep for 5 seconds so that we can start the server if required
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Calling this method on a Node will attempt to close any open sockets
	 * that have been connection to the server.
	 */
	@Override
	public void stop() 
	{
		super.stop();
		
		// Closes the socket if it's not null
		try {
			if( csock != null )
				csock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String ...args)
	{
		if( args.length < 1 ) {
			System.out.println("USAGE: ./jddb-client config.properties\n");
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

		new ClientNode(prop).start();
	}
}
