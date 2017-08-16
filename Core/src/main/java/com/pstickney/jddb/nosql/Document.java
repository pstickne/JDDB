package com.pstickney.jddb.nosql;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Set;

public class Document
{
	private Object prejson = null;
	private JSONObject json = null;
	private JSONParser parser = null; 
	
	public Document()
	{
		json = new JSONObject();
	}
	
	public Document(String json)
	{
		parseJSON(json);
	}
	
	public Document(Object obj)
	{
		json = (JSONObject) obj;
	}
	
	public Document(JSONObject obj)
	{
		json = obj;
	}
	
	public void parseJSON(String json)
	{
		if( parser == null )
			parser = new JSONParser();
		
		try {
			prejson = parser.parse(json);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		this.json = (JSONObject) prejson; 
	}

	public boolean containsKey(String key)
	{
		return json.containsKey(key);
	}
	public boolean containsValue(Object value)
	{
		return json.containsValue(value);
	}
	
	public JSONObject getJSONObject()
	{
		return json;
	}
	
	public boolean copy(JSONObject obj)
	{
		json = obj;
		return true;
	}
	public boolean copy(Document doc)
	{
		json = doc.getJSONObject();
		return true;
	}
	@SuppressWarnings("unchecked")
	public Set<String> getKeys()
	{
		return json.keySet();
	}
	
	public Object get(Object key)
	{
		return json.get(key);
	}
	@SuppressWarnings("unchecked")
	public void put(String key, Object value)
	{
		json.put(key, value);
	}
	public Object remove(Object key)
	{
		return json.remove(key);
	}
	
	@Override
	public String toString() 
	{
		return json.toJSONString();
	}
}
