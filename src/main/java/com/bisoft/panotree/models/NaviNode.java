package com.bisoft.panotree.models;

public class NaviNode {
	private String mnem;
	private String name;
	private String id;
	private String pid;
	private String otype;
	private Integer cnt;

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
	
	public void setId(String value) {
		id = value;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public void setMnem(String value) {
		mnem = value;
	}
	
	public void setPid(String value) {
		pid = value;
	}

	public NaviNode() {
	}

	public NaviNode(String id, String mnem, String name, String pid, String otype, Integer cnt) {
		this.id = id;
		this.mnem = mnem;
		this.name = name;
		this.pid = pid;
		this.otype = otype;
		this.cnt = cnt;
	}
	
	public String getOtype() {
		return otype;
	}

	public Integer getCnt() {
		return cnt;
	}

	public void setCnt(Integer cnt) {
		this.cnt = cnt;
	}
}
