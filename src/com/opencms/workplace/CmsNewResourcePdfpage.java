/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourcePdfpage.java,v $
* Date   : $Date: 2003/07/15 10:42:59 $
* Version: $Revision: 1.31 $
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

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeFolder;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.I_CmsXmlParser;
import com.opencms.util.Encoder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Template class for displaying the new resource screen for a new page
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.31 $ $Date: 2003/07/15 10:42:59 $
 */

public class CmsNewResourcePdfpage extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    private static final String C_DEFAULTBODY = "<?xml version=\"1.0\"?>\n<XMLTEMPLATE>\n<TEMPLATE/>\n</XMLTEMPLATE>";

    private static final String C_PDFTEMPLATE = "pdfpage";

    /**
     * This method checks if all nescessary folders are exisitng in the content body
     * folder and creates the missing ones. <br>
     * All page contents files are stored in the content body folder in a mirrored directory
     * structure of the OpenCms filesystem. Therefor it is nescessary to create the
     * missing folders when a new page document is createg.
     * @param cms The CmsObject
     * @param path The path in the CmsFilesystem where the new page should be created.
     * @throws CmsException if something goes wrong.
     */

    private void checkFolders(CmsObject cms, String path) throws CmsException {
        String completePath = C_VFS_PATH_BODIES;
        StringTokenizer t = new StringTokenizer(path, "/");

        // check if all folders are there
        while(t.hasMoreTokens()) {
            String foldername = t.nextToken();
            try {

                // try to read the folder. if this fails, an exception is thrown
                cms.readFolder(completePath + foldername + "/");
            }
            catch(CmsException e) {

                // the folder could not be read, so create it.
                String orgFolder = completePath + foldername + "/";
                orgFolder = orgFolder.substring(C_VFS_PATH_BODIES.length() - 1);
                CmsFolder newfolder = (CmsFolder)cms.createResource(completePath, foldername, CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME);
//                CmsFolder folder = cms.readFolder(orgFolder);
                cms.lockResource(cms.readAbsolutePath(newfolder));
//                cms.chown(cms.readAbsolutePath(newfolder), cms.readOwner(folder).getName());
//                cms.chgrp(cms.readAbsolutePath(newfolder), cms.readGroup(folder).getName());
//                cms.chmod(cms.readAbsolutePath(newfolder), folder.getAccessFlags());
                cms.unlockResource(cms.readAbsolutePath(newfolder), false);
            }
            completePath += foldername + "/";
        }
    }

    /**
     * Create the pagefile for this new page.
     * @classname The name of the class used by this page.
     * @template The name of the template (content) used by this page.
     * @return Bytearray containgin the XML code for the pagefile.
     */

    private byte[] createPagefile(String classname, String template, String contenttemplate) throws CmsException {
        byte[] xmlContent = null;
        try {
            I_CmsXmlParser parser = A_CmsXmlContent.getXmlParser();
            Document docXml = parser.createEmptyDocument("page");
            Element firstElement = docXml.getDocumentElement();

            // add element CLASS
            Element elClass = docXml.createElement("CLASS");
            firstElement.appendChild(elClass);
            Node noClass = docXml.createTextNode(classname);
            elClass.appendChild(noClass);

            // add element MASTERTEMPLATE
            Element elTempl = docXml.createElement("MASTERTEMPLATE");
            firstElement.appendChild(elTempl);
            Node noTempl = docXml.createTextNode(template);
            elTempl.appendChild(noTempl);

            //add element ELEMENTDEF
            Element elEldef = docXml.createElement("ELEMENTDEF");
            elEldef.setAttribute("name", "body");
            firstElement.appendChild(elEldef);

            //add element ELEMENTDEF.CLASS
            Element elElClass = docXml.createElement("CLASS");
            elEldef.appendChild(elElClass);
            Node noElClass = docXml.createTextNode(classname);
            elElClass.appendChild(noElClass);

            //add element ELEMENTDEF.TEMPLATE
            Element elElTempl = docXml.createElement("TEMPLATE");
            elEldef.appendChild(elElTempl);
            Node noElTempl = docXml.createTextNode(contenttemplate);
            elElTempl.appendChild(noElTempl);

            // generate the output
            StringWriter writer = new StringWriter();
            parser.getXmlText(docXml, writer);
            xmlContent = writer.toString().getBytes();
        }
        catch(Exception e) {
            throw new CmsException(e.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, e);
        }
        return xmlContent;
    }

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The new page template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;

        byte[] content = new byte[0];
        // CmsResource contentFile = null;
        I_CmsSession session = cms.getRequestContext().getSession(true);

        //get the current filelist
        // String currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        String currentFilelist = CmsWorkplaceAction.getCurrentFolder(cms);
        if(currentFilelist == null) {
            currentFilelist = cms.readAbsolutePath(cms.rootFolder());
        }

        // get request parameters
        String newFile = (String)parameters.get(C_PARA_NEWFILE);

        String templatefile = (String)parameters.get(C_PARA_TEMPLATE);
        String navtitle = Encoder.redecodeUriComponent((String)parameters.get(C_PARA_NAVTEXT));
        String navpos = (String)parameters.get(C_PARA_NAVPOS);

        // get the current phase of this wizard
        String step = cms.getRequestContext().getRequest().getParameter("step");
        if(step != null) {
            if(step.equals("1")) {

                //check if the fielname has a file extension
                if(newFile.indexOf(".") == -1) {
                    newFile += ".pdf";
                }
                try {

                    // create the content for the page file
                    content = createPagefile(C_XML_CONTROL_DEFAULT_CLASS, templatefile, C_VFS_PATH_BODIES
                            + currentFilelist.substring(1, currentFilelist.length()) + newFile);

                    // check if the nescessary folders for the content files are existing.
                    // if not, create the missing folders.
                    checkFolders(cms, currentFilelist);

                    // create the page file
                    CmsResource file = cms.createResource(currentFilelist, newFile, "pdfpage", new Hashtable(),content);
                    // now create the page content file
                    cms.createResource(C_VFS_PATH_BODIES + currentFilelist.substring(1,
                                                     currentFilelist.length()), newFile, "plain", new Hashtable(),
                                                     C_DEFAULTBODY.getBytes());

                    // set the flags for the content file to internal use, the content

                    // should not be loaded
                    // cms.chmod(cms.readAbsolutePath(contentFile), contentFile.getAccessFlags() + C_ACCESS_INTERNAL_READ);

                    // now check if navigation informations have to be added to the new page.
                    if(navtitle != null) {
                        cms.writeProperty(cms.readAbsolutePath(file), C_PROPERTY_NAVTEXT, navtitle);

                        // update the navposition.
                        if(navpos != null) {
                            updateNavPos(cms, file, navpos);
                        }
                    }
                }
                catch(CmsException ex) {
                    throw new CmsException("Error while creating new Page" + ex.getMessage(), ex.getType(), ex);
                }

                // now return to filelist
                try {
                    cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms));
                }
                catch(Exception e) {
                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
                }
                return null;
            }
        }
        else {
            session.removeValue(C_PARA_FILE);
        }

        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets all required navigation information from the files and subfolders of a folder.
     * A file list of all files and folder is created, for all those resources, the navigation
     * property is read. The list is sorted by their navigation position.
     * @param cms The CmsObject.
     * @return Hashtable including three arrays of strings containing the filenames,
     * nicenames and navigation positions.
     * @throws Throws CmsException if something goes wrong.
     */

    private Hashtable getNavData(CmsObject cms) throws CmsException {
        // I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        String[] filenames;
        String[] nicenames;
        String[] positions;
        Hashtable storage = new Hashtable();
        CmsFolder folder = null;
        CmsFile file = null;
        String nicename = null;
        String currentFilelist = null;
        int count = 1;
        float max = 0;

        // get the current folder
        // currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        currentFilelist = CmsWorkplaceAction.getCurrentFolder(cms);
        if(currentFilelist == null) {
            currentFilelist = cms.readAbsolutePath(cms.rootFolder());
        }

        // get all files and folders in the current filelist.
        List files = cms.getFilesInFolder(currentFilelist);
        List folders = cms.getSubFolders(currentFilelist);

        // combine folder and file vector
        Vector filefolders = new Vector();
        Iterator enum = folders.iterator();
        while(enum.hasNext()) {
            folder = (CmsFolder)enum.next();
            filefolders.addElement(folder);
        }
        enum = files.iterator();
        while(enum.hasNext()) {
            file = (CmsFile)enum.next();
            filefolders.addElement(file);
        }
        if(filefolders.size() > 0) {

            // Create some arrays to store filename, nicename and position for the
            // nav in there. The dimension of this arrays is set to the number of
            // found files and folders plus two more entrys for the first and last
            // element.
            filenames = new String[filefolders.size() + 2];
            nicenames = new String[filefolders.size() + 2];
            positions = new String[filefolders.size() + 2];

            //now check files and folders that are not deleted and include navigation
            // information
            enum = filefolders.iterator();
            while(enum.hasNext()) {
                CmsResource res = (CmsResource)enum.next();

                // check if the resource is not marked as deleted
                if(res.getState() != C_STATE_DELETED) {
                    String navpos = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVPOS);

                    // check if there is a navpos for this file/folder
                    if(navpos != null) {
                        nicename = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_NAVTEXT);
                        if(nicename == null) {
                            nicename = res.getResourceName();
                        }

                        // add this file/folder to the storage.
                        filenames[count] = cms.readAbsolutePath(res);
                        nicenames[count] = nicename;
                        positions[count] = navpos;
                        if(new Float(navpos).floatValue() > max) {
                            max = new Float(navpos).floatValue();
                        }
                        count++;
                    }
                }
            }
        }
        else {
            filenames = new String[2];
            nicenames = new String[2];
            positions = new String[2];
        }

        // now add the first and last value
        filenames[0] = "FIRSTENTRY";
        nicenames[0] = lang.getLanguageValue("input.firstelement");
        positions[0] = "0";
        filenames[count] = "LASTENTRY";
        nicenames[count] = lang.getLanguageValue("input.lastelement");
        positions[count] = new Float(max + 1).toString();

        // finally sort the nav information.
        sort(cms, filenames, nicenames, positions, count);

        // put all arrays into a hashtable to return them to the calling method.
        storage.put("FILENAMES", filenames);
        storage.put("NICENAMES", nicenames);
        storage.put("POSITIONS", positions);
        storage.put("COUNT", new Integer(count));
        return storage;
    }

    /**
     * Gets the files displayed in the navigation select box.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @return The vectors names and values are filled with data for building the navigation.
     * @throws Throws CmsException if something goes wrong.
     */

    public Integer getNavPos(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get the nav information
        Hashtable storage = getNavData(cms);
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // finally fill the result vectors
            for(int i = 0;i <= count;i++) {
                names.addElement(nicenames[i]);
                values.addElement(nicenames[i]);
            }
        }
        else {
            values = new Vector();
        }
        return new Integer(values.size() - 1);
    }

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @return The vectors names and values are filled with the information found in the
     * workplace.ini.
     * @throws Throws CmsException if something goes wrong.
     */

    public Integer getTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        //Vector files=cms.getFilesInFolder(C_VFS_PATH_DEFAULT_TEMPLATES);
        List files = cms.getFilesInFolder(C_VFS_PATH_DEFAULT_TEMPLATES);

        // get all module Templates
        List modules = (List) new ArrayList();
        modules = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_MODULES);
        for(int i = 0;i < modules.size();i++) {
            List moduleTemplateFiles = (List) new ArrayList();
            moduleTemplateFiles = cms.getFilesInFolder(cms.readAbsolutePath((CmsFolder)modules.get(i)) + "templates/");
            for(int j = 0;j < moduleTemplateFiles.size();j++) {
                files.add(moduleTemplateFiles.get(j));
            }
        }
        Iterator enum = files.iterator();
        String templateType = null;
        while(enum.hasNext()) {
            CmsFile file = (CmsFile)enum.next();
            templateType = cms.readProperty(cms.readAbsolutePath(file), C_PROPERTY_TEMPLATETYPE);
            if((file.getState() != C_STATE_DELETED) && (C_PDFTEMPLATE.equals(templateType))) {
                String nicename = cms.readProperty(cms.readAbsolutePath(file), C_PROPERTY_TITLE);
                if(nicename == null) {
                    nicename = file.getResourceName();
                }
                names.addElement(nicename);
                values.addElement(cms.readAbsolutePath(file));
            }
        }
        bubblesort(names, values);
        return new Integer(0);
    }

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

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Sorts a set of three String arrays containing navigation information depending on
     * their navigation positions.
     * @param cms Cms Object for accessign files.
     * @param filenames Array of filenames
     * @param nicenames Array of well formed navigation names
     * @param positions Array of navpostions
     */

    private void sort(CmsObject cms, String[] filenames, String[] nicenames,
            String[] positions, int max) {

        // Sorting algorithm
        // This method uses an bubble sort, so replace this with something more
        // efficient
        for(int i = max - 1;i > 1;i--) {
            for(int j = 1;j < i;j++) {
                float a = new Float(positions[j]).floatValue();
                float b = new Float(positions[j + 1]).floatValue();
                if(a > b) {
                    String tempfilename = filenames[j];
                    String tempnicename = nicenames[j];
                    String tempposition = positions[j];
                    filenames[j] = filenames[j + 1];
                    nicenames[j] = nicenames[j + 1];
                    positions[j] = positions[j + 1];
                    filenames[j + 1] = tempfilename;
                    nicenames[j + 1] = tempnicename;
                    positions[j + 1] = tempposition;
                }
            }
        }
    }

    /**
     * Updates the navigation position of all resources in the actual folder.
     * @param cms The CmsObject.
     * @param newfile The new file added to the nav.
     * @param navpos The file after which the new entry is sorted.
     */

    private void updateNavPos(CmsObject cms, CmsResource newfile, String newpos) throws CmsException {
        float newPos = 0;

        // get the nav information
        Hashtable storage = getNavData(cms);
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            String[] positions = (String[])storage.get("POSITIONS");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // now find the file after which the new file is sorted
            int pos = 0;
            for(int i = 0;i < nicenames.length;i++) {
                if(newpos.equals((String)nicenames[i])) {
                    pos = i;
                }
            }
            if(pos < count) {
                float low = new Float(positions[pos]).floatValue();
                float high = new Float(positions[pos + 1]).floatValue();
                newPos = (high + low) / 2;
            }
            else {
                newPos = new Float(positions[pos]).floatValue() + 1;
            }
        }
        else {
            newPos = 1;
        }
        cms.writeProperty(cms.readAbsolutePath(newfile), C_PROPERTY_NAVPOS, new Float(newPos).toString());
    }
}
