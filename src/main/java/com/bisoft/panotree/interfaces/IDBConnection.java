package com.bisoft.panotree.interfaces;

import com.bisoft.navi.common.exceptions.DBConnectionException;

public interface IDBConnection {
	IOpenedConnection openedConnection() throws DBConnectionException;
	
}
