/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/CmsConnectionGuard.java,v $
 * Date   : $Date: 2000/07/17 10:20:53 $
 * Version: $Revision: 1.8 $
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

package com.opencms.file.genericSql;

import com.opencms.file.utils.*;
import com.opencms.core.*;

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
 * @version $Revision: 1.8 $ $Date: 2000/07/17 10:20:53 $
 */
public class CmsConnectionGuard extends Thread implements I_CmsLogChannels {
	/** The keep-alive statement */
	private static String C_KEEP_ALIVE_STATEMENT = "select 1";
	
    /** Time to sleep */
    private long m_sleep;
    
    /** Reference to the pool used for queries.*/
    private CmsDbPool m_pool;
	
	/**
	 * Constructor for the Scheduler.
	 * @param pool pool the scheduler uses for database queries.
	 * @param sleep time the scheduler has to pause between two actions.
	 */
	public CmsConnectionGuard(CmsDbPool pool, long sleep) {
		m_pool = pool;
		m_sleep=60000*sleep;
	}
	
	/**
	 * Main loop of the Scheduler.
	 * So far, it only has one task, to keep querying different results from 
	 * the database. Between the queries it will pause a given time.
	 * <P>
	 * This is done because the connection between Database and Servlet is cut after some time.
	 */
    public void run() { 
		Vector connections;
		Connection connection;
		Statement statement;
        while(true) {
			
            try {              
                sleep(m_sleep);            
            } catch(Exception e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] Could not sleep. " + e.getMessage());
                }                
                return;
            }
			
            if(OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsConnectionGuard] Keeping alive database connection.");
            } 
			
			// get all connections
			m_pool.keepAlive();
        }
    }
                      	
	/**
	 * Destroys the Thread.
	 */
	public void destroy() {
	    super.stop();
    }
}
