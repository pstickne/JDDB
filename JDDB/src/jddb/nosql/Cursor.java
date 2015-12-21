package jddb.nosql;

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
	
	public Cursor(Collection collection, Document query, Document projection)
	{
		this.collection = collection;
		this.query = query;
		this.projection = projection;
		
		documents = new ArrayList<Document>();
		runQuery();
	}
	
	private void runQuery()
	{
		boolean inc = true;
		Document documentIn = null, documentOut = null;
		Set<String> queryKeys = query.getKeys(); 
		Set<String> projectionKeys = projection.getKeys();
		Iterator<Document> it = collection.documents.iterator();
		
		while( it.hasNext() )
		{
			inc = true;
			documentIn = it.next();
			
			for (String key : queryKeys)
				if( !documentIn.containsKey(key) || !documentIn.get(key).equals(query.get(key)) ) {
					inc = false;
					break;
				}
			
			if( !inc )
				continue;
			
			documentOut = documentIn;
			
			for(String key : projectionKeys) {
				if( documentOut.containsKey(key) ) {
					System.out.println("projKey: (" + key.getClass() + ") " +key);
					System.out.println("projVal: (" + projection.get(key).getClass() + ") " + projection.get(key));
					if( (projection.get(key) instanceof Long && (Long)projection.get(key) == 0) ||
						(projection.get(key) instanceof Boolean && (Boolean)projection.get(key) == false ))
						documentOut.remove(key);
				}
			}
			documents.add(documentOut);
		}
	}
	
	public int count()
	{
		if( documents != null )
			return documents.size();
		return 0;
	}

	public boolean remove(Document doc)
	{
		if( documents != null )
			return documents.remove(doc);
		return false;
	}
	
	@Override
	public boolean hasNext()
	{
		if( documents != null )
			return documents.iterator().hasNext();
		return false;
	}

	@Override
	public Document next() 
	{
		if( documents != null )
			return documents.iterator().next();
		return null;
	}

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
