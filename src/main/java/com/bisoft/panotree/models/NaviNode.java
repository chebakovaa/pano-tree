package com.bisoft.panotree.models;

public class NaviNode {
	private String mnem;
	private String name;
	private Integer id;
	private Integer pid;
	private String otype;
	private Integer cnt;

	public Integer getId() {
		return id;
	}
	
	public String getMnem() {
		return mnem;
	}
	
	public String getName() {
		return name;
	}
	
	public Integer getPid() {
		return pid;
	}
	
	public void setId(Integer value) {
		id = value;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public void setMnem(String value) {
		mnem = value;
	}
	
	public void setPid(Integer value) {
		pid = value;
	}

	public NaviNode() {
	}

	public NaviNode(Integer id, String mnem, String name, Integer pid, String otype, Integer cnt) {
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
