/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplace.java,v $
 * Date   : $Date: 2003/06/25 16:12:50 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.OpenCms;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.LinkSubstitution;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 * 
 * @since 5.1
 */
public abstract class CmsWorkplace {
    
    protected static final String C_SESSION_WORKPLACE_SETTINGS = "__org.opencms.workplace.CmsWorkplaceSettings";
    
    private CmsJspActionElement m_jsp;
    private CmsObject m_cms;
    private HttpSession m_session;
    private CmsWorkplaceSettings m_settings;
    private String m_resourceUri = null;
        
    /**
     * Public constructor.<p>
     * 
     * @param jsp the initialized JSP context
     */    
    public CmsWorkplace(CmsJspActionElement jsp) {
        m_jsp = jsp;        
        m_cms = m_jsp.getCmsObject();                
        m_session = m_jsp.getRequest().getSession();
        
        // get / create the workplace settings 
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(C_SESSION_WORKPLACE_SETTINGS);
        if (m_settings == null) {
            // create the settings object
            m_settings = new CmsWorkplaceSettings();
            initWorkplaceSettings(m_cms, m_settings);
            storeSettings(m_session, m_settings);
        }
        
        // check request for changes in the workplace settings
        initWorkplaceRequestValues(m_settings, m_jsp.getRequest());        
        
        // set cms context accordingly
        initWorkplaceCmsContext(m_settings, m_cms);
    }    
    
    /**
     * Get a localized key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized key value
     */
    public String key(String keyName) {
        return m_settings.getMessages().key(keyName);
    } 
    
    /**
     * Returns the current workplace encoding.<p>
     * 
     * @return the current workplace encoding
     */
    public String getEncoding() {
        return  m_settings.getMessages().getEncoding();
    }
    
    /**
     * Stores the settings in the given session.<p>
     * 
     * @param session the session to store the settings in
     * @param settings the settings
     */
    static synchronized void storeSettings(HttpSession session, CmsWorkplaceSettings settings) {
        // save the workplace settings in the session
        session.setAttribute(C_SESSION_WORKPLACE_SETTINGS, settings);        
    }
    
    /**
     * Initializes the current users workplace settings by reading the values 
     * from the users preferences.<p>
     * 
     * This method is synchronized to ensure that the settings are
     * initialized only once for a user.
     * 
     * @param cms the cms object for the current user
     * @param settings the current workplace settings
     * @return initialized object with the current users workplace settings 
     */    
    static synchronized CmsWorkplaceSettings initWorkplaceSettings(CmsObject cms, CmsWorkplaceSettings settings) {                
        // initialize the current user language
        String language = null;               
        Hashtable startSettings =
            (Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);  
        // try to read it form the user additional info
        if (startSettings != null) {
            language = (String)startSettings.get(I_CmsConstants.C_START_LANGUAGE);
        }    
        // no startup language in user settings found, so check the users browser locale settings
        if (language == null) {
            Vector languages = (Vector)cms.getRequestContext().getAcceptedLanguages();
            int numlangs = languages.size();
            for (int i = 0; i < numlangs; i++) {
                String lang = (String)languages.elementAt(i);
                try {
                    cms.readFolder(I_CmsWpConstants.C_VFS_PATH_LOCALES + lang);
                    // if we get past that readFolder() the language is supported
                    language = lang;
                    break;
                } catch (CmsException e) {
                    // browser language is not supported in OpenCms, continue looking
                }
            }
        }
        // if no language was found so far, use the default language
        if (language == null) {
            language = I_CmsWpConstants.C_DEFAULT_LANGUAGE;
        }
        
        // save language in settings
        settings.setLanguage(language);        
        
        // initialize messages and also store them in settings
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(cms, language);
        settings.setMessages(messages);        
        
        // save current workplace user
        settings.setUser(cms.getRequestContext().currentUser());

        // save current default group
        settings.setGroup(cms.getRequestContext().currentGroup().getName());        
        
        // save current project
        settings.setProject(cms.getRequestContext().currentProject().getId());
        
        // check out the user infor1ation if a default view is stored there
        if (startSettings != null) {
            settings.setCurrentView(LinkSubstitution.getLinkSubstitution(cms, (String)startSettings.get(I_CmsConstants.C_START_VIEW)));
        }
                  
        return settings;   
    }
    
