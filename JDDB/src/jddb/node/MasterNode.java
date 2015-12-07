package jddb.node;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MasterNode extends Node
{
	private ServerSocket ssock = null;
	private List<SlaveNode> slaves = null;
	
	public MasterNode(Integer port)
	{
		super(port);
		
		slaves = Collections.synchronizedList(new ArrayList<SlaveNode>());
	}
}
