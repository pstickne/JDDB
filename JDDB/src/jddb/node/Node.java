package jddb.node;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import jddb.io.Console;

public class Node implements INode
{
	protected String SERVER = null;
	protected int PORT = 0;
	
	protected Socket csock = null;
	protected ServerSocket ssock = null;
	protected Console console = null;
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
