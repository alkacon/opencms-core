/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourcePage.java,v $
* Date   : $Date: 2002/08/26 13:00:41 $
* Version: $Revision: 1.48 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.linkmanagement.*;
import com.opencms.template.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

/**
 * Template class for displaying the new resource screen for a new page
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.48 $ $Date: 2002/08/26 13:00:41 $
 */

public class CmsNewResourcePage extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {


    /** Definition of the class */
    private final static String C_CLASSNAME = "com.opencms.template.CmsXmlTemplate";

    private static final String C_DEFAULTBODY = "<?xml version=\"1.0\"?>\n<XMLTEMPLATE>\n<TEMPLATE/>\n</XMLTEMPLATE>";

    /**
     * Create the pagefile for this new page.
     * @classname The name of the class used by this page.
     * @template The name of the template (content) used by this page.
     * @return Bytearray containgin the XML code for the pagefile.
     */

    private byte[] createPagefile(String classname, String template,
            String contenttemplate) throws CmsException {
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
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // the template to be displayed
        String template = null;

        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // TODO: check, if this is neede: String type=null;
        byte[] content = new byte[0];
        CmsFile contentFile = null;
        I_CmsSession session = cms.getRequestContext().getSession(true);

        //get the current filelist
        String currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        if(currentFilelist == null) {
            currentFilelist = cms.rootFolder().getAbsolutePath();
        }

        // get request parameters
        String newFile = (String)parameters.get(C_PARA_NEWFILE);
        String title = (String)parameters.get(C_PARA_TITLE);
        String keywords = (String)parameters.get(C_PARA_KEYWORDS);
        String description = (String)parameters.get(C_PARA_DESCRIPTION);

        // look if createFolder called us, then we have to preselect index.html as name
        String fromFolder = (String)parameters.get("fromFolder");
        if((fromFolder != null) && ("true".equals(fromFolder))){
            xmlTemplateDocument.setData("name", "index.html");
            // deaktivate the linklayer
            xmlTemplateDocument.setData("doOnload", "checkInTheBox();");
        }else{
            xmlTemplateDocument.setData("name", "");
            xmlTemplateDocument.setData("doOnload", "");
        }

        // TODO: check, if this is neede: String flags=(String)parameters.get(C_PARA_FLAGS);
        String templatefile = (String)parameters.get(C_PARA_TEMPLATE);
        String navtitle = (String)parameters.get(C_PARA_NAVTEXT);
        String navpos = (String)parameters.get(C_PARA_NAVPOS);
        String layoutFilePath = (String)parameters.get(C_PARA_LAYOUT);

        // get the current phase of this wizard
        String step = cms.getRequestContext().getRequest().getParameter("step");
        if(step != null) {
            if(step.equals("1")) {

                //check if the fielname has a file extension
                if(newFile.indexOf(".") == -1) {
                    newFile += ".html";
                }
                try {

                    // create the content for the page file
                    content = createPagefile(C_CLASSNAME, templatefile, C_CONTENTBODYPATH
                            + currentFilelist.substring(1, currentFilelist.length()) + newFile);

                    // check if the nescessary folders for the content files are existing.

                    // if not, create the missing folders.
                    //checkFolders(cms, currentFilelist);

                    // create the page file
                    Hashtable prop = new Hashtable();
                    prop.put(C_PROPERTY_TITLE, title);
                    CmsResource file = null;
                    String resourceType = (String)session.getValue("resourctype_for_new_page");
                    session.removeValue("resourctype_for_new_page");
                    if(resourceType != null && "gemadipage".equals(resourceType)){
                        file = ((CmsResourceTypePage)cms.getResourceType(resourceType)).createResource(cms, currentFilelist, newFile, prop, "".getBytes(), templatefile) ;
                    }else{
                        //CmsResourceTypePage rtpage = new CmsResourceTypePage();
                        //file = rtpage.createResource(cms, currentFilelist, newFile, prop, "".getBytes(), templatefile);
                        file = ((CmsResourceTypePage)cms.getResourceType("page")).createResource(cms, currentFilelist, newFile, prop, "".getBytes(), templatefile) ;
                    }

                    if( keywords != null && !keywords.equals("") ) {
                        cms.writeProperty(file.getAbsolutePath(), C_PROPERTY_KEYWORDS, keywords);
                    }
                    if( description != null && !description.equals("") ) {
                        cms.writeProperty(file.getAbsolutePath(), C_PROPERTY_DESCRIPTION, description);
                    }

                    byte[] bodyBytes = null;
                    boolean layoutFileDefined = false;
                    if (layoutFilePath == null || layoutFilePath.equals("")) {
                        // layout not specified, use default body
                        bodyBytes = C_DEFAULTBODY.getBytes();
                    } else {
                        // do not catch exceptions, a specified layout should exist
                        CmsFile layoutFile = cms.readFile(layoutFilePath);
                        bodyBytes = layoutFile.getContents();
                        layoutFileDefined = true;
                    }
                    CmsFile bodyFile = cms.readFile(C_CONTENTBODYPATH + currentFilelist.substring(1,
                                currentFilelist.length()), newFile);
                    bodyFile.setContents(bodyBytes);
                    cms.writeFile(bodyFile);

                    // care about the linkmanagement if a default body was selected
                    if(layoutFileDefined){
                        CmsPageLinks linkObject = cms.getPageLinks(currentFilelist+newFile);
                        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
                    }
                    // now check if navigation informations have to be added to the new page.
                    if(navtitle != null) {
                        cms.writeProperty(file.getAbsolutePath(), C_PROPERTY_NAVTEXT, navtitle);

                        // update the navposition.
                        if(navpos != null) {
                            updateNavPos(cms, file, navpos);
                        }
                    }
                }catch(CmsException ex) {
                    throw new CmsException("Error while creating new Page" + ex.getMessage(), ex.getType(), ex);
                }

                // TODO: ErrorHandling
                // now return to filelist
                try {
                    cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                            + C_WP_EXPLORER_FILELIST);
                }catch(Exception e) {
                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                            + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
                }
                return null;
            }
        }else {
            String putValue = (String)parameters.get("root.pagetype");
            if(putValue != null){
                session.putValue("resourctype_for_new_page", (String)parameters.get("root.pagetype"));
            }else{
                session.removeValue("resourctype_for_new_page");
            }
            session.removeValue(C_PARA_FILE);
        }

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
     * @exception Throws CmsException if something goes wrong.
     */

