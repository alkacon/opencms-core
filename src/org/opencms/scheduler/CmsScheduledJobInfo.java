/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsScheduledJobInfo.java,v $
 * Date   : $Date: 2004/07/07 18:01:08 $
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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Describes a scheduled job for the OpenCms scheduler.<p>
 */
public class CmsScheduledJobInfo implements I_CmsConfigurationParameterHandler {

    /** Error message if a configuration change is attempted after configuration is frozen. */
    public static final String C_MESSAGE_FROZEN = "Job configuration has been frozen and can not longer be changed!";

    /** The name of the class to schedule. */
    private String m_className;

    /** The context information for the user to execute the job with. */
    private CmsContextInfo m_context;

    /** The cron expression for this scheduler job. */
    private String m_cronExpression;

    /** Indicates if the configuration of this job is finalized (frozen). */
    private boolean m_frozen;

    /** Instance object of the scheduled job (only required when instance is re-used). */
    private I_CmsScheduledJob m_jobInstance;

    /** The name of the job (for information purposes). */
    private String m_jobName;

    /** The parameters used for this job entry. */
    private ExtendedProperties m_parameters;

    /** Indicates if the job instance should be re-used if the job is run. */
    private boolean m_reuseInstance;

    /**
     * Default constructor.<p>
     */
    public CmsScheduledJobInfo() {

        m_reuseInstance = false;
        m_frozen = false;
        m_parameters = new ExtendedProperties();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        // add the configured parameter
        m_parameters.addProperty(paramName, paramValue);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(
                "addConfigurationParameter(" + paramName + ", " + paramValue + ") called on " + this);
        }
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
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        // this configuration does not support parameters
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("getConfiguration() called on " + this);
        }
        return getParameters();
    }

    /**
     * Returns the context information for the user executing the job.<p>
     *
     * @return the context information for the user executing the job
     */
    public CmsContextInfo getContextInfo() {

        return m_context;
    }

    /**
     * Returns the cron expression for this job entry.<p>
     * 
     * @return the cron expression for this job entry
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
    public synchronized I_CmsScheduledJob getJobInstance() {

        if (m_jobInstance != null) {
            
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Scheduler: Re-using instance of '" + m_jobInstance.getClass().getName() + "'");
            }
            
            // job instance already initialized
            return m_jobInstance;
        }

        I_CmsScheduledJob job = null;

        try {
            // create an instance of the OpenCms job class
            job = (I_CmsScheduledJob)Class.forName(getClassName()).newInstance();
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
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Scheduler: Created a new instance of '" + getClassName() + "'");
        }        

        return job;
    }

    /**
     * Returns the job name.<p>
     *
     * @return the job name
     */
    public String getJobName() {

        return m_jobName;
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
     * Finalizes (freezes) the configuration of this scheduler job entry.<p>
     * 
     * After this job entry has been frozen, any attempt to change the 
     * configuration of this entry with one of the "set..." methods
     * will lead to a <code>RuntimeException</code>.<p> 
     * 
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // simple default configuration does not need to be initialized
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("initConfiguration() called on " + this);
        }
        m_frozen = true;
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

        if (m_jobName == null) {
            // initialize job name with class name as default
            setJobName(className);
        }
    }

    /**
     * Sets the context information for the user executing the job.<p>
     *
     * This will also "freeze" the context information that is set.<p>
     *
     * @param contextInfo the context information for the user executing the job
     * 
     * @see CmsContextInfo#freeze()
     */
    public void setContextInfo(CmsContextInfo contextInfo) {

        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }

        contextInfo.freeze();
        m_context = contextInfo;
    }

    /**
     * Sets the cron expression for this job entry.<p>
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
     * Sets the job name.<p>
     *
     * @param jobName the job name to set
     */
    public void setJobName(String jobName) {

        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }

        m_jobName = jobName;
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
}