package jddb.node;

import java.util.List;

import jddb.nosql.Collection;

public class Shard 
{
	public static final long SHARD_SIZE = 2 * 1024 * 1024 * 1024; // 2 GB
	
	private List<Collection> collections = null;
	
}
