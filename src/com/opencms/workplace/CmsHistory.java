/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsHistory.java,v $
* Date   : $Date: 2003/07/16 10:12:10 $
* Version: $Revision: 1.30 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying the history file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.30 $ $Date: 2003/07/16 10:12:10 $
 */

public class CmsHistory extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the history template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The history template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {
            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
            session.removeValue("version");
        }

        // get the filename
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);

        // get the version
        String versionId = (String)parameters.get("versionid");
        Integer id = null;
        CmsBackupResource backupFile = null;
        String theFileName = "";
        if(versionId != null) {
            id = new Integer(Integer.parseInt(versionId));
            session.putValue("version", versionId);
            backupFile = (CmsBackupResource)cms.readBackupFileHeader(filename, id.intValue());
            theFileName = backupFile.getResourceName();
        }
        else {
            CmsFile offlineFile = (CmsFile)cms.readFileHeader(filename);
            theFileName = offlineFile.getResourceName();
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // test if the project paremeter was included, display the detail dialog.
        if(id != null) {
            template = "detail";
            CmsBackupProject project = cms.readBackupProject(id.intValue());
            xmlTemplateDocument.setData("PROJECT", project.getName());
            String title = cms.readProperty(filename, C_PROPERTY_TITLE);
            if(title == null) {
                title = "";
            }
            if(cms.getRequestContext().currentProject().getId() == C_PROJECT_ONLINE_ID){
                // This is the online project, show the buttons close and show version only
                xmlTemplateDocument.setData("BUTTONRESTORE",xmlTemplateDocument.getProcessedDataValue("DISABLERESTORE", this));
            } else {
                // This is an offline project, show all buttons if the resource is locked
                CmsFile currentFile = (CmsFile)cms.readFileHeader(filename);
                if (currentFile.isLocked()) {
                    // show the button for restore the version
                    xmlTemplateDocument.setData("BUTTONRESTORE",xmlTemplateDocument.getProcessedDataValue("ENABLERESTORE", this));
                } else {
                    xmlTemplateDocument.setData("BUTTONRESTORE",xmlTemplateDocument.getProcessedDataValue("DISABLERESTORE", this));
                }
            }
            String editedBy = backupFile.getLastModifiedByName();
            xmlTemplateDocument.setData("TITLE", Encoder.escapeXml(title));
            xmlTemplateDocument.setData("SIZE", new Integer(backupFile.getLength()).toString());
            xmlTemplateDocument.setData("EDITEDBY", editedBy);
            xmlTemplateDocument.setData("EDITEDAT", Utils.getNiceDate(backupFile.getDateLastModified()));
            xmlTemplateDocument.setData("PUBLISHEDBY", project.getPublishedByName());
            xmlTemplateDocument.setData("PUBLISHEDAT", Utils.getNiceDate(project.getPublishingDate()));
            xmlTemplateDocument.setData("PROJECTDESCRIPTION", Encoder.escapeXml(project.getDescription()));
        }
        xmlTemplateDocument.setData("FILENAME", theFileName);

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets all groups that can new group of the resource.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getFiles(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_FILE);
        if(filename != null) {
            List allFiles = cms.readAllBackupFileHeaders(filename);
            // vector is already sorted by version id
            //if(allFiles.size() > 0) {
            //    allFiles = sort(allFiles, Utils.C_SORT_PUBLISHED_DOWN);
            //}
            // fill the names and values
            for(int i = 0;i < allFiles.size();i++) {
                CmsBackupResource file = ((CmsBackupResource)allFiles.get(i));
                long updated = file.getDateCreated();
                String userName = "";
                try{
                    userName = cms.readUser(file.getUserLastModified()).getName();
                } catch(CmsException exc){
                    userName = file.getLastModifiedByName();
                }
                long lastModified = file.getDateLastModified();
                String output = Utils.getNiceDate(lastModified) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                                + Utils.getNiceDate(updated) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                                + userName;
                names.addElement(output);
                values.addElement(file.getVersionId()+"");
            }
        }
        return new Integer(-1);
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
