/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminModuleDeleteWarning.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.22 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the Warnings when deleting a module.
 * Creation date: (15.09.00 10:21:25)
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsAdminModuleDeleteWarning extends CmsWorkplaceDefault {
    
    /**
     * the different template tags.
     */
    private final String C_READY = "ready";
    private final String C_WINCONTENT = "windowcontent";
    private final String C_FILELISTENTRY = "filelist_entry";
    private final String C_FILENAME = "filename";
    private final String C_W_TITLE = "windowtitle";
    private final String C_W_TEXT = "windowtext";
    private final String C_CBENTRY = "checkbox_entry";
    private final String C_STEP_0 = "0";
    private final String C_STEP_CHECKSUM_2 = "checksum_2";
    private final String C_STEP_PROPFILES_1 = "propfiles_1";
    private final String C_STEP_PROPFILES_2 = "propjiles_2";
    private final String C_STEP_INUSE = "inuse";
    private final String C_STEP_MISSFILES = "missfiles";
    
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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
        
        //      CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);        
        //      CmsXmlLanguageFile lang=new CmsXmlLanguageFile(cms);   
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String step = (String)session.getValue(C_SESSION_MODULE_DELETE_STEP);
        if(step != null) {
            if(C_STEP_0.equals(step)) {
                
                // first call
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_CHECKSUM);
                if(files.isEmpty()) {
                    step = C_STEP_PROPFILES_1;
                }
                else {
                    
                    // there are some files with wrong Checksum; fill the template and set the next step
                    xmlTemplateDocument.setData(C_W_TITLE, lang.getLanguageValue("module.error.deletetitle"));
                    xmlTemplateDocument.setData(C_W_TEXT, lang.getLanguageValue("module.error.deletetext1"));
                    String output = "";
                    for(int i = 0;i < files.size();i++) {
                        xmlTemplateDocument.setData(C_FILENAME, (String)files.elementAt(i));
                        output += xmlTemplateDocument.getProcessedDataValue(C_CBENTRY);
                    }
                    xmlTemplateDocument.setData(C_WINCONTENT, output);
                    session.putValue(C_SESSION_MODULE_DELETE_STEP, C_STEP_CHECKSUM_2);
                }
            }
            if(C_STEP_CHECKSUM_2.equals(step)) {
                
                // ok, get the files that should not deleted and put them in the session.
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_CHECKSUM);
                Vector outFiles = new Vector();
                for(int i = 0;i < files.size();i++) {
                    String file = (String)files.elementAt(i);
                    String test = (String)parameters.get(file);
                    if(test == null) {
                        outFiles.addElement(file);
                    }
                }
                session.putValue(C_SESSION_MODULE_EXCLUSION, outFiles);
                step = C_STEP_PROPFILES_1;
            }
            if(C_STEP_PROPFILES_1.equals(step)) {
                
                // now we take care about the filesWithProperty vector.The same way like the wrong ChecksumVector.
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_PROPFILES);
                if(files.isEmpty()) {
                    step = C_STEP_INUSE;
                }
                else {
                    
                    // we have some of these files, lets fill the template and set the next step.
                    xmlTemplateDocument.setData(C_W_TITLE, lang.getLanguageValue("module.error.deletetitle"));
                    xmlTemplateDocument.setData(C_W_TEXT, lang.getLanguageValue("module.error.deletetext2"));
                    String output = "";
                    for(int i = 0;i < files.size();i++) {
                        xmlTemplateDocument.setData(C_FILENAME, (String)files.elementAt(i));
                        output += xmlTemplateDocument.getProcessedDataValue(C_CBENTRY);
                    }
                    xmlTemplateDocument.setData(C_WINCONTENT, output);
                    session.putValue(C_SESSION_MODULE_DELETE_STEP, C_STEP_PROPFILES_2);
                }
            }
            if(C_STEP_PROPFILES_2.equals(step)) {
                
                // ok, get the files that should not deleted and put them in the session.                
                // first look if there is already a exclusionVector in the session.
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_PROPFILES);
                Vector outFiles = (Vector)session.getValue(C_SESSION_MODULE_EXCLUSION);
                if(outFiles == null) {
                    outFiles = new Vector();
                }
                for(int i = 0;i < files.size();i++) {
                    String file = (String)files.elementAt(i);
                    String test = (String)parameters.get(file);
                    if(test == null) {
                        outFiles.addElement(file);
                    }
                }
                session.putValue(C_SESSION_MODULE_EXCLUSION, outFiles);
                step = C_STEP_INUSE;
            }
            if(C_STEP_INUSE.equals(step)) {
                
                // the files that are in use. if there are any, just show them to the user.
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_INUSE);
                if(files.isEmpty()) {
                    step = C_STEP_MISSFILES;
                }
                else {
                    xmlTemplateDocument.setData(C_W_TITLE, lang.getLanguageValue("module.error.deletetitle"));
                    xmlTemplateDocument.setData(C_W_TEXT, lang.getLanguageValue("module.error.deletetext3"));
                    String output = "";
                    for(int i = 0;i < files.size();i++) {
                        xmlTemplateDocument.setData(C_FILENAME, (String)files.elementAt(i));
                        output += xmlTemplateDocument.getProcessedDataValue(C_FILELISTENTRY);
                    }
                    xmlTemplateDocument.setData(C_WINCONTENT, output);
                    session.putValue(C_SESSION_MODULE_DELETE_STEP, C_STEP_MISSFILES);
                }
            }
            if(C_STEP_MISSFILES.equals(step)) {
                
                // they are already gone, just inform the user
                Vector files = (Vector)session.getValue(C_SESSION_MODULE_MISSFILES);
                if(files.isEmpty()) {
                    templateSelector = C_READY;
                }
                else {
                    xmlTemplateDocument.setData(C_W_TITLE, lang.getLanguageValue("module.error.deletetitle"));
                    xmlTemplateDocument.setData(C_W_TEXT, lang.getLanguageValue("module.error.deletetext4"));
                    String output = "";
                    for(int i = 0;i < files.size();i++) {
                        xmlTemplateDocument.setData(C_FILENAME, (String)files.elementAt(i));
                        output += xmlTemplateDocument.getProcessedDataValue(C_FILELISTENTRY);
                    }
                    xmlTemplateDocument.setData(C_WINCONTENT, output);
                    session.putValue(C_SESSION_MODULE_DELETE_STEP, C_READY);
                }
            }
            if(C_READY.equals(step)) {
                templateSelector = C_READY;
            }
        }
        
        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
}
