/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsSystemConfiguration.java,v $
 * Date   : $Date: 2004/08/06 16:17:42 $
 * Version: $Revision: 1.10 $
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

package org.opencms.configuration;

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.mail.CmsMailHost;
import org.opencms.mail.CmsMailSettings;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsRequestHandler;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.CmsScheduleManager;
import org.opencms.scheduler.CmsScheduledJobInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * VFS master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsSystemConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {

    /** The node name for a job class. */
    protected static final String N_CLASS = "class";   
    
    /** The node name for the job context. */
    protected static final String N_CONTEXT = "context"; 
    
    /** The node name for the job cron expression. */
    protected static final String N_CRONEXPRESSION = "cronexpression"; 
    
    /** The node name for the context encoding. */
    protected static final String N_ENCODING = "encoding";     
    
    /** The node name for the internationalization node. */
    protected static final String N_I18N = "internationalization";
    
    /** The node name for a job. */
    protected static final String N_JOB = "job";
    
    /** The node name for individual locales. */
    protected static final String N_LOCALE = "locale";
    
    /** The node name for the locale handler. */
    protected static final String N_LOCALEHANDLER = "localehandler";
    
    /** The node name for the configured locales. */
    protected static final String N_LOCALESCONFIGURED = "localesconfigured";
    
    /** The node name for the default locale(s). */
    protected static final String N_LOCALESDEFAULT = "localesdefault";    
    
    /** The node name for the mail configuration. */
    protected static final String N_MAIL = "mail";
    
    /** The node name for the "mail from" node. */
    protected static final String N_MAILFROM = "mailfrom";

    /** The node name for the "mail host" node. */
    protected static final String N_MAILHOST = "mailhost";
    
    /** The node name for a job name. */
    protected static final String N_NAME = "name";
    
    /** The node name for the job parameters. */
    protected static final String N_PARAMETERS = "parameters";     
    
    /** The node name for the context project name. */
    protected static final String N_PROJECT = "project";         
    
    /** The node name for the context remote addr. */
    protected static final String N_REMOTEADDR = "remoteaddr";     
    
    /** The node name for the context requested uri. */
    protected static final String N_REQUESTEDURI = "requesteduri";   
    
    /** The node name for the request handler classes. */
    protected static final String N_REQUESTHANDLER = "requesthandler";    
    
    /** The node name for the request handlers. */
    protected static final String N_REQUESTHANDLERS = "requesthandlers";
    
    /** The node name for the resource init classes. */
    protected static final String N_RESOURCEINIT = "resourceinit";
    
    /** The node name for the resource init classes. */
    protected static final String N_RESOURCEINITHANDLER = "resourceinithandler";
    
    /** The node name for the job "reuseinstance" value. */
    protected static final String N_REUSEINSTANCE = "reuseinstance";  
    
    /** The node name for the scheduler. */
    protected static final String N_SCHEDULER = "scheduler";

    /** The node name for the context site root. */
    protected static final String N_SITEROOT = "siteroot"; 
    
    /** The main system configuration node name. */
    protected static final String N_SYSTEM = "system";
    
    /** The node name for the context user name. */
    protected static final String N_USERNAME = "user";     
        
    /** The node name for the version history. */
    protected static final String N_VERSIONHISTORY = "versionhistory";
    
    /** The name of the DTD for this configuration. */
    private static final String C_CONFIGURATION_DTD_NAME = "opencms-system.dtd";
    
    /** The name of the default XML file for this configuration. */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms-system.xml";    
           
    /** The list of jobs for the scheduler. */
    private List m_configuredJobs;
    
    /** The configured locale manager for multi language support. */
    private CmsLocaleManager m_localeManager;
    
    /** The mail settings. */
    private CmsMailSettings m_mailSettings;
    
    /** A list of instanciated request handler classes. */
    private List m_requestHandlers;        
    
    /** A list of instanciated resource init handler classes. */
    private List m_resourceInitHandlers;
    
    /** The configured schedule manager. */
    private CmsScheduleManager m_scheduleManager;
    
    /** The temporary file project id. */
    private int m_tempFileProjectId;
    
    /** Indicates if the version history is enabled. */
    private boolean m_versionHistoryEnabled;
    
    /** The maximum number of entries in the version history (per resource). */
    private int m_versionHistoryMaxCount;
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsSystemConfiguration() {
        setXmlFileName(C_DEFAULT_XML_FILE_NAME);
        m_versionHistoryEnabled = true;
        m_versionHistoryMaxCount = 10;
        m_resourceInitHandlers = new ArrayList();
        m_requestHandlers = new ArrayList();
        m_configuredJobs = new ArrayList();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". System configuration : initialized");
        }           
    }
        
    /**
     * Adds a new job description for the scheduler.<p>
     * 
     * @param jobInfo the job description to add
     * 
     * @throws CmsException if called after configuration is finished
     */
    public void addJobFromConfiguration(CmsScheduledJobInfo jobInfo) throws CmsException {

        if (OpenCms.getRunLevel() > 1) {
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR);
        }
        
        m_configuredJobs.add(jobInfo);
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Scheduler config     : configured job named '" 
                    + jobInfo.getJobName()                    
                    + "' for class '"
                    + jobInfo.getClassName()
                    + "' with user "
                    + jobInfo.getContextInfo().getUserName());
        }          
    }    
    
    /**
     * Adds a new instance of a request handler class.<p>
     * 
     * @param clazz the class name of the request handler to instanciate and add
     */
    public void addRequestHandler(String clazz) {
        Object initClass;
        try {
            initClass = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            OpenCms.getLog(this).error(". Request handler class '" + clazz  + "' could not be instanciated", t);
            return;
        }
        if (initClass instanceof I_CmsRequestHandler) {
            m_requestHandlers.add(initClass);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Request handler      : " + clazz + " instanciated");
            }
        } else {        
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).error(". Request handler      : " + clazz + " invalid");
            }
        }
    }
    
    /**
     * Adds a new instance of a resource init handler class.<p>
     * 
     * @param clazz the class name of the resource init handler to instanciate and add
     */
    public void addResourceInitHandler(String clazz) {
        Object initClass;
        try {
            initClass = Class.forName(clazz).newInstance();
        } catch (Throwable t) {
            OpenCms.getLog(this).error(". Resource init class '" + clazz  + "' could not be instanciated", t);
            return;
        }
        if (initClass instanceof I_CmsResourceInit) {
            m_resourceInitHandlers.add(initClass);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Resource init class  : " + clazz + " instanciated");
            }
        } else {        
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).error(". Resource init class  : " + clazz + " invalid");
            }
        }
    }
    
    /**
     * Generates the schedule manager.<p>
     */
    public void addScheduleManager() {
        
        m_scheduleManager = new CmsScheduleManager(m_configuredJobs);   
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {
        // add finish rule
        digester.addCallMethod("*/" + N_SYSTEM, "initializeFinished");    
        
        // add rule for internationalization
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_I18N, CmsLocaleManager.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N, "setLocaleManager");
        
        // add locale handler creation rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, "setLocaleHandler");

        // add locale rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESCONFIGURED + "/" + N_LOCALE, "addAvailableLocale", 0);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALESDEFAULT + "/" + N_LOCALE, "addDefaultLocale", 0);
        
        // add version history rules
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, "setVersionHistorySettings", 2);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 0, A_ENABLED);    
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_VERSIONHISTORY, 1, A_COUNT);
        
        // add mail configuration rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_MAIL, CmsMailSettings.class);
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILFROM, "setMailFromDefault", 0);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_MAIL, "setMailSettings");
        
        // add mail host configuration rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, "addMailHost", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 0, A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 1, A_ORDER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 2, A_PROTOCOL);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 3, A_USER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 4, A_PASSWORD);

        // add scheduler creation rule
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_SCHEDULER, "addScheduleManager");
        
        // add scheduler job creation rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB, CmsScheduledJobInfo.class);
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_NAME, "jobName");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CLASS, "className");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CRONEXPRESSION, "cronExpression");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_REUSEINSTANCE, "reuseInstance");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB, "addJobFromConfiguration");
        
        // add job context creation rule
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT, CmsContextInfo.class);
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_USERNAME, "userName");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_PROJECT, "projectName");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_SITEROOT, "siteRoot");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_REQUESTEDURI, "requestedUri");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_LOCALE, "localeName");
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_ENCODING);
        digester.addBeanPropertySetter("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT + "/" + N_REMOTEADDR, "remoteAddr");
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_SCHEDULER + "/" + N_JOB + "/" + N_CONTEXT, "setContextInfo");        
        
        // add generic parameter rules (used for jobs)
        digester.addCallMethod("*/" + I_CmsXmlConfiguration.N_PARAM, I_CmsConfigurationParameterHandler.C_ADD_PARAMETER_METHOD, 2);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 1);         
        
        // add resource init classes
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER, "addResourceInitHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_RESOURCEINIT + "/" + N_RESOURCEINITHANDLER, 0, A_CLASS);    

        // add request handler classes
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER, "addRequestHandler", 1);
        digester.addCallParam("*/" + N_SYSTEM + "/" +  N_REQUESTHANDLERS + "/" + N_REQUESTHANDLER, 0, A_CLASS);    
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate vfs node and subnodes
        Element systemElement = parent.addElement(N_SYSTEM);        
        
        if (OpenCms.getRunLevel() > 1) {
            // initialized OpenCms instance is available, use latest values
            m_localeManager = OpenCms.getLocaleManager();
            m_mailSettings = OpenCms.getSystemInfo().getMailSettings();
            m_configuredJobs = OpenCms.getScheduleManager().getJobs();
            m_versionHistoryEnabled = OpenCms.getSystemInfo().isVersionHistoryEnabled();
            m_versionHistoryMaxCount = OpenCms.getSystemInfo().getVersionHistoryMaxCount();
            // m_resourceInitHandlers instance must be the one from configuration
            // m_requestHandlers instance must be the one from configuration
        }
        
        // i18n nodes
        Element i18nElement = systemElement.addElement(N_I18N);
        i18nElement.addElement(N_LOCALEHANDLER).addAttribute(A_CLASS, m_localeManager.getLocaleHandler().getClass().getName());
        Iterator i;
        Element localesElement;
        localesElement = i18nElement.addElement(N_LOCALESCONFIGURED);
        i = m_localeManager.getAvailableLocales().iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            localesElement.addElement(N_LOCALE).addText(locale.toString());
        }
        localesElement = i18nElement.addElement(N_LOCALESDEFAULT);
        i = m_localeManager.getDefaultLocales().iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            localesElement.addElement(N_LOCALE).setText(locale.toString());
        }
        
        // mail nodes
        Element mailElement = systemElement.addElement(N_MAIL);
        mailElement.addElement(N_MAILFROM).setText(m_mailSettings.getMailFromDefault());
        i = m_mailSettings.getMailHosts().iterator();
        while (i.hasNext()) {
            CmsMailHost host = (CmsMailHost)i.next();
            Element hostElement = mailElement.addElement(N_MAILHOST)
                .addAttribute(A_NAME, host.getHostname())
                .addAttribute(A_ORDER, host.getOrder().toString())
                .addAttribute(A_PROTOCOL, host.getProtocol());
            if (host.isAuthenticating()) {
                hostElement
                    .addAttribute(A_USER, host.getUsername())
                    .addAttribute(A_PASSWORD, host.getPassword());
            }
        }
        
        // scheduler node
        Element schedulerElement = systemElement.addElement(N_SCHEDULER);
        i = m_configuredJobs.iterator();
        while (i.hasNext()) {
            CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)i.next();
            Element jobElement = schedulerElement.addElement(N_JOB);
            jobElement.addElement(N_NAME).addText(jobInfo.getJobName());
            jobElement.addElement(N_CLASS).addText(jobInfo.getClassName());
            jobElement.addElement(N_REUSEINSTANCE).addText(String.valueOf(jobInfo.isReuseInstance()));
            jobElement.addElement(N_CRONEXPRESSION).addCDATA(jobInfo.getCronExpression());
            Element contextElement = jobElement.addElement(N_CONTEXT);
            contextElement.addElement(N_USERNAME).setText(jobInfo.getContextInfo().getUserName());
            contextElement.addElement(N_PROJECT).setText(jobInfo.getContextInfo().getProjectName());
            contextElement.addElement(N_SITEROOT).setText(jobInfo.getContextInfo().getSiteRoot());
            contextElement.addElement(N_REQUESTEDURI).setText(jobInfo.getContextInfo().getRequestedUri());
            contextElement.addElement(N_LOCALE).setText(jobInfo.getContextInfo().getLocaleName());
            contextElement.addElement(N_ENCODING).setText(jobInfo.getContextInfo().getEncoding());
            contextElement.addElement(N_REMOTEADDR).setText(jobInfo.getContextInfo().getRemoteAddr());
            Element parameterElement = jobElement.addElement(N_PARAMETERS);
            ExtendedProperties jobParameters = jobInfo.getConfiguration();
            if (jobParameters != null) {
                Iterator it = jobParameters.getKeys();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    String value = jobParameters.get(name).toString();
                    Element paramNode = parameterElement.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }
                
        // version history
        systemElement.addElement(N_VERSIONHISTORY)
            .addAttribute(A_ENABLED, new Boolean(m_versionHistoryEnabled).toString())
            .addAttribute(A_COUNT, new Integer(m_versionHistoryMaxCount).toString());       
        
        // resourceinit
        Element resourceinitElement = systemElement.addElement(N_RESOURCEINIT);        
        i = m_resourceInitHandlers.iterator();
        while (i.hasNext()) {
            I_CmsResourceInit clazz = (I_CmsResourceInit)i.next();
            Element handlerElement = resourceinitElement.addElement(N_RESOURCEINITHANDLER);
            handlerElement.addAttribute(A_CLASS, clazz.getClass().getName());            
        }
        
        // request handlers
        Element requesthandlersElement = systemElement.addElement(N_REQUESTHANDLERS);        
        i = m_requestHandlers.iterator();
        while (i.hasNext()) {
            I_CmsRequestHandler clazz = (I_CmsRequestHandler)i.next();
            Element handlerElement = requesthandlersElement.addElement(N_REQUESTHANDLER);
            handlerElement.addAttribute(A_CLASS, clazz.getClass().getName());            
        }
        
        // return the vfs node
        return systemElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {
        return C_CONFIGURATION_DTD_NAME;
    }
    
    /**
     * Returns the configured locale manager for multi language support.<p>
     * 
     * @return the configured locale manager for multi language support
     */
    public CmsLocaleManager getLocaleManager() {
        return m_localeManager;
    }
    
    /**
     * Returns the configured mail settings.<p>
     * 
     * @return the configured mail settings
     */
    public CmsMailSettings getMailSettings() {
        return m_mailSettings;
    }
    
    /**
     * Returns the list of instanciated request handler classes.<p>
     * 
     * @return the list of instanciated request handler classes
     */
    public List getRequestHandlers() {
        return m_requestHandlers;
    }    
    
    /**
     * Returns the list of instanciated resource init handler classes.<p>
     * 
     * @return the list of instanciated resource init handler classes
     */
    public List getResourceInitHandlers() {
        return m_resourceInitHandlers;
    }
    
    /**
     * Returns the configured schedule manager.<p>
     *
     * @return the configured schedule manager
     */
    public CmsScheduleManager getScheduleManager() {

        return m_scheduleManager;
    }
    
    /**
     * Returns temporary file project id.<p>
     * 
     * @return temporary file project id
     */
    public int getTempFileProjectId() {
        return m_tempFileProjectId;
    }

    
    /**
     * Returns the maximum number of versions that are kept per file in the VFS version history.<p>
     * 
     * If the versin history is disabled, this setting has no effect.<p>
     * 
     * @return the maximum number of versions that are kept per file
     * @see #isVersionHistoryEnabled()
     */
    public int getVersionHistoryMaxCount() {
        return m_versionHistoryMaxCount;
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". System configuration : finished");
        }            
    }   
    
    /**
     * Returns if the VFS version history is enabled.<p> 
     * 
     * @return if the VFS version history is enabled
     */
    public boolean isVersionHistoryEnabled() {
        return m_versionHistoryEnabled;
    }    
    
    /**
     * Sets the locale manager for multi language support.<p>
     * 
     * @param localeManager the locale manager to set
     */
    public void setLocaleManager(CmsLocaleManager localeManager) {
        m_localeManager = localeManager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : finished");
        }                    
    }
    
    /**
     * Sets the mail settings.<p>
     * 
     * @param mailSettings the mail settings to set.
     */
    public void setMailSettings(CmsMailSettings mailSettings) {
        m_mailSettings = mailSettings;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Mail settings set " + m_mailSettings);
        }          
    }
    
    /**
     * Sets the temporary file project id.<p>
     * 
     * @param tempFileProjectId the temporary file project id to set
     */
    public void setTempFileProjectId(String tempFileProjectId) {
        try {
            m_tempFileProjectId = Integer.valueOf(tempFileProjectId).intValue();
        } catch (Throwable t) {
            m_tempFileProjectId = -1;
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". System configuration : temporary file project id is " + m_tempFileProjectId);
        }             
    }
        
    /**
     * VFS version history settings are set here.<p>
     * 
     * @param historyEnabled if true the history is enabled
     * @param historyMaxCount the maximum number of versions that are kept per VFS resource
     */
    public void setVersionHistorySettings(String historyEnabled, String historyMaxCount) {
        m_versionHistoryEnabled = Boolean.valueOf(historyEnabled).booleanValue();
        m_versionHistoryMaxCount = Integer.valueOf(historyMaxCount).intValue();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". History settings     : enabled=" + m_versionHistoryEnabled + " count=" + m_versionHistoryMaxCount);
        }             
    }
}
