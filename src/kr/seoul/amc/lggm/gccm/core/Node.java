package kr.seoul.amc.lggm.gccm.core;

import java.util.HashMap;
import java.util.HashSet;

public class Node
{
  public String ID;
  public HashMap<String, String> Data = new HashMap();
  public HashSet<String> connections = new HashSet();
  public float degree = 0.0F;
  
  public Node(String ID)
  {
    this.ID = ID;
    this.degree = 0.0F;
  }
  
  public void IncreaseDegree()
  {
    this.degree += 1.0F;
  }
  
  public void AddData(String key, String value)
  {
    this.Data.put(key, value);
  }
  
  public void UpdateData(String key, String value)
  {
    this.Data.put(key, (String)this.Data.get(key) + ";" + value);
  }
}
