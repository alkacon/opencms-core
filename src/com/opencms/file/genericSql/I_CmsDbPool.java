package com.opencms.file.genericSql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/I_CmsDbPool.java,v $
 * Date   : $Date: 2000/09/06 15:50:05 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 
import java.sql.PreparedStatement;
import java.sql.Statement;
import com.opencms.core.CmsException;

/**
 * Interface for CmsDBPool.
 * Creation date: (06-09-2000 13:42:40)
 * @author: Anders Fugmann
 * @author: Jan Krag
 */ 
public interface I_CmsDbPool {

	/**
	 * Gets a PreparedStatement object and remove it from the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @return a prepared statement matching the key
	 */	
  public PreparedStatement getPreparedStatement(Integer key) throws CmsException;          
	/**
	 * Gets a (Simple)Statement object and remove it from the list of available statements.
	 * 
	 * @return a statement to execute queries on.
	 */

  public Statement getStatement() throws CmsException;          
 	/**
	 * Init the PreparedStatement on all connections.
	 * 
	 * @param key - the hashtable key
	 * @param sql - a SQL statement that may contain one or more '?' IN parameter placeholders
	 */  
  public void initPreparedStatement(Integer key, String sql) throws CmsException;            
  	/**
	 * Add the given statement to the list of available statements.
	 * 
	 * @param key - the hashtable key
	 * @param pstmt - the statement
	 */
  public void putPreparedStatement(Integer key, PreparedStatement pstmt);        
}
