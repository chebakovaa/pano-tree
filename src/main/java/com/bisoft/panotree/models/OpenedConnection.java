package com.bisoft.panotree.models;

import com.bisoft.panotree.interfaces.IOpenedConnection;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public class OpenedConnection implements IOpenedConnection {
	private final Driver driver;
	
	public OpenedConnection(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public Session session() {
		return driver.session();
	}
	
	@Override
	public void close() throws Exception {
		driver.close();
	}
}
