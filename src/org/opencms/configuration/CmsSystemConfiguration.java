/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsSystemConfiguration.java,v $
 * Date   : $Date: 2004/06/13 23:31:17 $
 * Version: $Revision: 1.2 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * VFS master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsSystemConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** The name of the DTD for this configuration */
    private static final String C_CONFIGURATION_DTD_NAME = "opencms-system.dtd";
    
    /** The name of the default XML file for this configuration */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms-system.xml";    
    
    /** The node name for the internationalization node */
    protected static final String N_I18N = "internationalization";
    
    /** The node name for individual locales */
    protected static final String N_LOCALE = "locale";
    
    /** The node name for the locale handler */
    protected static final String N_LOCALEHANDLER = "localehandler";
    
    /** The node name for the configured locales */
    protected static final String N_LOCALESCONFIGURED = "localesconfigured";
    
    /** The node name for the default locale(s)*/
    protected static final String N_LOCALESDEFAULT = "localesdefault";    
    
    /** The node name for the mail configuration */
    protected static final String N_MAIL = "mail";
    
    /** The node name for the "mail from" node */
    protected static final String N_MAILFROM = "mailfrom";

    /** The node name for the "mail host" node */
    protected static final String N_MAILHOST = "mailhost";
    
    /** The node name for the resource init classes */
    protected static final String N_RESOURCEINIT = "resourceinit";
    
    /** The node name for the resource init classes */
    protected static final String N_RESOURCEINITHANDLER = "resourceinithandler";
    
    /** The main system configuration node name */
    protected static final String N_SYSTEM = "system";
        
    /** The node name for the version history */
    protected static final String N_VERSIONHISTORY = "versionhistory";
    
    /** The configured locale manager for multi language support */
    private CmsLocaleManager m_localeManager;
    
    /** The mail settings */
    private CmsMailSettings m_mailSettings;
    
    /** A list of instanciated resource init handler classes */
    private List m_resourceInitHandlers;
    
    /** The temporary file project id */
    private int m_tempFileProjectId;
    
    /** Indicates if the version history is enabled */
    private boolean m_versionHistoryEnabled;
    
    /** The maximum number of entries in the version history (per resource) */
    private int m_versionHistoryMaxCount;
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsSystemConfiguration() {
        setXmlFileName(C_DEFAULT_XML_FILE_NAME);
        m_versionHistoryEnabled = true;
        m_versionHistoryMaxCount = 10;
        m_resourceInitHandlers = new ArrayList();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". System configuration : initialized");
        }           
    }
    
    /**
     * Adds a new instance of a resource init handler class.<p>
     * 
     * @param clazz the class name of the resource init handler to instanciate and add
     */
    public void addResourceInitHandler(String clazz) {
        // initialize "resourceinit" registry classes
        try {
            m_resourceInitHandlers.add(Class.forName(clazz).newInstance());
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Resource init class  : " + clazz + " instanciated");
            }
        } catch (Throwable t) {
            OpenCms.getLog(this).error(". Resource init class '" + clazz  + "' could not be instanciated", t);
        }
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
        
        digester.addObjectCreate("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_SYSTEM + "/" + N_I18N + "/" + N_LOCALEHANDLER, "setLocaleHandler");

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
        
        // add mail host configuration rul
        digester.addCallMethod("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, "addMailHost", 5);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 0, A_NAME);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 1, A_ORDER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 2, A_PROTOCOL);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 3, A_USER);
        digester.addCallParam("*/" + N_SYSTEM + "/" + N_MAIL + "/" + N_MAILHOST, 4, A_PASSWORD);
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate vfs node and subnodes
        Element systemElement = parent.addElement(N_SYSTEM);        
        
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
            CmsMailSettings.CmsMailHost host = (CmsMailSettings.CmsMailHost)i.next();
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
                
        // version history
        systemElement.addElement(N_VERSIONHISTORY)
            .addAttribute(A_ENABLED, new Boolean(m_versionHistoryEnabled).toString())
            .addAttribute(A_COUNT, new Integer(m_versionHistoryMaxCount).toString());       
        
        // resourceinit
        // TODO: Use this
        systemElement.addElement(N_RESOURCEINIT);        
        
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
     * Returns the list of instanciated resource init handler classes.<p>
     * 
     * @return the list of instanciated resource init handler classes
     */
    public List getResourceInitHandlers() {
        return m_resourceInitHandlers;
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
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". System configuration : Temporary file project id is " + m_tempFileProjectId);
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
