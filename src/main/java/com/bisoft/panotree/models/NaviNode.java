package com.bisoft.panotree.models;

public class NaviNode {
	private String mnem;
	private String name;
	private String id;
	private String pid;
	
	public String getId() {
		return id;
	}
	
	public String getMnem() {
		return mnem;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPid() {
		return pid;
	}
	
	public NaviNode(String id, String mnem, String name, String pid) {
		this.id = id;
		this.mnem = mnem;
		this.name = name;
		this.pid = pid;
	}
	
}
