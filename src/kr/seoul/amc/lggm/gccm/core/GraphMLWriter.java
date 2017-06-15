package kr.seoul.amc.lggm.gccm.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class GraphMLWriter
{
  public HashMap<String, Node> nodes = new HashMap();
  public HashMap<String, Edge> edges = new HashMap();
  public List<Key> keys = new ArrayList();
  public float EdgeThreshhold = 1.0F;
  
  public GraphMLWriter() {}
  
  public GraphMLWriter(float threshold)
  {
    this.EdgeThreshhold = threshold;
  }
  
  public boolean AddNode(Node node)
  {
    if (!this.nodes.containsKey(node.ID))
    {
      this.nodes.put(node.ID, node);
      return true;
    }
    return false;
  }
  
  public boolean AddEdge(Edge edge)
  {
    if ((this.nodes.containsKey(edge.to)) && (this.nodes.containsKey(edge.from)) && 
      (edge.weight >= this.EdgeThreshhold) && 
      (!this.edges.containsKey(edge.ID)))
    {
      this.edges.put(edge.ID, edge);
      ((Node)this.nodes.get(edge.to)).degree += edge.weight;
      ((Node)this.nodes.get(edge.from)).degree += edge.weight;
      
      return true;
    }
    return false;
  }
  
  public boolean ContainNode(String nodeID)
  {
    return this.nodes.containsKey(nodeID);
  }
  
  public boolean ContainEdge(String edgeID)
  {
    return this.edges.containsKey(edgeID);
  }
  
  public void AddKey(Key key)
  {
    this.keys.add(key);
  }
  
  public void AddConnection(String NodeID, String connection)
  {
    ((Node)this.nodes.get(NodeID)).connections.add(connection);
  }
  
  public String GetGraphML()
  {
    String graphML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
    
    graphML = graphML + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" \r\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\nxsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \r\nhttp://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd\"> \r\n<!--Content: List of graphs and data--> \r\n";
    
    graphML = graphML + "<graph id=\"G\" edgedefault=\"directed\">\r\n";
    
    int size = this.keys.size();
    Key key = null;
    for (int i = 0; i < size; i++)
    {
      key = (Key)this.keys.get(i);
      graphML = graphML + "<key id=\"" + key.ID + "\" " + 
        "for=\"" + key.For + "\" " + 
        "attr.name=\"" + key.Name + "\" " + 
        "attr.type=\"" + key.Type + "\" />\r\n";
    }
    size = this.nodes.size();
    Node node = null;
    Iterator it = this.nodes.entrySet().iterator();
    Map.Entry pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      node = (Node)pair.getValue();
      if (node.degree >= 1.0F)
      {
        String nodeString = "<node id=\"" + node.ID + "\"> ";
        
        Iterator subit = node.Data.entrySet().iterator();
        while (subit.hasNext())
        {
          Map.Entry subpair = (Map.Entry)subit.next();
          nodeString = nodeString + "<data key=\"" + subpair.getKey() + "\">" + subpair.getValue() + "</data>";
        }
        nodeString = nodeString + "</node>\r\n";
        graphML = graphML + nodeString;
      }
    }
    size = this.edges.size();
    it = this.edges.entrySet().iterator();
    Edge edge = null;
    pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      edge = (Edge)pair.getValue();
      if ((((Node)this.nodes.get(edge.to)).degree >= 1.0F) && (((Node)this.nodes.get(edge.from)).degree >= 1.0F))
      {
        String nodeString = "<edge id=\"" + edge.ID + "\" " + 
          "source=\"" + edge.from + "\"  target=\"" + edge.to + "\"> ";
        Iterator subit = edge.Data.entrySet().iterator();
        while (subit.hasNext())
        {
          Map.Entry subpair = (Map.Entry)subit.next();
          nodeString = nodeString + "<data key=\"" + subpair.getKey() + "\">" + subpair.getValue() + "</data>";
        }
        nodeString = nodeString + "</edge>\r\n";
        graphML = graphML + nodeString;
      }
    }
    graphML = graphML + "</graph>\r\n</graphml>";
    
    return graphML;
  }
  
  public String GetCSVString()
  {
    String csvString = "";
    int size = this.keys.size();
    Key key = null;
    ArrayList<String> nodeKeysSeq = new ArrayList();
    for (int i = 0; i < size; i++)
    {
      key = (Key)this.keys.get(i);
      if ((key.For.equals("node")) || (key.For.equals("all")))
      {
        csvString = csvString + key.Name + "\t";
        nodeKeysSeq.add(key.ID);
      }
    }
    csvString = csvString + "Parent Node \r\n";
    
    Iterator it = null;
    Map.Entry pair = null;
    
    Map<String, ArrayList<String>> connectivyInfo = new HashMap();
    
    size = this.edges.size();
    it = this.edges.entrySet().iterator();
    Edge edge = null;
    pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      edge = (Edge)pair.getValue();
      if ((((Node)this.nodes.get(edge.to)).degree >= 1.0F) && (((Node)this.nodes.get(edge.from)).degree >= 1.0F))
      {
        if (!connectivyInfo.containsKey(edge.to)) {
          connectivyInfo.put(edge.to, new ArrayList());
        }
        ((ArrayList)connectivyInfo.get(edge.to)).add(edge.from);
      }
    }
    size = this.nodes.size();
    Node node = null;
    it = this.nodes.entrySet().iterator();
    pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      node = (Node)pair.getValue();
      if (node.degree >= 1.0F)
      {
        csvString = csvString + node.ID + ",";
        size = nodeKeysSeq.size();
        for (int i = 0; i < size; i++) {
          csvString = csvString + (String)node.Data.get(nodeKeysSeq.get(i)) + "\t";
        }
        csvString = csvString + connectivyInfo.get(node.ID) + "\r\n";
        System.out.println(csvString);
      }
    }
    return csvString;
  }
  
  public JSONObject GetJSONString()
  {
    JSONObject mainObject = new JSONObject();
    
    JSONArray jsonnodes = new JSONArray();
    JSONArray jsonedges = new JSONArray();
    
    Node node = null;
    Iterator it = this.nodes.entrySet().iterator();
    Map.Entry pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      node = (Node)pair.getValue();
      
      JSONObject jsonnode = new JSONObject();
      
      jsonnode.put("id", node.ID);
      jsonnode.put("label", node.Data.get("l0"));
      jsonnode.put("group", node.Data.get("dbName"));
      jsonnodes.put(jsonnode);
    }
    it = this.edges.entrySet().iterator();
    Edge edge = null;
    pair = null;
    while (it.hasNext())
    {
      pair = (Map.Entry)it.next();
      edge = (Edge)pair.getValue();
      
      JSONObject jsonedge = new JSONObject();
      jsonedge.put("id", edge.ID);
      
      jsonedge.put("source", edge.from);
      jsonedge.put("target", edge.to);
      
      jsonedges.put(jsonedge);
    }
    mainObject.put("nodes", jsonnodes);
    mainObject.put("edges", jsonedges);
    
    return mainObject;
  }
}
