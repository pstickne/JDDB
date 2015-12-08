package jddb;

import java.io.IOException;

import jddb.nosql.Collection;
import jddb.nosql.Document;

public class Main 
{
	public static void main(String ...args)
	{
		String basePath = "C:/jddb/";
		String file = "collection.json";
		
		Document query = new Document("{\"group\": 1}");
		Collection collection = new Collection(basePath, file);
		
		for( int i = 0; i < 4; i++ ) {
			for( int j = 0; j < 20000; j++ )
				collection.insert(new Document("{\"_id\": " + (i*j+j) + ", \"group\": " + ((i*j+j)%2000) + "}"));
			System.gc();
		}
		
//		System.out.println(collection.find(query));
		
		try {
			collection.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
