package jddb.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import jddb.nosql.Collection;

public class Console 
{
	private java.io.Console console = null;
	private Properties props = null;
	
	private String server = "";
	private String basePath = "";
	private String file = "";
	
	public Console()
	{
		props = new Properties();
		props.setProperty("server", null);
		props.setProperty("basePath", null);
		props.setProperty("file", null);
	}
	
	public Console(Properties p)
	{
		props = p;
	}
	
	public void init()
	{
		String parts[];
		String cmd = "", choice = "";
		console = System.console();

		System.out.println("");
		if( console == null ) {
			System.err.println("No console has been detected, sorry.");
			System.err.println("The program will now exit.\n");
			System.exit(1);
		}
		
		server = props.getProperty("server");
		basePath = props.getProperty("basePath");
		file = props.getProperty("file");
		
		if( server == null )
			server = console.readLine("Please input server address: ");
		System.out.println("Using Server Address: " + server);
		
		if( basePath == null )
			basePath = console.readLine("Input the path to your database file: ");
		System.out.println("Using Base Path: " + basePath);
		
		if( file == null )
			System.out.println("Database not specified. Please use \"USE <database.json>\"");
		else
			System.out.println("Using Database: " + file);
		System.out.println("");
		
		Collection.getCollection().connectTo(basePath, file);
		
		while( true )
		{
			cmd = console.readLine("> ").trim();

			if( cmd.toLowerCase().equals("exit") )
				break;
			
			else if( cmd.toLowerCase().startsWith("use") )
			{
				parts = cmd.split(" ");
				if( parts.length == 2 )
				{
					String c = parts[1];
					
					if( !Collection.getCollection().getCurrentCollectionFile().equals(c) )
					{
						choice = console.readLine("Would you like to save any changes made to your current collection? [Y/N]");
						if( choice.equalsIgnoreCase("Y") )
						{
							try {
								Collection.getCollection().save();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							Collection.getCollection().connectTo(Collection.getCollection().getCurrentBasePath(), c);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else
					System.out.println("\nUsage: USE [collection_name]\n");
			}
			else if( cmd.toLowerCase().startsWith("show") || cmd.toLowerCase().startsWith("list") )
			{
				parts = cmd.split(" ");
				if( parts.length == 2 )
				{
					String c = parts[1];
					
					if( c.equalsIgnoreCase("collections") )
					{
						String path = ".";
						try {
							path = Collection.getCollection().getCurrentBasePath();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						File file = new File(path);
						File files[] = file.listFiles(new FilenameFilter() {
							@Override public boolean accept(File dir, String name) {
								return name.endsWith(".json");
							}
						});
						
						for( File f : files )
							System.out.println(f);
						System.out.println("");
					}
					else if( c.equalsIgnoreCase("status") )
					{
						
					}
					else
						System.out.println("\nUsage: " + parts[0] + " [collections | status]\n");
				}
				else
					System.out.println("\nUsage: " + parts[0] + " [collections | status]\n");
			}
			else if( cmd.toLowerCase().startsWith("help") )
			{
				System.out.println("\nUsage: jddb.jar config.properties\n");
				System.out.println("Commands:");
				System.out.printf("\t%-40s\t%s\n", "HELP", "Shows the help menu.");
				System.out.printf("\t%-40s\t%s\n", "USE  [collection]", "Change your current document collection to the specified one.");
				System.out.printf("\t%-40s\t%s\n", "SHOW [collections | status]", "Display information about related topic.");
				System.out.printf("\t%-40s\t%s\n", "LIST [collections | status]", "Alias of SHOW.");
				System.out.println("");
			}
		}
	}
}
/*
Document query = new Document("{\"group\": 1}");
Collection collection = new Collection(basePath, file);

for( int i = 0; i < 4; i++ ) {
	for( int j = 0; j < 20000; j++ )
		collection.insert(new Document("{\"_id\": " + (i*j+j) + ", \"group\": " + ((i*j+j)%2000) + "}"));
	System.gc();
}

//System.out.println(collection.find(query));

try {
	collection.save();
} catch (IOException e) {
	e.printStackTrace();
}
*/