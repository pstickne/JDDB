package jddb.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class Node implements INode
{
	protected Socket csock = null;
	protected ServerSocket ssock = null;
	
	protected String thisIP = "";
	protected Integer thisPort = 0;
	
	public Node(Integer port)
	{
		thisPort = port;
		thisIP = getMyIP();
	}
	
	protected String getMyIP()
	{
		URL checkIP = null;
		BufferedReader in = null; 
		String ip = null;
		try {
			checkIP = new URL("http://checkip.amazonaws.com");
			in = new BufferedReader(new InputStreamReader(checkIP.openStream()));
			ip = in.readLine();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ip;
	}
}
