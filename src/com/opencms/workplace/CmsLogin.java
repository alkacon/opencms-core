/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsLogin.java,v $
 * Date   : $Date: 2000/05/18 13:39:47 $
 * Version: $Revision: 1.28 $
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
 * @author Waruschan Babachan
 * @version $Revision: 1.28 $ $Date: 2000/05/18 13:39:47 $
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
		
		CmsXmlWpConfigFile configFile=new CmsXmlWpConfigFile(cms);
		String actionPath=configFile.getWorkplaceActionPath();
		
		String startTaskId = (String)parameters.get(C_PARA_STARTTASKID);
		String startProjectId = (String)parameters.get(C_PARA_STARTPROJECTID);		
		if (startTaskId == null) {
			startTaskId = "";
		}
		if (startProjectId == null) {
			startProjectId = "";
		}				
		if (!startProjectId.equals("")) {
			session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
			session.putValue(C_PARA_STARTPROJECTID,startProjectId);
		}		
		// check if this is a link of a task
		if (!startTaskId.equals("")) {
			session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
			session.putValue(C_PARA_STARTTASKID,startTaskId);
			Vector viewNames = new Vector();
			Vector viewLinks = new Vector();
			configFile.getWorkplaceIniData(viewNames, viewLinks,"WORKPLACEVIEWS","VIEW");
			String link="";
			for (int i=0;i<viewNames.size();i++) {
				if (((String)viewNames.elementAt(i)).equals("tasks")) {
					link=(String)viewLinks.elementAt(i);
					break;
				}
			}
			session.putValue(C_PARA_VIEW,link);		
		}	
		
		// Indicates, if this is a request of a guest user.			
		if (!cms.anonymousUser().equals(cms.getRequestContext().currentUser()) && (!startTaskId.equals(""))) {
			// set current project to a default or to a specified project
			Integer currentProject=null;
			session.removeValue(C_PARA_STARTPROJECTID);
			if (!startProjectId.equals("")) {
				currentProject = new Integer(startProjectId);
				boolean access=true;
				try {
					access=cms.accessProject(currentProject.intValue());
				} catch (Exception e) {
					access=false;
				}
				if (!access) {
					// check out the user information if a default project is stored there.
					Hashtable startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
					if (startSettings != null) {
						currentProject = (Integer)startSettings.get(C_START_PROJECT);
					}
				}
			} else {
				// check out the user information if a default project is stored there.
				Hashtable startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
				if (startSettings != null) {
					currentProject = (Integer)startSettings.get(C_START_PROJECT);
				}
			}					
			try {
				if (cms.accessProject(currentProject.intValue())) {
					cms.getRequestContext().setCurrentProject(currentProject.intValue());
				}
			} catch (Exception e) {
			}
			
			try {
				cms.getRequestContext().getResponse().sendCmsRedirect(actionPath+"index.html");
				// return "".getBytes();
                return null;
			} catch (Exception e) {
				throw new CmsException(e.getMessage());	
			}
			
		}
		
		// the template to be displayed		
        String template=templateSelector;
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
				
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
					xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
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
               	
				// set current project to a default or to a specified project
				Integer currentProject=null;
				startProjectId=(String)session.getValue(C_PARA_STARTPROJECTID);
				if (startProjectId!=null && (!startProjectId.equals(""))) {
					currentProject = new Integer(startProjectId);
					session.removeValue(C_PARA_STARTPROJECTID);
					boolean access=true;
					try {
						access=cms.accessProject(currentProject.intValue());
					} catch (Exception e) {
						access=false;
					}
					if (!access) {
						// check out the user information if a default project is stored there.
						Hashtable startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
						if (startSettings != null) {
							currentProject = (Integer)startSettings.get(C_START_PROJECT);
						}
					}
					
				} else {
					// check out the user information if a default project is stored there.
					Hashtable startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
					if (startSettings != null) {
						currentProject = (Integer)startSettings.get(C_START_PROJECT);
					}
				}
				try {
					if (cms.accessProject(currentProject.intValue())) {
						cms.getRequestContext().setCurrentProject(currentProject.intValue());
					}
				} catch (Exception e) {
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
				String projectid=(String)session.getValue(C_PARA_STARTPROJECTID);
				String taskid=(String)session.getValue(C_PARA_STARTTASKID);
				String view=(String)session.getValue(C_PARA_VIEW);
				if (projectid==null) {
					projectid="";
				}
				if (taskid==null) {				
					taskid="";
					view="";
				}
				//session.invalidate();
				session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
				if (!projectid.equals("")) {
					session.putValue(C_PARA_STARTPROJECTID,projectid);
				}
				if (!taskid.equals("")) {				
					session.putValue(C_PARA_STARTTASKID,taskid);
					session.putValue(C_PARA_VIEW,view);
				}
            }
        }
        
        // this is the first time the dockument is selected, so reade the page forwarding
        if (username == null) {
            xmlTemplateDocument.clearStartup();
        } else {
            long id=System.currentTimeMillis();
            xmlTemplateDocument.setData("ID",new Long(id).toString());
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
         filelist=4095 + 512;
        pref.put(C_USERPREF_FILELIST,new Integer(filelist));
        return pref;
    }
}
