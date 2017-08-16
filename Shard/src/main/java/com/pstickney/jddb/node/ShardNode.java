package com.pstickney.jddb.node;

import com.pstickney.jddb.io.ProcessConsoleInput;
import com.pstickney.jddb.io.ProcessSocketInput;
import com.pstickney.jddb.nosql.Collection;
import com.pstickney.jddb.nosql.Document;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class ShardNode extends Node
{
	private Collection COLLECTION = null;
	
	/**
	 * Creates a database shard that will provide horizontal partitioning 
	 * to your database.<br><br>
	 * 
	 * Each shard will have its own separate space on disk as well as its
	 * own individual JVM memory space. Shards should be started up in 
	 * separate consoles to provide this functionality and should also use
	 * separate config.properties files. 
	 * 
	 * @param prop The configuration settings for this shard
	 */
	public ShardNode(Properties prop)
	{
		super(prop);
		
		
		// Extract the server address and port from the property file
		SERVER = properties.getProperty("server").trim();
		PORT = Integer.parseInt(properties.getProperty("port").trim());
		
		
		// Extract the basePath and collection file from the property file
		BASEPATH = properties.getProperty("basePath").trim();
		COLLECTIONFILE = properties.getProperty("file").trim();
		
		
		/*
		 * Here we want to check for the loopback interface.
		 * In order to connect to a socket on the loopback interface which 
		 * is the current machine, we need to pass null as the address argument.
		 * 
		 * Therefore, localhost and 127.0.0.1 must be changed to null if passed.
		 */
		if( SERVER.equalsIgnoreCase("localhost") || SERVER.equalsIgnoreCase("127.0.0.1") )
			SERVER = null;
		
		
		// Create a new shard specific collection
		COLLECTION = new Collection(BASEPATH, COLLECTIONFILE);
		try {
			COLLECTION.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calling this method on a {@link #ShardNode(Properties)} will attempt to 
	 * establish a connection to the server address and port specified in the 
	 * config file provided to this application as a command line parameter.<br><br>
	 * 
	 * A local console input listener will be created to listen for
	 * incoming requests made locally by the user.
	 * 
	 * Likewise, once a connection has been established, a socket input stream listener
	 * will be created to listen for incoming requests from the server.<br><br>
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
		
		
		/*
		 * Starup the ProcessConsoleInput listener.
		 * 
		 * This listens on the current console for user input. 
		 * The user inputs requests in the form of a String command line argument
		 * which will be processed by the console.
		 */
		ProcessConsoleInput pci = new ProcessConsoleInput(System.console()) {
			@Override
			public void onConsoleInput(String cmd) {
				exec(System.console(), cmd);
			}
		};
		pci.start();
		
		
		while( true )
		{
			try {
				/*
				 * Create socket connect to SERVER:PORT
				 * If this line throws an error because we can't connect,
				 * we will jump down to the ConnectException catch handler
				 * and do nothing but wait and try to connect again until
				 * a connection can be established.
				 */
				csock = new Socket(SERVER, PORT);
				
				/*
				 * Startup the ProcessInputStream listener.
				 * 
				 * This listens on the socket connected to the server for any incoming requests. 
				 * These requests are in the form of a String command line argument to be 
				 * processed by the console.
				 */
				ProcessSocketInput pis = new ProcessSocketInput(csock) {
					@Override
					public void onStreamInput(String input, boolean isError) {
						if( isError ) 
							System.out.println("Warning: " + input);
						else
							exec(csock, input);
					}
				};
				pis.start();
				
				// We have a connection to the server and stared the socket input listener,
				// now we can break out of the loop and continue execution.
				break;
				
			} catch (ConnectException e) {
				// The server isn't started yet so just keep looping
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Sleep for 5 seconds so that we have time to start the server if required
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Calling this method on a Node will attempt to close any open
	 * sockets that have been connected to the server. 
	 */
	@Override
	public void stop() 
	{
		super.stop();
		
		// Close the socket if it is not null
		try {
			if( csock != null )
				csock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes the command on the shard that was supplied by console input.<br>
	 * This is just a function wrapper that specifies the output stream to 
	 * send the results to once the execution has finished.
	 * 
	 * @param console The console the user is using
	 * @param cmd The command to execute
	 */
	public void exec(Console console, String cmd)
	{
		exec(System.out, cmd);
	}
	
	/**
	 * Executes the command on the shard that was supplied by socket input
	 * This is just a function wrapper that specifies the output stream to 
	 * send the results to once the execution has finished.
	 * 
	 * @param socket The socket connected to the server
	 * @param cmd The command to execute
	 */
	public void exec(Socket socket, String cmd)
	{
		try {
			exec(socket.getOutputStream(), cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes the command on the shard.<br><br>
	 * 
	 * Once the execution has finished, the results will then be pushed
	 * to the output stream that was previously specified by the wrapper
	 * functions.
	 * 
	 * @param stream The output stream to send the results to
	 * @param cmd The command to execute
	 */
	public void exec(OutputStream stream, String cmd)
	{
		String parts[];
		PrintWriter out = new PrintWriter(stream);
		
		// Happens when the user hits ^C on the keyboard
		if( cmd == null )
			return;
		
		// Stop the shard
		if( cmd.toLowerCase().equals("exit") )
			System.exit(0);
		
		
		// This lets the server know what kind of application 
		// is connecting to the server.
		else if( cmd.toLowerCase().equals("identify") )
			out.println("SHARD");
		
		
		// Change the JSON database file you are using
		else if( cmd.toLowerCase().startsWith("use") )
		{
			parts = cmd.split(Pattern.quote(" "));
			if( parts.length == 2 )
			{
				String c = parts[1];
				
				if( !COLLECTION.getCurrentCollectionFile().equals(c) )
				{
					try {
						COLLECTION.connectTo(COLLECTION.getCurrentBasePath(), c);
					} catch (FileNotFoundException e) {
						out.println(e.toString());
						out.println("\nSee: SHOW COLLECTIONS\n");
					}
				}
			}
			else
				out.println("\nUsage: USE [collection_name]\n");
		}
		
		
		// Show or list some general info about the server status and collections
		else if( cmd.toLowerCase().startsWith("show") || cmd.toLowerCase().startsWith("list") )
		{
			parts = cmd.split(Pattern.quote(" "));
			if( parts.length == 2 )
			{
				String c = parts[1];
				
				if( c.equalsIgnoreCase("collections") )
				{
					// Filter files by extension of ".json"
					File file = new File(COLLECTION.getCurrentBasePath());
					File files[] = file.listFiles(new FilenameFilter() {
						@Override public boolean accept(File dir, String name) {
							return name.endsWith(".json");
						}
					});
					
					for( File f : files )
						out.println(f.getName());
				}
				else if( c.equalsIgnoreCase("status") )
				{
					out.println("Using Server Address: " + (SERVER == null ? "127.0.0.1" : SERVER));
					out.println("Using Base Path: " + COLLECTION.getCurrentBasePath());
					out.println("Using Database: " + COLLECTION.getCurrentCollectionFile());
				}
				else
					out.println("\nUsage: " + parts[0] + " [collections | status]\n");
			}
			else
				out.println("\nUsage: " + parts[0] + " [collections | status]\n");
		}
		
		
		// Issue commands on the database collection file
		else if( cmd.toLowerCase().startsWith("db") )
		{
			parts = splitOnCharacter(cmd, '.');
			
			
			
			// output the collection itself
			if( cmd.equalsIgnoreCase("db.collection") )
				out.println(COLLECTION);
			
			
			
			// Find a specific set of records matching the query document
			else if( cmd.toLowerCase().contains("db.collection.find") )
			{
				String	funcCall = parts[2],
						insideParens = funcCall.substring(funcCall.indexOf("(")+1, funcCall.indexOf(")"));
				String[] args = splitOnCharacter(insideParens, ',');
				
				if( args.length == 1 )
					out.println(COLLECTION.find(new Document(args[0])));
				else if( args.length == 2 )
					out.println(COLLECTION.find(new Document(args[0]), new Document(args[1])));
				else
					out.println("Illegal number of arguments to find()");
			}
			
			
			
			// Insert a Document into the collection 
			else if( cmd.toLowerCase().contains("db.collection.insert") )
			{
				String	funcCall = parts[2],
						insideParens = funcCall.substring(funcCall.indexOf("(")+1, funcCall.indexOf(")"));
				String[] args = splitOnCharacter(insideParens, ',');
				
				if( args.length == 1 )
					out.println(COLLECTION.insert(new Document(args[0])));
				else
					out.println("Illegal number of arguments to insert()");
			}
			
			
			
			// Update a Document that is already in the collection
			else if( cmd.toLowerCase().contains("db.collection.update") )
			{
				String	funcCall = parts[2],
						insideParens = funcCall.substring(funcCall.indexOf("(")+1, funcCall.indexOf(")"));
				String[] args = splitOnCharacter(insideParens, ',');
				
				if( args.length == 2 )
					out.println(COLLECTION.update(new Document(args[0]), new Document(args[1])));
				else
					out.println("Illegal number of arguments to find()");
			}
			
			
			
			// Remove a Document from the collection
			else if( cmd.toLowerCase().contains("db.collection.remove") )
			{
				String	funcCall = parts[2],
						insideParens = funcCall.substring(funcCall.indexOf("(")+1, funcCall.indexOf(")"));
				String[] args = splitOnCharacter(insideParens, ',');
				
				if( args.length == 1 )
					out.println(COLLECTION.remove(new Document(args[0])));
				else if( args.length == 2 )
					out.println(COLLECTION.remove(new Document(args[0]), Boolean.parseBoolean(args[1])));
				else
					out.println("Illegal number of arguments to remove()");
			}
			
			
			
			// Save the collection to disk
			else if( cmd.toLowerCase().contains("db.collection.save") )
			{
				try {
					COLLECTION.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			
			
			
			// If nothing matches the call the user made, mark it as an error
			else 
			{
				out.println("Unknown call to " + cmd);
			}
		}
		else
		{
			out.println("\nUsage: jddb-shard.jar config.properties\n");
			out.println("Commands:");
			out.printf("\t%-40s\t%s\n", "HELP", "Shows the help menu.");
			out.printf("\t%-40s\t%s\n", "USE  [collection]", "Change your current document collection to the specified one.");
			out.printf("\t%-40s\t%s\n", "SHOW [collections | status]", "Display information about related topic.");
			out.printf("\t%-40s\t%s\n", "LIST [collections | status]", "Alias of SHOW.");
			out.println("");
		}
		out.flush();
	}
	
	
	/**
	 * Split a string on a specific character. This will do a special split 
	 * in which it will not split on the character if it is inside quotes {@code (")}
	 * or curly braces {@code ({})}.
	 * 
	 * @param str The string to split
	 * @param c The character to split on
	 * @return A string array of {@code str} tokenized by {@code c}.
	 */
	public String[] splitOnCharacter(String str, Character c)
	{
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		boolean inBrace = false, inQuote = false;
		
		// Loop through every character in the array
		for( int i = 0; i < str.length(); i++ )
		{
			/*
			 * if the character is found and we are not in a
			 * curly brace or quote, add to the result array
			 * and clear the string buffer
			 */
			if( str.charAt(i) == c && !inBrace && !inQuote ) {
				result.add(sb.toString());
				sb.setLength(0);
				continue;
			}
			
			if( str.charAt(i) == '{' )					inBrace = true;
			else if( str.charAt(i) == '}' )				inBrace = false;
			else if( str.charAt(i) == '"' && !inQuote )	inQuote = true;
			else if( str.charAt(i) == '"' && inQuote )	inQuote = false;
			
			// Add every non-token character to the string buffer
			sb.append(str.charAt(i));
		}
		
		// Add everything in the string buffer after the last found token
		result.add(sb.toString());
		
		return result.toArray(new String[result.size()]);
	}
	
	public static void main(String ...args)
	{
		if( args.length < 1 ) {
			System.out.println("USAGE: ./jddb-shard config.properties\n");
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
}
