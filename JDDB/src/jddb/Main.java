package jddb;

import jddb.nosql.Collection;
import jddb.nosql.Document;

public class Main 
{
	public static void main(String ...args)
	{
		String basePath = "E:/Documents/4 College/Big Data/data/";
		String collectionName = "collection";
		
		Collection collection = new Collection(basePath, collectionName);
		collection.insert(new Document("{\"_id\": 100, \"hello\": 34}"));
	}
}
