/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/Attic/CmsSchedulerEntry.java,v $
 * Date   : $Date: 2004/07/05 15:35:12 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.scheduler;

import org.opencms.main.OpenCms;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Describes a job entry for the OpenCms scheduler.<p>
 */
public class CmsSchedulerEntry {
    
    /** Message if a configuration error occurs. */
    public static final String C_MESSAGE_FROZEN = "Job entry has been frozen and can not longer be changed!";
    
    /** The name of the class to schedule. */
    private String m_className;

    /** The cron expression for this scheduler entry. */
    private String m_cronExpression;
    
    /** Indicates if the configuration of this entry is finalized (frozen). */
    private boolean m_frozen;
    
    /** Instance object of the scheduled job (only required when instance is re-used). */
    private I_CmsSchedulerJob m_jobInstance;       
    
    /** The parameters used for this scheduler entry. */
    private ExtendedProperties m_parameters;
    
    /** Indicates if the job instance should be re-used if the job is run. */
    private boolean m_reuseInstance;
    
    /** The name of the user to execute the job with. */
    private String m_userName; 
    
    /**
     * Default constructor.<p>
     */
    public CmsSchedulerEntry() {
        
        m_reuseInstance = false;
        m_frozen = false;
        m_parameters = new ExtendedProperties();
    }
    
    /**
     * Finializes (freezes) the configuration of this scheduler job entry.<p>
     * 
     * After this entry has been frozen, any attempt to change the 
     * configuration of this entry with one of the "set..." methods
     * will lead to a <code>RuntimeException</code>.<p> 
     */
    public void freeze() {
        
        m_frozen = true;
    }
    
    /**
     * Returns the name of the class to schedule.<p>
     * 
     * @return the name of the class to schedule
     */
    public String getClassName() {

        return m_className;
    }
    
    /**
     * Returns the cron expression for this scheduler entry.<p>
     * 
     * @return the cron expression for this scheduler entry
     */
    public String getCronExpression() {

        return m_cronExpression;
    }
    
    
    /**
     * Returns an instance of the configured job class.<p>
     * 
     * If any error occurs during class invocaion, the error 
     * is written to the OpenCms log and <code>null</code> is returned.<p>
     *
     * @return an instance of the configured job class, or null if an error occured
     */
    public synchronized I_CmsSchedulerJob getJobInstance() {

        if (m_jobInstance != null) {
            // job instance already initialized
            return m_jobInstance;
        }
        
        I_CmsSchedulerJob job = null;
        
        try {
            // create an instance of the OpenCms job class
            job = (I_CmsSchedulerJob)Class.forName(getClassName()).newInstance();
        } catch (ClassNotFoundException e) {
            OpenCms.getLog(this).error("Scheduler: Scheduled class not found '" + getClassName() + "'", e);
        } catch (IllegalAccessException e) {
            OpenCms.getLog(this).error("Scheduler: Illegal access", e);
        } catch (InstantiationException e) {
            OpenCms.getLog(this).error("Scheduler: Instantiation error", e);
        } catch (ClassCastException e) {
            OpenCms.getLog(this).error("Scheduler: Scheduled class does not implement scheduler interface", e);
        } 
        
        if (m_reuseInstance) {
            // job instance must be re-used
            m_jobInstance = job;
        }
        
        return job;
    }
        
    /**
     * Returns the parameters.<p>
     *
     * @return the parameters
     */
    public ExtendedProperties getParameters() {

        return m_parameters;
    }
        
    /**
     * Returns the user name.<p>
     *
     * @return the user name
     */
    public String getUserName() {

        return m_userName;
    }
    
    
    /**
     * Returns true if the job instance class is reused for this job.<p>
     *
     * @return true if the job instance class is reused for this job
     */
    public boolean isReuseInstance() {

        return m_reuseInstance;
    }
    
    /**
     * Sets the name of the class to schedule.<p>
     * 
     * @param className the class name to set
     */
    public void setClassName(String className) {
        
        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        
        m_className = className;    
    }
    
    /**
     * Sets the cron expression for this scheduler entry.<p>
     * 
     * @param cronExpression the cron expression to set
     */
    public void setCronExpression(String cronExpression) {

        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        
        m_cronExpression = cronExpression;
    }
        
    /**
     * Controls if the job instance class is reused for this job,
     * of if a new instance is generated every time the job is run.<p>
     * 
     * @param reuseInstance must be true if the job instance class is to be reused
     */
    public void setReuseInstance(boolean reuseInstance) {
        
        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        
        m_reuseInstance = reuseInstance;
    }
        
    /**
     * Sets the user name.<p>
     *
     * @param userName the user name to set
     */
    public void setUserName(String userName) {
        
        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        
        m_userName = userName;
    }
}
