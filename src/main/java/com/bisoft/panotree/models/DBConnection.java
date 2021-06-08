package com.bisoft.panotree.models;

import com.bisoft.navi.common.exceptions.DBConnectionException;
import com.bisoft.panotree.interfaces.IDBConnection;
import com.bisoft.panotree.interfaces.IOpenedConnection;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Map;

public class DBConnection implements IDBConnection {
	private final Map<String, String> resource;
	
	public DBConnection(Map<String, String> resource) {
		this.resource = resource;
	}
	
	@Override
	public IOpenedConnection openedConnection() throws DBConnectionException {
		Driver driver = GraphDatabase.driver( resource.get("neo.url"), AuthTokens.basic( resource.get("neo.username"), resource.get("neo.password") ) );
		if (driver == null) {
			throw new DBConnectionException(
				String.format("DB connection fail with url: <%s>, username: <%s>", resource.get("neo.url"), resource.get("neo.username"))
				, new Exception()
			);
		}
		return new OpenedConnection(driver);
	}
	
}
