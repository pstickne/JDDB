
package com.pstickney.jddb.nosql;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Set;

/**
 * Wrapper class for a NoSQL document.
 * 
 * @author pstickne
 */
public class Document
{
	private Object prejson = null;
	private JSONObject json = null;
	private JSONParser parser = null; 
	
	/**
	 * Construct a blank document
	 */
	public Document()
	{
		json = new JSONObject();
	}
	
	
	/**
	 * Construct a document based on JSON string
	 * 
	 * @param json The JSON string
	 */
	public Document(String json)
	{
		parseJSON(json);
	}
	
	
	/**
	 * Construct a document based on an object
	 * 
	 * @param obj Object that can be casted as a JSON object
	 */
	public Document(Object obj)
	{
		json = (JSONObject) obj;
	}
	
	
	/**
	 * Construct a document clone
	 * 
	 * @param obj The document to clone
	 */
	public Document(JSONObject obj)
	{
		json = obj;
	}
	
	
	/**
	 * Parses a JSON string and sets this json object to the result
	 * 
	 * @param json The JSON string
	 */
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

	
	/**
	 * Wrapper for {@link HashMap#containsKey(Object)}
	 * 
	 * Returns true if key exists, false otherwise
	 * 
	 * @param key The key to be tested
	 * @return true if the mapping contains the key
	 */
	public boolean containsKey(String key)
	{
		return json.containsKey(key);
	}
	
	
	/**
	 * Wrapper for {@link HashMap#containsValue(Object)}
	 * 
	 * Returns true if this mapping has one or more keys mapped
	 * to the specific value.
	 * 
	 * @param value The value to be tested
	 * @return true if the mapping has one or more keys associated to the value
	 */
	public boolean containsValue(Object value)
	{
		return json.containsValue(value);
	}
	
	
	/**
	 * Get the current JSON object for this document
	 * 
	 * @return The JSON object
	 */
	public JSONObject getJSONObject()
	{
		return json;
	}
	
	
	/**
	 * Sets this JSON object to obj
	 * 
	 * @param obj The JSONObject to clone
	 * @return true
	 */
	public boolean copy(JSONObject obj)
	{
		json = obj;
		return true;
	}
	
	
	/**
	 * Sets this JSON object to doc
	 * 
	 * @param doc The Document to clone
	 * @return true
	 */
	public boolean copy(Document doc)
	{
		json = doc.getJSONObject();
		return true;
	}
	
	
	/**
	 * Gets the current documents keys contained in the mapping
	 * 
	 * @return The document's keys
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getKeys()
	{
		return json.keySet();
	}
	
	
	/**
	 * Gets the value of a specific key in the document
	 * 
	 * @param key The key of the mapping
	 * @return The value of the mapping at the key
	 */
	public Object get(Object key)
	{
		return json.get(key);
	}
	
	
	/**
	 * Sets the value of the key in the map
	 * 
	 * @param key The key
	 * @param value The value
	 */
	@SuppressWarnings("unchecked")
	public void put(String key, Object value)
	{
		json.put(key, value);
	}
	
	
	/**
	 * Removes the entry in the mapping of the key
	 * 
	 * @param key The key
	 * @return The previously associated value at the key
	 */
	public Object remove(Object key)
	{
		return json.remove(key);
	}
	
	
	/**
	 * Gets a serialized string value of the JSON object
	 */
	@Override
	public String toString() 
	{
		return json.toJSONString();
	}
}
