/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsLogin.java,v $
 * Date   : $Date: 2000/03/16 19:26:44 $
 * Version: $Revision: 1.14 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the login screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.14 $ $Date: 2000/03/16 19:26:44 $
 */
public class CmsLogin extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Overwrtied the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the longin templated and processed the data input.
     * If the user has authentificated to the system, the login window is closed and
     * the workplace is opened. <br>
     * If the login was incorrect, an error message is displayed and the login
     * dialog is displayed again.
     * @param cms The CmsObject.
     * @param templateFile The login template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        String username=null;
        HttpSession session=null;
        A_CmsUser user;
        // the template to be displayed
        String template="template";
        Hashtable preferences=new Hashtable();
        // get user name and password
        String name=(String)parameters.get("NAME");
        String password=(String)parameters.get("PASSWORD");
        // try to read this user
        if ((name != null) && (password != null)){
            try {
                username=cms.loginUser(name,password);
            } catch (CmsException e) {
              if (e.getType()==CmsException.C_NO_ACCESS) {
                    // there was an authentification error during login
                    // set user to null and switch to error template
                    username=null;     
                    template="error";
                } else {
                    throw e;
                }   
            }   
            // check if a user was found.
            if (username!= null) {
                // get a session for this user so that he is authentificated at the
                // end of this request
                session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsLogin] Login user " + username);
                }
                // now get the users preferences
                user=cms.readUser(username);
                
                String currentProject;
                Hashtable startSettings=null;
        
                // check out the user information if a default project is stored there.
                startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
                if (startSettings != null) {
                    currentProject = (String)startSettings.get(C_START_PROJECT);
                    try {
                        cms.getRequestContext().setCurrentProject(currentProject);
                    } catch (Exception e) {
                    }
                }              
                
                preferences=(Hashtable)user.getAdditionalInfo(C_ADDITIONAL_INFO_PREFERENCES);
                // check if preferences are existing, otherwiese use defaults
                if (preferences == null) {
                    preferences=getDefaultPreferences();
                }
                session.putValue(C_ADDITIONAL_INFO_PREFERENCES,preferences);
            }
        } else {
            // This is a new login!
            // If there is an old session, remove all user variables from this session
            session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(false);
            if(session != null) {
                String[] valueNames = session.getValueNames();
                int numValues = valueNames.length;
                for(int i=0; i<numValues; i++) {
                    session.removeValue(valueNames[i]);
                }
            }
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        // this is the first time the dockument is selected, so reade the page forwarding
        if (username == null) {
            xmlTemplateDocument.clearStartup();
        } else {
            xmlTemplateDocument.setXmlData("ID",session.getId().replace('.','_'));
        }
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    
    }
    
    /**
     * Sets the default preferences for the current user if those values are not available.
     * @return Hashtable with default preferences.
     */
    private Hashtable getDefaultPreferences() {
        Hashtable pref=new Hashtable();
        
        // set the default columns in the filelist
        int filelist=C_FILELIST_TITLE+C_FILELIST_TYPE+C_FILELIST_CHANGED;
        // HACK
         filelist=4095;
        pref.put(C_USERPREF_FILELIST,new Integer(filelist));
        return pref;
    }
}
