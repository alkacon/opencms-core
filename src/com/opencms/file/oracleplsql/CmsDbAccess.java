package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/09/18 12:48:44 $
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

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;
import com.opencms.util.*;
import com.opencms.file.genericSql.I_CmsDbPool;



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.1 $ $Date: 2000/09/18 12:48:44 $ * 
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {
	/**
	 * Instanciates the access-module and sets up all required modules and connections.
	 * @param config The OpenCms configuration.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsDbAccess(Configurations config) 
		throws CmsException {

		super(config);
	}
/**
 * Create a new Connection guard.
 * This method should be overloaded if another connectionguard should be used.
 * Creation date: (06-09-2000 14:33:30)
 * @return com.opencms.file.genericSql.CmsConnectionGuard
 * @param m_pool com.opencms.file.genericSql.I_CmsDbPool
 * @param sleepTime long
 */
public com.opencms.file.genericSql.CmsConnectionGuard createCmsConnectionGuard(I_CmsDbPool m_pool, long sleepTime) {
	return new com.opencms.file.oracleplsql.CmsConnectionGuard(m_pool, sleepTime);
}
/**
 * Creates a CmsDbPool
 * Creation date: (06-09-2000 14:08:10)
 * @return com.opencms.file.genericSql.CmsDbPool
 * @param driver java.lang.String
 * @param url java.lang.String
 * @param user java.lang.String
 * @param passwd java.lang.String
 * @param maxConn int
 * @exception com.opencms.core.CmsException The exception description.
 */
public I_CmsDbPool createCmsDbPool(String driver, String url, String user, String passwd, int maxConn) throws com.opencms.core.CmsException {
	return new com.opencms.file.oracleplsql.CmsDbPool(driver,url,user,passwd,maxConn);
}
/**
 * retrieve the correct instance of the queries holder.
 * This method should be overloaded if other query strings should be used.
 */
protected com.opencms.file.genericSql.CmsQueries getQueries()
{
	return new com.opencms.file.oracleplsql.CmsQueries();
}
	/**
	 * Private method to init all statements in the pool.
	 */
  protected void initStatements () throws CmsException
  {
	  super.initStatements();
  }    
}
