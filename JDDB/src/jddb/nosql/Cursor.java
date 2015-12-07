package jddb.nosql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
}
