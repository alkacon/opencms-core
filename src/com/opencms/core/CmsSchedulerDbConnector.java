/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsSchedulerDbConnector.java,v $
 * Date   : $Date: 2000/06/05 13:37:50 $
 * Version: $Revision: 1.5 $
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
 * @version $Revision: 1.5 $ $Date: 2000/06/05 13:37:50 $
 */
public class CmsSchedulerDbConnector extends Thread implements I_CmsLogChannels {
    /** Time to sleep */
    private long m_sleep;
    
    /** Reference to the cms object used for queries.*/
    private CmsObject m_cms;
	
	/**
	 * Constructor for the Scheduler.
	 * @param cms cms object the scheduler uses for database queries.
	 * @param sleep time the scheduler has to pause between two actions.
	 */
	public CmsSchedulerDbConnector(CmsObject cms, long sleep) {
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
        // HACK: database connection is keeped alive by sending requesting all possible resource types via cms object.
        while(true) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Keeping alive database connection.");
            } 
            try {
                m_cms.getFilesInFolder("/");
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read files. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.getUsers();
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read users. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.getAllAccessibleProjects();
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read projects. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.getGroups();
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read groups. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.readAllProperties("/");
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read file properties. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.getGroupsOfUser("Admin");
            } catch(Exception e) {
                if(A_OpenCms.isLogging()){
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read groups of user. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.readTask(1);
            } catch(Exception e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read task. " + e);                                                                                                    
                }
            } 
            try {
                m_cms.readFileExtensions(); 
            } catch(Exception e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not read property. " + e);                                                                                                    
                }
            } 
      
            
            
            try {              
                sleep(m_sleep);            
            } catch(Exception e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsSchedulerDbConnector] Could not sleep. " + e);                                                                                                    
                }                
                return;
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
