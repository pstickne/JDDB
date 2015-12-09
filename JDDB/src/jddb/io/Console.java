package jddb.io;

import java.util.Properties;
import java.util.Scanner;


public class Console 
{
	private Scanner in = null;
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
		in = new Scanner(System.in);
		
		server = props.getProperty("server");
		basePath = props.getProperty("basePath");
		file = props.getProperty("file");
		
		if( server == null ) {
			System.out.println("Please input the server address:");
			server = in.nextLine();
		}
		System.out.println("Using Server Address: " + server);
		
		if( basePath == null ) {
			System.out.println("Input the path to your database file:");
			basePath = in.nextLine();
		}
		System.out.println("Using Base Path: " + basePath);
		
		if( file == null )
			System.out.println("Database not specified. Please use \"USE <database.json>\"");
		else
			System.out.println("Using Database: " + file);
		
		
	}
	
	private void onCommandInput(String str)
	{
		
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