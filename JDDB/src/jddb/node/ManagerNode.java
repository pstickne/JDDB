package jddb.node;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jddb.io.ProcessInputStream;
import jddb.io.ProcessOutputStream;

public class ManagerNode extends Node
{
	/**
	 * List of client connection threads. 
	 */
	private List<ServerThread> clientThreads = null;
	
	/**
	 * Shard Manager
	 */
	private ServerThread masterThread = null;
	
	/**
	 * Main application method. This is called when the user starts up
	 * the jddb-master.jar. The args input should be the properties file
	 * to tell the program which port to run on.
	 * 
	 * @param args The set of arguments supplied to the program.
	 */
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

		new ManagerNode(prop).start();
	}
	
	public ManagerNode(Properties prop)
	{
		super(prop);
		
		PORT = Integer.parseInt(properties.getProperty("port"));
	}
	
	@Override
	public void start()
	{
		super.start();
		clientThreads = new ArrayList<ServerThread>();
		
		System.out.println("\nManagerNode is starting up...");
		try {
			ssock = new ServerSocket(PORT, 500);
		} catch (BindException e) {
			System.err.println("\nPort is already in use, please choose another.");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override public void run() {
				stop();
			}
		}));

		System.out.println("ManagerNode is listening for shard servers on port " + PORT);
		while( true )
		{
			try {
				new ServerThread(ssock.accept()).start();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
	
	@Override
	public void stop()
	{
		super.stop();
		
		ServerThread t = null;
		System.out.println("ManagerNode is shutting down...");
		
		while( !clientThreads.isEmpty() )
		{
			t = clientThreads.get(0);
			t.close();
			clientThreads.remove(0);
		}
		
		try {
			if( ssock != null )
				ssock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void usage()
	{
		System.out.println("USAGE: ./jddb-manager config.properties\n");
	}

	class ServerThread extends Thread
	{
		private Socket socket = null;
		private BufferedReader in = null;
		private PrintWriter out = null;
		private boolean isMaster = false;
		
		public ServerThread(Socket sock)
		{
			socket = sock;
		}
		
		public PrintWriter getOutputWriter()
		{
			try {
				if( out == null )
					out = new PrintWriter(socket.getOutputStream()); 
				return out;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public BufferedReader getInputReader()
		{
			try {
				if( in == null )
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				return in;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	
		public void close()
		{
			// close all streams
			try {
				if( socket != null )
					System.out.println("Shard disconnecting from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				else
					System.out.println("Shard disconnecting...");
				
				clientThreads.remove(this);
				if( getOutputWriter() != null ) getOutputWriter().close();
				if( getInputReader() != null ) getInputReader().close();
				socket.close();
			} catch (IOException e) {
	//			e.printStackTrace();
			}
		}
		
		@Override
		public void run()
		{
			super.run();
			
			String input = "";
			try {
				
				if( masterThread == null )
				{
					System.out.println("New MasterNode connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
					masterThread = this;
					isMaster = true;
					
					ProcessInputStream instream = new ProcessInputStream(socket.getInputStream()) {
						@Override
						public void onStreamInput() {
							
						}
					};
					instream.start();
					
					ProcessOutputStream outstream = new ProcessOutputStream(socket.getOutputStream()) {
						@Override
						public void onStreamOutput() {
							
						}
					};
					outstream.start();
				}
				
				System.out.println("New shard connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				clientThreads.add(this);
				
				while( true )
				{
					input = in.readLine();
					
					if( input == null )
						break;
					
					for( ServerThread st : clientThreads )
						st.getOutputWriter().println(input);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Something went wrong with the client so close streams
				close();
			}
		}
	}
}
