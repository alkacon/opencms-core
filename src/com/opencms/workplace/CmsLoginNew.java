/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsLoginNew.java,v $
 * Date   : $Date: 2002/11/02 10:37:08 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2001  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org 
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.workplace;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsUser;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;

/**
 * Template class for displaying the login screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $ 
 */

public class CmsLoginNew extends CmsXmlTemplate implements I_CmsWpConstants,I_CmsConstants {

    /** Debug flag, set to 9 for maximum verbosity */
    private static final int DEBUG = 0;

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

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
                                      
        I_CmsSession session = cms.getRequestContext().getSession(false);
        // Check if there already is a session
        if (session != null) {
            // Old session found, must be invalidated
            session.invalidate();
        }

        // the template to be displayed
        CmsXmlTemplateFile xmlTemplateDocument = new CmsXmlTemplateFile(cms, templateFile);
                
        String username = null;
        CmsUser user;

        // get user name and password
        String name = (String)parameters.get("NAME");
        String password = (String)parameters.get("PASSWORD");

        if (DEBUG > 1) System.err.println("CmsLoginNew: name=" + name + " password=" + password);

        if((name != null) && (password != null)) {

            // user and password have been submitted, try to log in the user        
            boolean validLogin;
            try {
                username = cms.loginUser(name, password);
                validLogin = true;
                if (DEBUG > 1) System.err.println("CmsLoginNew: cms.loginUser() successfull");
            }
            catch(CmsException e) {
                // invalid login
                validLogin = false;
                if (DEBUG > 1) System.err.println("CmsLoginNew: cms.loginUser() failed");
            }

            if((username != null) && (username.equals(cms.C_USER_GUEST))) {
                // please no Guest user in the workplace
                // use the same behaviour as if the access was unauthorized
                validLogin = false;
                if (DEBUG > 1) System.err.println("CmsLoginNew: user was guest user");
            } else if ( (username != null) && 
                (! cms.userInGroup(username, cms.C_GROUP_USERS)) && 
                (! cms.userInGroup(username, cms.C_GROUP_PROJECTLEADER)) && 
                (! cms.userInGroup(username, cms.C_GROUP_ADMIN)) ) {
                // user MUST be in at last one of the default groups "Administrators", "Users" or "Projectmanagers"
                // use the same behaviour as if the access was unauthorized
                validLogin = false;
                if (DEBUG > 1) System.err.println("CmsLoginNew: user was not in default groups");
            }
            
            if (! validLogin) {
                throw new CmsException( "[OpenCms login failed]", CmsException.C_NO_USER);
            }
            if (DEBUG > 0) System.err.println("CmsLoginNew: user " + username + " logged in");

            // get a session for this user so that he is authentificated at the
            // end of this request
            session = cms.getRequestContext().getSession(true);
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsLogin] Login user " + username);
            }

            // now get the users preferences
            user = cms.readUser(username);

            // set current project to the default online project or to 
            // project specified in the users preferences
            int currentProject = cms.onlineProject().getId();
            // check out the user information if a default project is stored there.
            Hashtable startSettings = (Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
            if(startSettings != null) {
                Integer i = (Integer)startSettings.get(C_START_PROJECT);
                if (i != null) currentProject = i.intValue();
            }
            // set the current project
            try {
                if (! cms.accessProject(currentProject)) {
                    // user has no access to the project
                    currentProject = cms.onlineProject().getId();
                }
            }
            catch(Exception e) {
                // project will default to online project
                currentProject = cms.onlineProject().getId();
            }
            cms.getRequestContext().setCurrentProject(currentProject);
            
            // set the additional user preferences
            Hashtable preferences = (Hashtable)user.getAdditionalInfo(C_ADDITIONAL_INFO_PREFERENCES);
            // check if preferences are existing, otherwise use defaults
            if(preferences == null) {
                preferences = getDefaultPreferences();
            }
            session.putValue(C_ADDITIONAL_INFO_PREFERENCES, preferences);
        }

        long id = System.currentTimeMillis();
        xmlTemplateDocument.setData("ID", new Long(id).toString());
        
        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, templateSelector);
    }

    /**
     * Sets the default preferences for the current user if those values are not available.
     * @return Hashtable with default preferences.
     */

    private Hashtable getDefaultPreferences() {
        Hashtable pref = new Hashtable();

        // set the default columns in the filelist
        int filelist = C_FILELIST_TITLE + C_FILELIST_TYPE + C_FILELIST_CHANGED;

        // HACK
        filelist = 4095 + 512;
        pref.put(C_USERPREF_FILELIST, new Integer(filelist));
        return pref;
    }

    /**
     * Customized <code>getTitle()</code> method for adding the current 
     * version information to the title of the login screen.
     *
     * @param cms for accessing system resources
     * @param tagcontent (unused)
     * @param doc reference to the A_CmsXmlContent object of the initiating XML document.
     * @param userObj must ba a <code>java.util.Hashtable</code> with request parameters
     * @return String with customized title information
     * @exception CmsException
     */
    public Object getTitle(CmsObject cms, String tagcontent, A_CmsXmlContent doc,
            Object userObject) throws CmsException {
        String title = (String)super.getTitle(cms, tagcontent, doc, userObject);
        if(title == null) title = "";
        title += " - " + cms.version();
        return title;
    }

    /**
     * Returns a String with the version information of this OpenCms instance
     *
     * @param cms for accessing system resources
     * @param tagcontent (unused)
     * @param doc reference to the A_CmsXmlContent object of the initiating XML document.
     * @param userObj must ba a <code>java.util.Hashtable</code> with request parameters
     * @return String with the version information of this OpenCms instance
     * @exception CmsException in case of errors processing the template
     */
    public Object version(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        return cms.version();
    }

    /**
     * Prevent caching of this template
     *
     * @param cms for accessing system resources
     * @param templateFile filename of the template file
     * @param elementName element name of this template in our parent template.
     * @param parameters hash with all template class parameters
     * @param templateSelector template section that should be processed
     * @return false
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Prevent caching of this template in the element cache
     *
     * @param cms for accessing system resources
     * @param templateFile filename of the template file
     * @param elementName element name of this template in our parent template.
     * @param parameters hash with all template class parameters
     * @param templateSelector template section that should be processed
     * @return <code>new CmsCacheDirectives(false)</code>
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return new CmsCacheDirectives(false);
    }    
}
