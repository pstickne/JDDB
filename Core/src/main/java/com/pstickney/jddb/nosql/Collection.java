package com.pstickney.jddb.nosql;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class Collection 
{
	private File collectionFile = null;
	private File tempFile = null;
	
	private JSONParser parser = null;
	private JSONObject Jcollection = null;
	private JSONArray Jdocuments = null;
	
	public Map<String, Object> collection = null;
	public List<Document> documents = null;
	
	public Collection()
	{
		Jcollection = new JSONObject();
		Jdocuments = new JSONArray();
		
		collection = new HashMap<String, Object>();
		documents = new ArrayList<Document>();
	}
	
	public Collection(String path, String name)
	{
		Jcollection = new JSONObject();
		Jdocuments = new JSONArray();
		
		collection = new HashMap<String, Object>();
		documents = new ArrayList<Document>();
		
		try {
			connectTo(path, name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Collection connectTo(String path, String name) throws FileNotFoundException
	{
		tempFile = new File(path, name);
		if( !tempFile.exists() )
			throw new FileNotFoundException("File does not exist");
		
		collectionFile = tempFile;
		return this;
	}
	
	public void load() throws FileNotFoundException, IOException 
	{
		parser = new JSONParser();

		if( collectionFile.exists() )
		{
			try {
				Jcollection = (JSONObject) parser.parse(new FileReader(collectionFile));
			} catch (ParseException | ClassCastException e) {
				Jcollection = new JSONObject();
			}
			
			if( Jcollection.containsKey("documents") )
				Jdocuments = (JSONArray) Jcollection.get("documents");
			else
				Jdocuments = new JSONArray();
		}
		
		for( Object o : Jdocuments )
			documents.add(new Document(o));
	}
	
	@SuppressWarnings("unchecked")
	public void save() throws IOException
	{
		if( !collectionFile.exists() ) {
			collectionFile.getParentFile().mkdirs();
			collectionFile.createNewFile();
		}
		
		Jdocuments.clear();
		Jdocuments.addAll(documents);
		
		Jcollection.put("documents", Jdocuments);
		
		FileOutputStream out = new FileOutputStream(collectionFile);
		out.write(Jcollection.toJSONString().getBytes());
		out.flush();
		out.getFD().sync();
		out.close();
	}
	
	
	
	
	public File getCurrentBasePathFile()
	{
		return collectionFile.getParentFile();
	}
	public String getCurrentBasePath()
	{
		try {
			return collectionFile.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public String getCurrentCollectionFile()
	{
		return collectionFile.getName();
	}
	
	
	
	
	public boolean drop()
	{
		return collectionFile.delete();
	}
	
	
	
	
	/**
	 * Returns a database cursor for the matching Document query you are searching for
	 * 
	 * @param query The document query to search for
	 * @return Database cursor matching the search query
	 */
	public Cursor find(Document query)
	{
		return find(query, new Document());
	}
	/**
	 * Returns a database cursor for the matching Document query you are searching for
	 * 
	 * @param query The document query to search for
	 * @param projection The JSON keys to keep or remove in the resulting cursor
	 * @return Database cursor matching the seaerch query
	 */
	public Cursor find(Document query, Document projection)
	{
		return new Cursor(this, query, projection);
	}
	
	
	
	
	/**
	 * Insert a new Document into the database
	 * 
	 * @param doc The Document to insert
	 * @return true if successful, false otherwise
	 */
	public boolean insert(Document doc)
	{
		// generate a random key for the document if it does not contain one
		if( !doc.containsKey("_id") )
			doc.put("_id", UUID.randomUUID().toString());
		
		return documents.add(doc);
	}
	/**
	 * Inserts a list of Documents into the database
	 * 
	 * @param docs The Documents to insert
	 * @return true if all of the inserts were successful, false otherwise
	 */
	public boolean insert(List<Document> docs)
	{
		boolean result = true;
		Iterator<Document> it = docs.iterator();
		
		while( it.hasNext() )
			result &= insert(it.next());
		
		return result;
	}
	
	
	
	
	/**
	 * Updates a single matching document in the database
	 * 
	 * @param query The Document query to search for
	 * @param update The new Document to replace
	 * @return true if successful, false otherwise
	 */
	public boolean update(Document query, Document update)
	{
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("upsert", false);
		options.put("multi", false);
		
		return update(query, update, options);
	}
	/**
	 * Updates single or multiple document(s) in the database
	 * 
	 * @param query The Document query to search for
	 * @param update The new Document to replace 
	 * @param options Option map containing upsert and multi values
	 * @return true if successful, false otherwise
	 */
	public boolean update(Document query, Document update, Map<String, Boolean> options)
	{
		Document doc = null;
		Cursor cursor = find(query);
		
		while( cursor.hasNext() )
		{
			doc = cursor.next();
			doc.copy(update);
			
			// if multi is not set, then just break out of the loop
			// after we do a single update
			if( !options.get("multi").booleanValue() )
				break;
		}
		
		return true;
	}
	
	
	
	
	/**
	 * Remove a single Document from the database
	 * 
	 * @param query The Document query to search for
	 * @return true if successful, false otherwise
	 */
	public boolean remove(Document query)
	{
		return remove(query, false);
	}
	/**
	 * Removes Document(s) from the database
	 * 
	 * @param query The Document query to search for
	 * @param justOne true to remove a single Document, false to remove all matching Documents 
	 * @return true if all removes were successful, false otherwise
	 */
	public boolean remove(Document query, boolean justOne)
	{
		boolean result = false;
		Cursor cursor = find(query);
		
		while( cursor.hasNext() )
		{
			result &= cursor.remove(cursor.next());
			if( justOne )
				break;
		}
		
		return result;
	}
	
	@Override
	public String toString() 
	{
		return documents.toString();
	}
}
