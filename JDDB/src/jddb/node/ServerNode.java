package jddb.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jddb.io.ProcessSocketInput;

public class ServerNode extends Node
{
	private List<ClientThread> clients = null;
	private List<ShardThread> shards = null;
	
	/**
	 * Creates a server node that handles the input and output 
	 * transactions between clients and shard nodes.<br><br>
	 * 
	 * The server node is required as a single point-of-contact 
	 * for clients to connect to when trying to establish 
	 * communication to the shards.
	 * 
	 * @param prop The configuration settings for the server
	 */
	public ServerNode(Properties prop)
	{
		super(prop);
		
		PORT = Integer.parseInt(properties.getProperty("port"));
	}
	
	/**
	 * Calling this method on a {@link #ServerNode(Properties)} 
	 */
	@Override
	public void start()
	{
		super.start();
		
		clients = new ArrayList<ClientThread>();
		shards = new ArrayList<ShardThread>();
		
		System.out.println("\nServerNode is starting up...");
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

		System.out.println("ServerNode is listening for shard servers on port " + PORT);
		while( true )
		{
			try {
				new DeciderThread(ssock.accept()).start();
			} catch (IOException e) {
				// Do nothing
			}
		}
	}
	
	@Override
	public void stop()
	{
		super.stop();
		System.out.println("\nServerNode is shutting down...");
		
		System.out.println("Closing all ClientNode connections");
		ClientThread ct = null;
		while( !clients.isEmpty() )
		{
			ct = clients.get(0);
			ct.close();
		}
		System.out.println("Closing all ShardNode connections");
		ShardThread st = null;
		while( !shards.isEmpty() )
		{
			st = shards.get(0);
			st.close();
		}
		
		try {
			if( ssock != null )
				ssock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main application method. This is called when the user starts up
	 * the jddb-server.jar. The args input should be the properties file
	 * to tell the program which port to run on.
	 * 
	 * @param args The set of arguments supplied to the program.
	 */
	public static void main(String ...args)
	{
		if( args.length < 1 ) {
			System.out.println("USAGE: ./jddb-server config.properties\n");
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

		new ServerNode(prop).start();
	}

	class DeciderThread extends Thread
	{
		private Socket socket = null;
		private PrintWriter out = null;
		
		public DeciderThread(Socket sock)
		{
			socket = sock;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void close()
		{
			try {
				if( socket != null )
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() 
		{
			super.run();
			
			System.out.println("\nNew connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
			
			ProcessSocketInput psi = new ProcessSocketInput(socket) {
				@Override
				@SuppressWarnings("deprecation")
				public void onStreamInput(String input, boolean isError) {
					if( input == null )
						return;
					
					System.out.println(input);
					if( input.equalsIgnoreCase("client") ) {
						new ClientThread(socket).start();
						Thread.currentThread().stop();
					} else if( input.equalsIgnoreCase("shard") ) {
						new ShardThread(socket).start();
						Thread.currentThread().stop();
					}
				}
			};
			psi.start();
			
			System.out.printf("Asking affiliation...");
			out.println("IDENTIFY");
			out.flush();
		}
	}
	
	class ClientThread extends Thread 
	{
		private Socket socket = null;
		private PrintWriter out = null;
		
		public ClientThread(Socket sock)
		{
			socket = sock;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void close()
		{
			try {
				if( socket != null )
					socket.close();
				
				clients.remove(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run()
		{
			super.run();
			
			System.out.println("Adding new client thread to client list.");
			clients.add(this);
			
			ProcessSocketInput psi = new ProcessSocketInput(socket) {
				@Override
				public void onStreamInput(String input, boolean isError) {
					if( input == null )
						return;
					
					System.out.println("Client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ": " + input);
					
					if( isError )
						out.println("Warning: " + input);
					else {
						if( input.startsWith("db.") )
						{
							for( ShardThread st : shards )
								st.send(input);
						}
					}
					out.flush();
				}
			};
			psi.start();
		}
	}
	
	class ShardThread extends Thread
	{
		private Socket socket = null;
		private PrintWriter out = null;
		
		public ShardThread(Socket sock)
		{
			socket = sock;
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void send(String cmd)
		{
			out.println(cmd);
			out.flush();
		}
		
		public void close()
		{
			try {
				if( socket != null )
					socket.close();
				
				shards.remove(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() 
		{
			super.run();
			
			System.out.println("Adding new shard thread to shard list.");
			shards.add(this);
			
			ProcessSocketInput psi = new ProcessSocketInput(socket) {
				@Override
				public void onStreamInput(String input, boolean isError) {
					if( input == null )
						return;
					
					System.out.println(input);
					
					if( isError )
						out.println("Warning: " + input);
					else {
						out.println(input);
					}
					out.flush();
				}
			};
			psi.start();
		}
	}
}
