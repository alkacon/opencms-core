/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskHead.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace task head screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.34 $ $Date: 2005/02/18 14:23:16 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsTaskHead extends CmsWorkplaceDefault {
       
    /** Constant for filter */
    private static final String C_SPACER = "------------------------------------------------";
    
    /**
     * User method to get the checked value.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @throws CmsException
     */
    
    public Object checked(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Object allProjects = session.getValue(C_SESSION_TASK_ALLPROJECTS);
        
        // was the allprojects checkbox checked?
        if((allProjects != null) && (((Boolean)allProjects).booleanValue())) {
            return "checked";
        }
        else {
            return "";
        }
    }
    
    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, 
            String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        
        // is this the first-time, this page is viewed?
        if(session.getValue(C_SESSION_TASK_ALLPROJECTS) == null) {
            
            // YES! read the relevant userproperties
            CmsUserSettings settings = new CmsUserSettings(cms.getRequestContext().currentUser());
            String startupFilter = settings.getTaskStartupFilter();
            boolean allProjects = settings.getTaskShowAllProjects();
                
            // the tasksettings exists - use them
            session.putValue(C_SESSION_TASK_ALLPROJECTS, new Boolean(allProjects));
            session.putValue(C_SESSION_TASK_FILTER, startupFilter);
       
        }
        
        // is this the result of a submit?
        if(parameters.get("filter") != null) {
            
            // YES: get the checkbox-value
            if("OK".equals(parameters.get("ALL"))) {
                session.putValue(C_SESSION_TASK_ALLPROJECTS, new Boolean(true));
            }
            else {
                session.putValue(C_SESSION_TASK_ALLPROJECTS, new Boolean(false));
            }
        }
        
        // is the listbox chosen?
        if((parameters.get("filter") != null) && (!(parameters.get("filter").equals("-")))) {
            session.putValue(C_SESSION_TASK_FILTER, parameters.get("filter"));
        }
        
        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
    
    /**
     * Gets all filters available in the task screen.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @throws CmsException
     */
    
    public Integer getFilters(CmsObject cms, CmsXmlLanguageFile lang, Vector values, Vector names, 
            Hashtable parameters) throws CmsException {
        
        // Let's see if we have a session
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String filter = (String)session.getValue(C_SESSION_TASK_FILTER);
        int selected = 0;
        names.addElement("a1");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a1"));
        if("a1".equals(filter)) {
            selected = 0;
        }
        names.addElement("b1");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b1"));
        if("b1".equals(filter)) {
            selected = 1;
        }
        names.addElement("c1");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c1"));
        if("c1".equals(filter)) {
            selected = 2;
        }
        names.addElement("-");
        values.addElement(C_SPACER);
        names.addElement("a2");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a2"));
        if("a2".equals(filter)) {
            selected = 4;
        }
        names.addElement("b2");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b2"));
        if("b2".equals(filter)) {
            selected = 5;
        }
        names.addElement("c2");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c2"));
        if("c2".equals(filter)) {
            selected = 6;
        }
        names.addElement("-");
        values.addElement(C_SPACER);
        names.addElement("a3");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a3"));
        if("a3".equals(filter)) {
            selected = 8;
        }
        names.addElement("b3");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b3"));
        if("b3".equals(filter)) {
            selected = 9;
        }
        names.addElement("c3");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c3"));
        if("c3".equals(filter)) {
            selected = 10;
        }
        names.addElement("-");
        values.addElement(C_SPACER);
        names.addElement("d1");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d1"));
        if("d1".equals(filter)) {
            selected = 12;
        }
        names.addElement("d2");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d2"));
        if("d2".equals(filter)) {
            selected = 13;
        }
        names.addElement("d3");
        values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d3"));
        if("d3".equals(filter)) {
            selected = 14;
        }
        return (new Integer(selected));
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
}
