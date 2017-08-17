package com.pstickney.jddb.nosql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Cursor implements Iterator<Document>
{
	private Collection collection = null;
	private Document query = null;
	private Document projection = null;
	
	private List<Document> documents = null; 
	
	/**
	 * Constructs a unique set of {@link Document}s in the {@code collection} that
	 * is filtered based on the {@code query} document. <br>
	 * Optionally, a {@code projection} can be specified that limit the fields
	 * in the resulting documents.
	 * 
	 * @param collection The collection in use
	 * @param query The filter document
	 * @param projection The field document specifier
	 */
	public Cursor(Collection collection, Document query, Document projection)
	{
		this.collection = collection;
		this.query = query;
		this.projection = projection;
		
		documents = new ArrayList<Document>();
		
		// Run the query on the collection once we have all the variables set
		runQuery();
	}
	
	
	/**
	 * 
	 */
	private void runQuery()
	{
		boolean inc = true;
		Document documentIn = null, documentOut = null;
		Set<String> queryKeys = query.getKeys(); 
		Set<String> projectionKeys = projection.getKeys();
		Iterator<Document> it = collection.documents.iterator();
		
		/*
		 * Loop over every document in the collection in order to filter out
		 * which ones match and don't match. The ones that match will get 
		 * returned back to the user.
		 * 
		 * Note: looping over all documents in the collection is O(n). 
		 * 	This should be satisfactory since the number of documents 
		 * 	per shard should be small.
		 */
		while( it.hasNext() )
		{
			inc = true;
			documentIn = it.next();
			
			// Check over each key in the query and make sure it 
			// exists in the document in the collection
			for (String key : queryKeys)
				
				// If the key doesn't exist, mark include as false and go to next document
				if( !documentIn.containsKey(key) || !documentIn.get(key).equals(query.get(key)) ) {
					inc = false;
					break;
				}
			
			// Should not include this document so continue to the next document
			if( !inc )
				continue;
			
			// Should include this document so set the out value to 
			// the in value so we can modify it without affecting the original
			documentOut = documentIn;
			
			for(String key : projectionKeys) {
				if( documentOut.containsKey(key) ) {
					System.out.println("projKey: (" + key.getClass() + ") " +key);
					System.out.println("projVal: (" + projection.get(key).getClass() + ") " + projection.get(key));
					if( (projection.get(key) instanceof Long && (Long)projection.get(key) == 0) ||
						(projection.get(key) instanceof Boolean && (Boolean)projection.get(key) == false ))
						
						// Removes the specific key if it has been 
						// set to false in the projection
						documentOut.remove(key);
				}
			}
			documents.add(documentOut);
		}
	}
	
	
	/**
	 * Gets the count of the number of documents in this Cursor
	 * 
	 * @return The number of documents
	 */
	public int count()
	{
		if( documents != null )
			return documents.size();
		return 0;
	}

	
	/**
	 * Removes a document from the Cursor
	 * 
	 * @param doc The document to remove
	 * @return true if the the document was in the Cursor
	 */
	public boolean remove(Document doc)
	{
		if( documents != null )
			return documents.remove(doc);
		return false;
	}
	
	
	/**
	 * Iterator wrapper to see if the next element exists
	 * 
	 * @return true if the iterator has more elements
	 */
	@Override
	public boolean hasNext()
	{
		if( documents != null )
			return documents.iterator().hasNext();
		return false;
	}

	
	/**
	 * Iterator wrapper for the next element
	 * 
	 * @return The next element in the iteration
	 */
	@Override
	public Document next() 
	{
		if( documents != null )
			return documents.iterator().next();
		return null;
	}

	
	/**
	 * Iterator wrapper to remove elements
	 */
	@Override
	public void remove()
	{
		if( documents != null )
			documents.iterator().remove();
	}
	
	
	@Override
	public String toString() {
		return documents.toString();
	}
}
