package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsConnectionGuard.java,v $
 * Date   : $Date: 2000/12/21 11:12:36 $
 * Version: $Revision: 1.2 $
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

import com.opencms.file.utils.*;
import com.opencms.core.*;
import com.opencms.file.genericSql.I_CmsDbPool;


import java.util.*;
import java.sql.*;

/**
 * Database scheduler class.
 * <P>
 * Runs as a thread and keeps all database connections alive by
 * sending a query all <code>sleep</code> seconds.
 * 
 * @author Alexander Lucas
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/12/21 11:12:36 $
 */
public class CmsConnectionGuard extends com.opencms.file.genericSql.CmsConnectionGuard {
/**
	 * Constructor for the Scheduler.
	 * @param pool pool the scheduler uses for database queries.
	 * @param sleep time the scheduler has to pause between two actions.
	 */
	public CmsConnectionGuard(I_CmsDbPool pool, long sleep) {
		super(pool,sleep);
}
/**
 * Main loop of the Scheduler.
 * So far, it only has one task, to keep querying different results from 
 * the database. Between the queries it will pause a given time.
 * <P>
 * This is done because the connection between Database and Servlet is cut after some time.
 */
public void run() {
	Vector connections = null;
	Connection connection;
	Statement statement;
	while (true) {
		try {
			sleep(m_sleep);
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] Could not sleep. " + e.getMessage());
			}
			return;
		}
		if (OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] Keeping alive database connection.");
		}

		// get all connections
		connections = ((com.opencms.file.oracleplsql.CmsDbPool) m_pool).getAllConnections();
		//((com.opencms.file.oracleplsql.CmsDbPool)m_pool ).keepAlive();
		if (connections != null) {
			for (int i = 0; i < connections.size(); i++) {
				try {
					connection = (Connection) connections.elementAt(i);
					statement = connection.createStatement();
					statement.execute("select null from dual");
					statement.close();
				} catch (SQLException exc) {
					if (OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] error while sending keep-alive query: " + exc.getMessage());
					}
				}
			}
		} else {
			if (OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] error while keeping-alive database connection.");
			}
		}
	}
}
}
