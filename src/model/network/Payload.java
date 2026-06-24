package model.network;

import java.io.Serializable;

public class Payload implements Serializable{
		
	private static final long serialVersionUID = 1L;
	
	public final String type;
	public final Object data;
	public final Object more;
	
	private boolean broadcast = false;
	public boolean isBroadcast() {return this.broadcast;}
	public void setBroadcast(boolean value) {this.broadcast = value;}
	
	public Payload(String type, Object data) {
		this.type = type;
		this.data = data;
		this.more = null;
	}
	
	public Payload(String type, Object data, Object more) {
		this.type = type;
		this.data = data;
		this.more = more;
	}
	
//	public String toString() {
//		String result = "  Payload: " + getClass();
//		result += "\n  type: " + getType();
//		result += "\n  data: " + getData();
//		result += "\n  more: " + getMore();
//		return result;
//	}
	
	public String toString() {
		String result = "\n[payload]  |  " + "type: " + getType() + "; "
				      + "\n           |  " + "data: " + getData() + "; "
				      + "\n           |  " + "more: " + getMore();
		return result;
	}
	
	public String getType() {return this.type;}
	public Object getData() {return this.data;}
	public Object getMore() {return this.more;}
	
	public void print() {
		System.out.println("  type: " + type);
		System.out.println("  data: " + data.toString());
		System.out.println("  more: " + more.toString());
	}
	
}
