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

	public File collectionFile = null;
	private JSONObject collectionObject = null;
	private JSONObject options = null;
	private JSONArray documents = null;
	
	public Collection(String path, String name)
	{
		try 
		{
			this.collectionFile = new File(path, name + ".json");
			load();
		} 
		catch (IOException | ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load() throws FileNotFoundException, IOException, ParseException 
	{
		this.parser = new JSONParser();

		if( collectionFile.exists() )
		{
			this.collectionObject = (JSONObject) parser.parse(new FileReader(collectionFile));
			
			if( collectionObject.containsKey("options") )	
				this.options = (JSONObject) collectionObject.get("options");
			else
				this.options = new JSONObject();
			
			if( collectionObject.containsKey("documents") )
				this.documents = (JSONArray) collectionObject.get("documents");
			else
				this.documents = new JSONArray();
		}
		else
		{
			this.options = new JSONObject();
			this.documents = new JSONArray();
			this.collectionObject = new JSONObject();
			
			collectionObject.put("options", options);
			collectionObject.put("documents", documents);
		}
	}
	
	public boolean save() throws IOException
	{
		if( !collectionFile.exists() )
			collectionFile.createNewFile();
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(collectionFile);
			out.write(collectionObject.toJSONString().getBytes());
			out.flush();
			out.getFD().sync();
			out.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public int count(Document query)
	{
		return find(query).count();
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
		return new Cursor(this, query, projection);
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
