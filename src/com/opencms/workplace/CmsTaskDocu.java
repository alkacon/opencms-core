/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskDocu.java,v $
 * Date   : $Date: 2000/02/21 08:49:40 $
 * Version: $Revision: 1.2 $
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

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.util.*;

import java.util.*;
import java.lang.reflect.*;


/**
 * Class for building task list documentation. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;TASKDOCU&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/02/21 08:49:40 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskDocu extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants, I_CmsConstants {
	
    /**
     * Handling of the special workplace <CODE>&lt;TASKDOCU&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;TASKDOCU /&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param An XML element containing the <code>&lt;TASKDOCU&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
	
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        Vector taskdocs=new Vector();
        CmsTaskLog tasklog;  
        
        // Get the template definition values
        CmsXmlWpTemplateFile template = getTaskDocuDefinitions(cms);
	
        // get the task id
        String taskId=(String)parameters.get("taskid");
        if (taskId != null) {
            int id=new Integer(taskId).intValue();
            taskdocs=cms.readTaskLogs(id);
        }
        
        // go through all tasklogs
        for (int i=1;i<=taskdocs.size();i++) {
            tasklog=(CmsTaskLog)taskdocs.elementAt(taskdocs.size()-i);
            int type=tasklog.getType();
            // check if this is a type to be displayed
            if (type >= 100) {
                // add the time
                java.sql.Timestamp time=tasklog.getStartTime();
                template.setXmlData("DATE", Utils.getNiceDate(time.getTime()));
                // add the user
                A_CmsUser user=cms.readOwner(tasklog);
                template.setXmlData("USER", user.getFirstname()+" "+user.getLastname()+ "("+user.getName()+")");
                // set the message
                template.setXmlData("MESSAGE",tasklog.getComment());                
                // set the image
                template.setXmlData("ICON",template.getProcessedXmlDataValue("ICON"+type, callingObject, parameters));
                // set the headline
                template.setXmlData("HEADLINE",lang.getLanguageValue("task.headline.m"+type)); 
                
                // generate the entry
                result.append(template.getProcessedXmlDataValue("LISTENTRY", callingObject, parameters));
                // generate the spacerline
                result.append(template.getProcessedXmlDataValue("LISTSEPETATOR", callingObject, parameters));
         
	        }
     
        }
  
        
        
		return result.toString();
    }
}
