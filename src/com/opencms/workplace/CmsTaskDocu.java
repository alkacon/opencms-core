/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskDocu.java,v $
* Date   : $Date: 2003/08/30 11:30:08 $
* Version: $Revision: 1.15 $
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

import org.opencms.workflow.CmsTaskLog;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsUser;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Class for building task list documentation. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;TASKDOCU&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2003/08/30 11:30:08 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsTaskDocu extends A_CmsWpElement implements I_CmsWpElement {
    
    /**
     * This private helper method adds a <code>&lt;BR&gt;</code> tag to each line.
     * 
     * @param value The string to add the br-tags.
     * @return The resulting string with br-tags.
     */
    
    private String addBrTags(String value) {
        String lines[] = Utils.split(value, "\n");
        String retValue = "";
        for(int i = 0;i < lines.length;i++) {
            retValue += lines[i] + "<BR>";
        }
        return retValue;
    }
    
    /**
     * Handling of the special workplace <CODE>&lt;TASKDOCU&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * <CODE>&lt;TASKDOCU /&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param An XML element containing the <code>&lt;TASKDOCU&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, 
            Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        Vector taskdocs = new Vector();
        CmsTaskLog tasklog;
        
        // Get the template definition values
        CmsXmlWpTemplateFile template = getTaskDocuDefinitions(cms);
        
        // get the task id
        String taskId = (String)parameters.get("taskid");
        if(taskId != null) {
            int id = new Integer(taskId).intValue();
            taskdocs = cms.readTaskLogs(id);
        }
        
        // go through all tasklogs
        for(int i = 1;i <= taskdocs.size();i++) {
            tasklog = (CmsTaskLog)taskdocs.elementAt(taskdocs.size() - i);
            int type = tasklog.getType();
            
            // check if this is a type to be displayed
            if(type >= 100) {
                
                // add the time
                java.sql.Timestamp time = tasklog.getStartTime();
                template.setData("DATE", Utils.getNiceDate(time.getTime()));
                
                // add the user
                CmsUser user = cms.readOwner(tasklog);
                template.setData("USER", Utils.getFullName(user));
                
                // set the message
                template.setData("MESSAGE", addBrTags(tasklog.getComment()));
                
                // set the image
                template.setData("ICON", template.getProcessedDataValue("ICON" + type, callingObject, parameters));
                
                // set the headline
                template.setData("HEADLINE", lang.getLanguageValue("task.headline.m" + type));
                
                // generate the entry
                result.append(template.getProcessedDataValue("LISTENTRY", callingObject, parameters));
                
                // generate the spacerline
                result.append(template.getProcessedDataValue("LISTSEPETATOR", callingObject, parameters));
            }
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
}