    /**
     * Analyzes the request for workplace parameters and adjusts the workplace
     * settings accordingly.<p> 
     * 
     * @param settings the workplace settings
     * @param request the current request
     */
    protected abstract void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request);
        
    /**
     * Sets the cms request context and other cms related settings to the 
     * values stored int the workplace settings.<p>
     * 
     * @param settings the workplace settings
     * @param cms the current cms object
     */
    private void initWorkplaceCmsContext(CmsWorkplaceSettings settings, CmsObject cms) {

        CmsRequestContext reqCont = cms.getRequestContext();

        // check project setting        
        if (settings.getProject() != reqCont.currentProject().getId()) {
            try {                
                reqCont.setCurrentProject(settings.getProject());
            } catch (CmsException e) {
                // do nothing
            }                    
        }

        // check group setting
        if (!(settings.getGroup().equals(reqCont.currentGroup().getName()))) {
            try {
                reqCont.setCurrentGroup(settings.getGroup());
            } catch (CmsException e) {
                // do nothing
            }
        }
    }
    
    /**
     * Returns the initialized cms object for the current user.<p>
     * 
     * @return the initialized cms object for the current user
     */
    public CmsObject getCms() {
        return m_cms;
    }

    /**
     * Returns the JSP action element.<p>
     * 
     * @return the JSP action element
     */
    public CmsJspActionElement getJsp() {
        return m_jsp;
    }

    /**
     * Returns the current user http session.<p>
     * 
     * @return the current user http session
     */
    public HttpSession getSession() {
        return m_session;
    }

    /**
     * Returns the current users workplace settings.<p>
     * 
     * @return the current users workplace settings
     */
    public CmsWorkplaceSettings getSettings() {
        return m_settings;
    }
    
    /**
     * Returns the path to the workplace static resources.<p>
     * 
     * Workplaces static resources are images, css files etc.
     * These are exported during the installation of OpenCms,
     * and are usually only read from this exported location to 
     * avoid the overhaead of accessing the database later.<p> 
     * 
     * @return the path to the workplace static resources
     */
    public String getResourceUri() {
        if (m_resourceUri != null) return m_resourceUri;
        synchronized(this) {
            boolean useVfs = true;
            // check registry for setting of workplace images
            try {
                useVfs = (new Boolean(OpenCms.getRegistry().getSystemValue("UseWpPicturesFromVFS"))).booleanValue();
            } catch (CmsException e) {
                // by default (useVfs == true) we assume that we want to use exported resources
            }            
            if (useVfs) {
                m_resourceUri = m_cms.getRequestContext().getRequest().getServletUrl() + I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS;
            } else {
                m_resourceUri = m_cms.getRequestContext().getRequest().getWebAppUrl() + I_CmsWpConstants.C_SYSTEM_PICS_EXPORT_PATH;
            }            
        }
        return m_resourceUri;
    }
    
    /**
     * Returns the path to the currently selected skin.<p>
     * 
     * @return the path to the currently selected skin
     */
    public String getSkinUri() {
        return m_cms.getRequestContext().getRequest().getWebAppUrl() + "/skins/modern/";        
    }    
    
    /**
     * Generates a html select box out of the provided values.<p>
     * 
     * @param parameters a string that will be inserted into the initial select tag,
     *      if null no parameters will be inserted
     * @param options the options 
     * @param values the option values, if null the select will have no value attributes
     * @param selected the index of the pre-selected option, if -1 no option is pre-selected
     * @return a String representing a html select box
     */
    public String buildSelect(String parameters, List options, List values, int selected) {
        StringBuffer result = new StringBuffer(1024);
        result.append("<select ");
        if (parameters != null) result.append(parameters);
        result.append(">\n");
        int length = options.size();
        String value = null;
        for (int i=0; i<length; i++) {
            if (values != null) {
                try {
                    value = (String)values.get(i);
                } catch (Exception e) {
                    // lists are not properly initialized, just don't use the value
                    value = null;
                }                
            }
            if (value == null) {
                result.append("<option");
                if (i == selected) result.append(" selected");   
                result.append(">");  
                result.append(options.get(i));
                result.append("</option>\n");
            } else {
                result.append("<option value=\"");
                result.append(value);
                result.append("\""); 
                if (i == selected) result.append(" selected");
                result.append(">");                
                result.append(options.get(i));
                result.append("</option>\n");                
            }       
        }        
        result.append("</select>\n");                
        return result.toString();
    }

}