    private Hashtable getNavData(CmsObject cms) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
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
        currentFilelist = (String)session.getValue(C_PARA_FILELIST);
        if(currentFilelist == null) {
            currentFilelist = cms.rootFolder().getAbsolutePath();
        }

        // get all files and folders in the current filelist.
        Vector files = cms.getFilesInFolder(currentFilelist);
        Vector folders = cms.getSubFolders(currentFilelist);

        // combine folder and file vector
        Vector filefolders = new Vector();
        Enumeration enum = folders.elements();
        while(enum.hasMoreElements()) {
            folder = (CmsFolder)enum.nextElement();
            filefolders.addElement(folder);
        }
        enum = files.elements();
        while(enum.hasMoreElements()) {
            file = (CmsFile)enum.nextElement();
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
            enum = filefolders.elements();
            while(enum.hasMoreElements()) {
                CmsResource res = (CmsResource)enum.nextElement();

                // check if the resource is not marked as deleted
                if(res.getState() != C_STATE_DELETED) {
                    String navpos = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_NAVPOS);

                    // check if there is a navpos for this file/folder
                    if(navpos != null) {
                        nicename = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_NAVTEXT);
                        if(nicename == null) {
                            nicename = res.getName();
                        }

                        // add this file/folder to the storage.
                        filenames[count] = res.getAbsolutePath();
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
        nicenames[0] = lang.getDataValue("input.firstelement");
        positions[0] = "0";
        filenames[count] = "LASTENTRY";
        nicenames[count] = lang.getDataValue("input.lastelement");
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
     * @returns The vectors names and values are filled with data for building the navigation.
     * @exception Throws CmsException if something goes wrong.
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
     * @returns The vectors names and values are filled with the information found in the
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */

    public Integer getTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        return CmsHelperMastertemplates.getTemplates(cms, names, values, null);
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
     * Sorts a set of three String arrays containing navigation information depending on
     * their navigation positions.
     * @param cms Cms Object for accessign files.
     * @param filenames Array of filenames
     * @param nicenames Array of well formed navigation names
     * @param positions Array of navpostions
     */

    private void sort(CmsObject cms, String[] filenames, String[] nicenames, String[] positions, int max) {

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
        cms.writeProperty(newfile.getAbsolutePath(), C_PROPERTY_NAVPOS, new Float(newPos).toString());
    }

    /**
     * Gets the default bodies displayed on the "new page" dialog.
     * @param cms The CmsObject.
     * @param names Will be filled with the display names of the found default bodies.
     * @param values Will be filled with the file names of the found default bodies.
     * @param parameters Hashtable of parameters (not used yet).
     * @return The return value is always 0
     * @exception Throws CmsException if something goes wrong.
     */
    public Integer getLayouts(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // Gather templates from the VFS
        CmsHelperMastertemplates.getTemplateElements(cms, I_CmsWpConstants.C_DEFAULTBODIESDIR, names, values);

        // Always return 0
        return new Integer(0);
    }
}
