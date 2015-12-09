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
	private File collectionFile = null;
	
	private JSONParser parser = null;
	private JSONObject Jcollection = null;
	private JSONObject Joptions = null;
	private JSONArray Jdocuments = null;
	
	public Map<String, Object> collection = null;
	public Map<String, Object> options = null;
	public List<Document> documents = null;
	
	private static Collection _instance = null;
	public static Collection getCollection()
	{
		if( _instance == null )
			_instance = new Collection();
		return _instance;
	}
	
	public Collection()
	{
		
	}
	
	public Collection(String path, String name)
	{
		connectTo(path, name);
	}
	
	public Collection connectTo(String path, String name)
	{
		collectionFile = new File(path, name);
		return this;
	}
	
	public void load() throws FileNotFoundException, IOException 
	{
		parser = new JSONParser();

		if( collectionFile.exists() )
		{
			try {
				Jcollection = (JSONObject) parser.parse(new FileReader(collectionFile));
			} catch (ParseException e) {
				Jcollection = new JSONObject();
			}
			
			if( Jcollection.containsKey("options") )
				Joptions = (JSONObject) Jcollection.get("options");
			else
				Joptions = new JSONObject();
			
			if( Jcollection.containsKey("documents") )
				Jdocuments = (JSONArray) Jcollection.get("documents");
			else
				Jdocuments = new JSONArray();
		}
		else
		{
			Jcollection = new JSONObject();
			Joptions = new JSONObject();
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
		
		Jcollection.put("options", Joptions);
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
	public String getCurrentBasePath() throws IOException
	{
		return collectionFile.getParentFile().getCanonicalPath().replaceAll("\\", "/");
	}
	public String getCurrentCollectionFile()
	{
		return collectionFile.getName();
	}
	
	
	
	
	public boolean drop()
	{
		return collectionFile.delete();
	}
	
	
	
	
	
	public Cursor find(Document query)
	{
		return find(query, new Document());
	}
	public Cursor find(Document query, Document projection)
	{
		return new Cursor(query, projection);
	}
	
	
	
	
	
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