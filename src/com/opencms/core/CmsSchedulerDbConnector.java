/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsSchedulerDbConnector.java,v $
 * Date   : $Date: 2000/02/16 18:57:40 $
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

package com.opencms.core;

import com.opencms.file.*;

/**
 * Database scheduler class.
 * <P>
 * Runs as a thread and keeps all database connections alive by
 * sending a query all <code>sleep</code> seconds.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/02/16 18:57:40 $
 */
public class CmsSchedulerDbConnector extends Thread implements I_CmsLogChannels {
    /** Time to sleep */
    private long m_sleep;
    
    /** Reference to the cms object used for queries.*/
    private A_CmsObject m_cms;
	
	/**
	 * Constructor for the Scheduler.
	 * @param cms cms object the scheduler uses for database queries.
	 * @param sleep time the scheduler has to pause between two actions.
	 */
	public CmsSchedulerDbConnector(A_CmsObject cms, long sleep) {
		m_cms = cms;
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
        while(true) {
            try {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Keeping alive database connection.");
                }
                m_cms.getFilesInFolder("/");
                m_cms.getUsers();
                m_cms.getAllAccessibleProjects();
                m_cms.getGroups();
                m_cms.readAllMetainformations("/");
                m_cms.getGroupsOfUser("Admin");                
                sleep(m_sleep);
            } catch(Exception e) {
                System.err.println("*E*");
                e.printStackTrace();
            }
        }
    }
                      	
	/**
	 * Destroys the Thread.
	 */
	public void destroy() {
	    super.stop();
    }
}
