package kr.seoul.amc.lggm.gccm.core;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphObject
{
  public String ObjectID;
  public String ObjectLabel;
  public String ObjectURL;
  public ArrayList<String> ObjectConntions;
  public HashMap<String, String> props;
  
  public GraphObject(String ID, String Label, String ConnectedTo, String url)
  {
    this.ObjectID = ID;
    this.ObjectLabel = Label;
    this.ObjectURL = url;
    this.props = new HashMap();
    this.ObjectConntions = new ArrayList();
    this.ObjectConntions.add(ConnectedTo);
  }
  
  public void AddConnection(String ConnectedTo)
  {
    if (!this.ObjectConntions.contains(ConnectedTo)) {
      this.ObjectConntions.add(ConnectedTo);
    }
  }
  
  public void AddData(String key, String value)
  {
    this.props.put(key, value);
  }
  
  public void UpdateData(String key, String value)
  {
    String oldValue = (String)this.props.get(key);
    String newValue = oldValue + "; " + value;
    this.props.put(key, newValue);
  }
  
  public String GetData(String key)
  {
    return (String)this.props.get(key);
  }
  
  public boolean HasMultiRelations()
  {
    if (this.ObjectConntions.size() > 1) {
      return true;
    }
    return false;
  }
  
  public boolean HasDefinedRelations(int i)
  {
    if (this.ObjectConntions.size() >= i) {
      return true;
    }
    return false;
  }
}
