/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplace.java,v $
 * Date   : $Date: 2003/06/06 16:47:10 $
 * Version: $Revision: 1.1 $
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
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsWorkplace {
    
    protected static final String C_SESSION_WORKPLACE_SESSTINGS = "__org.opencms.workplace.CmsWorkplaceSesstings";
    protected static final String C_WORKPLACE_LOCALES_BUNDLE = "com.opencms.workplace.workplace";
    
    private CmsJspActionElement m_jsp;
    private CmsObject m_cms;
    private HttpSession m_session;
    private CmsWorkplaceSettings m_settings;
    private CmsWorkplaceMessages m_messages;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp the initialized JSP context
     */
    public CmsWorkplace(CmsJspActionElement jsp) {
        m_jsp = jsp;        
        m_cms = m_jsp.getCmsObject();
        
        m_session = m_jsp.getRequest().getSession();
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(C_SESSION_WORKPLACE_SESSTINGS);
        if (m_settings == null) {
            initWorkplaceSettings(m_cms);
        }
        CmsRequestContext reqCont = m_cms.getRequestContext();
        
        // check if the user requested a group change
        String newGroup = (String)m_jsp.getRequest().getParameter("wpGroup");
        if (newGroup != null) {
            if (!(newGroup.equals(reqCont.currentGroup().getName()))) {
                try {
                    reqCont.setCurrentGroup(newGroup);
                } catch (CmsException e) {
                    // do nothing
                }
            }
        }

        // check if the user requested a project change
        String newProject = (String)m_jsp.getRequest().getParameter("wpProject");
        if (newProject != null) {
            if (!(Integer.parseInt(newProject) == reqCont.currentProject().getId())) {
                try {                
                    reqCont.setCurrentProject(Integer.parseInt(newProject));
                } catch (CmsException e) {
                    // do nothing
                }                    
            }
        }

        // check if the user requested a view change
        String newView = (String)m_jsp.getRequest().getParameter("wpView");
        if (newView != null) {
            System.err.println("view: " + newView);
            m_session.setAttribute(I_CmsWpConstants.C_PARA_VIEW, newView);
        }
                
        m_messages = m_settings.getMessages();
    }    
    
    /**
     * Get a localized key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized key value
     */
    public String key(String keyName) {
        return m_messages.key(keyName);
    } 
    
    /**
     * Returns the current workplace encoding.<p>
     * 
     * @return the current workplace encoding
     */
    public String getEncoding() {
        return m_messages.getEncoding();
    }
    
    /**
     * Initializes the current users workplace settnings.<p>
     * 
     * @param cms the cms object for the current user
     */    
    private void initWorkplaceSettings(CmsObject cms) {        
        // create the settings object
        CmsWorkplaceSettings settings = new CmsWorkplaceSettings();
        
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
        // store language in workplace settings
        settings.setLanguage(language);
        
        // initialize messages and also store them in settings
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(cms, language);
        settings.setMessages(messages);
        
        // save the workplace settings in the session
        m_session.setAttribute(C_SESSION_WORKPLACE_SESSTINGS, settings);     
        m_settings = settings;   
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
     * Returns the current workplace message object.<p>
     * 
     * @return the current workplace message object
     */
    public CmsWorkplaceMessages getMessages() {
        return m_messages;
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
