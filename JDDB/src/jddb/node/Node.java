package jddb.node;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Node implements INode
{
	protected String SERVER = null;
	protected int PORT = 0;
	
	protected String BASEPATH = null;
	protected String COLLECTIONFILE = null;
	
	protected Socket csock = null;
	protected ServerSocket ssock = null;
	
	protected Properties properties = null;
	
	public Node(Properties prop)
	{
		properties = prop;
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}
}
