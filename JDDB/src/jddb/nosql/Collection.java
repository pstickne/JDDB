package jddb.nosql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Collection 
{
	private JSONParser parser = null;

	private File collectionFile = null;
	public static JSONObject collection = null;
	public static JSONObject options = null;
	public static JSONArray documents = null;
	
	public Collection(String path, String name)
	{
		try 
		{
			this.collectionFile = new File(path, name);
			load();
		} 
		catch (IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void load() throws FileNotFoundException, IOException, ParseException 
	{
		parser = new JSONParser();

		if( collectionFile.exists() )
		{
			collection = (JSONObject) parser.parse(new FileReader(collectionFile));
			
			if( collection.containsKey("options") )	
				options = (JSONObject) collection.get("options");
			else
				options = new JSONObject();
			
			if( collection.containsKey("documents") )
				documents = (JSONArray) collection.get("documents");
			else
				documents = new JSONArray();
		}
		else
		{
			collection = new JSONObject();
			options = new JSONObject();
			documents = new JSONArray();
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean save() throws IOException
	{
		if( !collectionFile.exists() ) {
			collectionFile.getParentFile().mkdirs();
			collectionFile.createNewFile();
		}
		
		collection.put("options", options);
		collection.put("documents", documents);
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(collectionFile);
			out.write(collection.toJSONString().getBytes());
			out.flush();
			out.getFD().sync();
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean drop()
	{
		return collectionFile.delete();
	}
	
	@SuppressWarnings("unchecked")
	public boolean insert(Document doc)
	{
		if( !doc.containsKey("_id") )
			doc.put("_id", UUID.randomUUID().toString());
		
		return documents.add(doc);
	}
	
	public boolean insert(List<Document> docs)
	{
		boolean result = true;
		Iterator<Document> it = docs.iterator();
		
		while( it.hasNext() )
			result &= insert(it.next());
		
		return result;
	}
	
	public boolean save(Document doc) throws IOException
	{
		if( !doc.containsKey("_id") )
			return insert(doc);
		
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("upsert", true);
		options.put("multi", false);
		
		return update(new Document("\"_id\":" + doc.get("_id")), doc, options);
	}
	public Cursor find(Document query)
	{
		return find(query, new Document());
	}
	public Cursor find(Document query, Document projection)
	{
		return new Cursor(query, projection);
	}
	public boolean update(Document query, Document update)
	{
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("upsert", false);
		options.put("multi", false);
		
		return update(query, update, options);
	}
	public boolean update(Document query, Document update, Map<String, Boolean> options)
	{
		Document doc = null;
		Cursor cursor = find(query);
		
		while( cursor.hasNext() )
		{
			doc = cursor.next();
			doc.copy(update);
			
			if( !options.get("multi").booleanValue() )
				break;
		}
		
		return true;
	}
	public boolean remove(Document query)
	{
		return remove(query, false);
	}
	public boolean remove(Document query, boolean justOne)
	{
		Cursor cursor = find(query);
		
		while( cursor.hasNext() )
		{
			cursor.remove(cursor.next());
			if( justOne )
				break;
		}
		
		return true;
	}
}
