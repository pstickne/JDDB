package com.pstickney.jddb.node;

import com.pstickney.jddb.io.ProcessSocketInput;

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
	 * Calling this method on a {@link #ServerNode(Properties)} will
	 * start up the server socket and listen for incoming connections.
	 * 
	 * 
	 */
	@Override
	public void start()
	{
		super.start();
		
		clients = new ArrayList<ClientThread>();
		shards = new ArrayList<ShardThread>();
		
		System.out.println("\nServerNode is starting up...");
		
		/*
		 * Creates a server socket and binds to the specified port.
		 * We will maintain a backlog queue of 500 to process the requests.
		 * If the port is already in use, tell the user and exit the program 
		 */
		try {
			ssock = new ServerSocket(PORT, 500);
		} catch (BindException e) {
			System.err.println("\nPort is already in use, please choose another.");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	
	
	/**
	 * This stop is special in the case that is contains multiple 
	 * client and shard threads that all need to be closed once this
	 * application is stopped.
	 */
	@Override
	public void stop()
	{
		super.stop();
		System.out.println("\nServerNode is shutting down...");
		
		/*
		 * Loop over all the clients in the client list
		 * and call the close function on each of those threads
		 */
		System.out.println("Closing all ClientNode connections");
		ClientThread ct = null;
		while( !clients.isEmpty() )
		{
			ct = clients.get(0);
			ct.close();
		}
		
		/*
		 * Loop over all the shards in the shard list
		 * and call the close function on each of those threads
		 */
		System.out.println("Closing all ShardNode connections");
		ShardThread st = null;
		while( !shards.isEmpty() )
		{
			st = shards.get(0);
			st.close();
		}
		
		// Finally, close this application's server socket
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

	
	/**
	 * This thread will handle the server socket accept method. 
	 * It takes the result socket and sends a request to the 
	 * new connection asking it to IDENTIFY itself.<br><br>
	 * 
	 * Once the connection returns a response with either
	 * SHARD or CLIENT, the decider can then spawn a new
	 * thread that matches the new connection's affiliation.
	 * Finally, the decider thread will terminate itself.
	 */
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
			// Close the socket connection if it is not null
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
			
			/*
			 * Creates a process socket input listener.
			 * 
			 * This sole purpose is to listen for the next incoming
			 * message from the socket with the affiliation of the 
			 * new connection. It will then spawn a new affiliated thread
			 * and then terminate itself.
			 */
			ProcessSocketInput psi = new ProcessSocketInput(socket) {
				@Override
				@SuppressWarnings("deprecation")
				public void onStreamInput(String input, boolean isError) {
					if( input == null )
						return;
					
					System.out.println(input);
					
					/*
					 * Here we check if the return value from the 
					 * call to IDENTIFY is equal to "CLIENT" or "SHARD"
					 * and create corresponding threads for both.
					 */
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
			
			/*
			 * Send out the request to the new connection to determine
			 * what exactly it is.
			 */
			System.out.printf("Asking affiliation...");
			out.println("IDENTIFY");
			out.flush();
		}
	}
	
	
	/**
	 * This thread will handle all client connections that connect to the server. 
	 */
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
		
		public void send(String str)
		{
			out.println(str);
			out.flush();
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
			
			/*
			 * Creates a process socket input listener
			 * 
			 * Here we want to listen for any requests coming form the clients.
			 * In general, we want to Map the message out to all the shard nodes.
			 */
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
	
	
	/**
	 * This thread will handle all shard connections that connect to the server.
	 */
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
			
			/*
			 * Creates a process socket input listener.
			 * 
			 * Here we listen for responses coming form the shard. 
			 * In general, this will be results from user commands
			 * that have been previously sent to the shards
			 */
			ProcessSocketInput psi = new ProcessSocketInput(socket) {
				@Override
				public void onStreamInput(String input, boolean isError) {
					if( input == null )
						return;
					
					System.out.println(input);
					
					if( isError )
						out.println("Warning: " + input);
					else {
						for( ClientThread ct : clients )
							ct.send(input);
					}
					out.flush();
				}
			};
			psi.start();
		}
	}
}
