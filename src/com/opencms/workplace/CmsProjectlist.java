/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsProjectlist.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.34 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.util.CmsDateUtil;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.34 $ $Date: 2005/02/18 14:23:15 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsProjectlist extends A_CmsWpElement {
    
    
    /**
     * Javascriptmethod, to call for contextlink
     */
    private static final String C_PROJECT_LOCK = "project_lock";
    
    
    /**
     * Javascriptmethod, to call for contextlink
     */
    private static final String C_PROJECT_UNLOCK = "project_unlock";
    
    /**
     * Handling of the special workplace <CODE>&lt;PROJECTLIST&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * <CODE>&lt;PROJECTLIST /&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, 
            Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Read projectlist parameters
        String listMethod = n.getAttribute(C_PROJECTLIST_METHOD);
        
        // Get list definition and language values
        CmsXmlWpTemplateFile listdef = getProjectlistDefinitions(cms);
        
        // call the method for generating projectlist elements
        Method callingMethod = null;
        Vector list = new Vector();
        try {
            callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {
                CmsObject.class, CmsXmlLanguageFile.class
            });
            list = (Vector)callingMethod.invoke(callingObject, new Object[] {
                cms, lang
            });
        }
        catch(NoSuchMethodException exc) {
            
            // The requested method was not found.
            throwException("Could not find method " + listMethod + " in calling class " 
                    + callingObject.getClass().getName() + " for generating projectlist content.", 
                    CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {
            
            // the method could be invoked, but throwed a exception            
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                
                // Only print an error if this is NO CmsException
                throwException("User method " + listMethod + " in calling class " 
                        + callingObject.getClass().getName() + " throwed an exception. " 
                        + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {
                
                // This is a CmsException                
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("User method " + listMethod + " in calling class " 
                    + callingObject.getClass().getName() + " was found but could not be invoked. " 
                    + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        String state = C_PROJECTLIST_STATE_UNLOCKED;
        String snaplock = listdef.getProcessedDataValue(C_TAG_PROJECTLIST_SNAPLOCK, callingObject, parameters);
        for(int i = 0;i < list.size();i++) {
            
            // get the actual project
            CmsProject project = (CmsProject)list.elementAt(i);
            
            // get the correckt state
            if(cms.countLockedResources(project.getId()) == 0) {
                state = C_PROJECTLIST_STATE_UNLOCKED;
            }
            else {
                state = C_PROJECTLIST_STATE_LOCKED;
            }
            
            // get the processed list.
            setListEntryData(cms, lang, listdef, project);
            if(state.equals(C_PROJECTLIST_STATE_UNLOCKED)) {
                listdef.setData(C_PROJECTLIST_LOCKSTATE, "");
                listdef.setData(C_PROJECTLIST_MENU, C_PROJECT_UNLOCK);
            }
            else {
                listdef.setData(C_PROJECTLIST_LOCKSTATE, snaplock);
                listdef.setData(C_PROJECTLIST_MENU, C_PROJECT_LOCK);
            }
            listdef.setData(C_PROJECTLIST_IDX, new Integer(i).toString());
            result.append(listdef.getProcessedDataValue(C_TAG_PROJECTLIST_DEFAULT, callingObject, 
                    parameters));
        }
        return result.toString();
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Method to set details about a project into xml-datas.
     * @param cms The cms-object.
     * @param lang The language-file.
     * @param xmlFile The file to set the xml-data into.
     * @param project The project to get the data from.
     * @throws CmsException is thrown if something goes wrong.
     */
    
    public static void setListEntryData(CmsObject cms, CmsXmlLanguageFile lang, 
            CmsXmlWpTemplateFile xmlFile, CmsProject project) throws CmsException {
        String state;
        
        // get the correckt state
        if(cms.countLockedResources(project.getId()) == 0) {
            state = C_PROJECTLIST_STATE_UNLOCKED;
        }
        else {
            state = C_PROJECTLIST_STATE_LOCKED;
        }
        xmlFile.setData(C_PROJECTLIST_NAME, project.getName());
        xmlFile.setData(C_PROJECTLIST_NAME_ESCAPED, CmsEncoder.escape(project.getName(),
            cms.getRequestContext().getEncoding()));
        xmlFile.setData(C_PROJECTLIST_PROJECTID, project.getId() + "");
        xmlFile.setData(C_PROJECTLIST_DESCRIPTION, project.getDescription());
        xmlFile.setData(C_PROJECTLIST_STATE, lang.getLanguageValue(state));
        xmlFile.setData(C_PROJECTLIST_PROJECTMANAGER, cms.readManagerGroup(project).getName());
        xmlFile.setData(C_PROJECTLIST_PROJECTWORKER, cms.readGroup(project).getName());
        xmlFile.setData(C_PROJECTLIST_DATECREATED, CmsDateUtil.getDateTimeShort(project.getCreateDate()));
        xmlFile.setData(C_PROJECTLIST_OWNER, cms.readOwner(project).getName());
    }
}
