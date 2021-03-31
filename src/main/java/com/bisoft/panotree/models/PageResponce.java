package com.bisoft.panotree.models;

public class PageResponce {
	
	public Object children;
	public int total;
	
	public PageResponce(Object body, int count) {
		children = body;
		total = count;
	}
}