/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceExternallink.java,v $
* Date   : $Date: 2004/02/05 08:28:07 $
* Version: $Revision: 1.6 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypePointer;
import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Template class for displaying the new resource screen for a new link
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2004/02/05 08:28:07 $
 */

public class CmsNewResourceExternallink extends CmsWorkplaceDefault {
    
    private static final String C_PARA_KEEP_PROPERTIES = "keepproperties";
    private static final String C_PARA_ADD_TO_NAV = "addtonav";
    private static final int DEBUG = 0;

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource othertype template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        String error = "";
        boolean checkurl = true;

        String filename = null;
        String targetName = null;
        String foldername = null;
        // String type = null;
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null){
            // remove all session values
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_LINK);
            session.removeValue(C_PARA_VIEWFILE);
            session.removeValue(C_PARA_NAVPOS);
            session.removeValue(C_PARA_NAVTEXT);
            session.removeValue("lasturl");
        }
        // get the lasturl from parameters or from session
        String lastUrl = getLastUrl(cms, parameters);
        if(lastUrl != null){
            session.putValue("lasturl", lastUrl);
        }
        // get the linkname and the linkurl
        filename = cms.getRequestContext().getRequest().getParameter(C_PARA_RESOURCE);
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }  else {
            // try to get the value from the session, e.g. after an error
            filename = (String)session.getValue(C_PARA_RESOURCE)!=null?(String)session.getValue(C_PARA_RESOURCE):"";
        }
        targetName = cms.getRequestContext().getRequest().getParameter(C_PARA_LINK);
        if(targetName != null) {
            session.putValue(C_PARA_LINK, targetName);
        } else {
            // try to get the value from the session, e.g. after an error
            targetName = (String)session.getValue(C_PARA_LINK)!=null?(String)session.getValue(C_PARA_LINK):"";
        }
        
        // get the parameters
        String navtitle = (String)parameters.get(C_PARA_NAVTEXT);
        if(navtitle != null) {
            session.putValue(C_PARA_NAVTEXT, navtitle);
        } else {
            // try to get the value from the session, e.g. after an error
            navtitle = (String)session.getValue(C_PARA_NAVTEXT)!=null?(String)session.getValue(C_PARA_NAVTEXT):"";
        }
        
        String navpos = (String)parameters.get(C_PARA_NAVPOS);
        if(navpos != null) {
            session.putValue(C_PARA_NAVPOS, navpos);
        } else {
            // try to get the value from the session, e.g. after an error
            navpos = (String)session.getValue(C_PARA_NAVPOS)!=null?(String)session.getValue(C_PARA_NAVPOS):"";
        }
        
        String dummy = (String)parameters.get(CmsNewResourceExternallink.C_PARA_KEEP_PROPERTIES);
        if (DEBUG>0) System.out.println( "parameter " + CmsNewResourceExternallink.C_PARA_KEEP_PROPERTIES + ": " + dummy );
        boolean keepTargetProperties = false;
        if (dummy!=null) {
        	session.putValue(CmsNewResourceExternallink.C_PARA_KEEP_PROPERTIES, dummy);
        }
        else {
        	dummy = (String)session.getValue(CmsNewResourceExternallink.C_PARA_KEEP_PROPERTIES)!=null?(String)session.getValue(CmsNewResourceExternallink.C_PARA_KEEP_PROPERTIES):"true";
        }
		keepTargetProperties = dummy.trim().equalsIgnoreCase("true");
        
        dummy = (String)parameters.get(CmsNewResourceExternallink.C_PARA_ADD_TO_NAV);
        if (DEBUG>0) System.out.println( "parameter " + CmsNewResourceExternallink.C_PARA_ADD_TO_NAV + ": " + dummy );
        boolean addToNav = false;
        if (dummy!=null) {
        	session.putValue(CmsNewResourceExternallink.C_PARA_ADD_TO_NAV, dummy);
        }
        else {
        	dummy = (String)session.getValue(CmsNewResourceExternallink.C_PARA_ADD_TO_NAV)!=null?(String)session.getValue(CmsNewResourceExternallink.C_PARA_ADD_TO_NAV):"false";
        }
		addToNav = dummy.trim().equalsIgnoreCase("true");        
        
        String notChange = (String)parameters.get("newlink");
        
        CmsResource linkResource = null;
        String step = cms.getRequestContext().getRequest().getParameter("step");

        // set the values e.g. after an error
        xmlTemplateDocument.setData("LINKNAME", filename);
        xmlTemplateDocument.setData("LINKVALUE", targetName);
        xmlTemplateDocument.setData("NAVTITLE", CmsEncoder.escapeHtml(navtitle));
        xmlTemplateDocument.setData("KEEPPROPERTIES", keepTargetProperties==true ? "true" : "false" );
        xmlTemplateDocument.setData("ADDTONAV", addToNav==true ? "true" : "false" );

        // if an existing link should be edited show the change page
        if(notChange != null && notChange.equals("false") && step == null) {
            try{
                CmsFile currentFile = cms.readFile(filename);
                String content = new String(currentFile.getContents());
                xmlTemplateDocument.setData("LINKNAME", currentFile.getName());
                xmlTemplateDocument.setData("LINK", cms.readAbsolutePath(currentFile));
                xmlTemplateDocument.setData("LINKVALUE", content);
                templateSelector = "change";
            } catch (CmsException e){
                error = e.getShortException();
            }
        }

        // get the current phase of this wizard
        if(step != null) {
            // step 1 - show the final selection screen
            if(step.equals("1") || step.equals("2")) {
                try{
                    // step 1 - create the link with checking http-link
                    // step 2 - create the link without link check
                    // get folder- and filename
                    // foldername = (String)session.getValue(C_PARA_FILELIST);
                    foldername = CmsWorkplaceAction.getCurrentFolder(cms);

                    if(foldername == null) {
                        foldername = cms.readAbsolutePath(cms.rootFolder());
                    }

                    String title = lang.getLanguageValue("explorer.linkto") + " " + targetName;
                    // type = "link";
                    if(notChange != null && notChange.equals("false")) {
            
                        // change old file
                        CmsFile editFile = cms.readFile(filename);
                        editFile.setContents(targetName.getBytes());

                        if(step.equals("1")){
                            // TODO: write some better link check which will not fail if there is
                            // no access to the internet                            
                            //if(!targetName.startsWith("/")){
                            //    checkurl = CmsLinkCheck.checkUrl(targetName);
                            //}
                        }
                        checkurl=true;
                        if(checkurl){
                            cms.writeFile(editFile);
                            cms.writeProperty(filename, C_PROPERTY_TITLE, title);
                        }
                        linkResource = editFile;
                    } else {
                        // link URL is a file, so create the new file
                        Hashtable prop = new Hashtable();
                        prop.put(C_PROPERTY_TITLE, title);
                        if (step.equals("1")) {
                            //if (!targetName.startsWith("/")) {
                            //    checkurl = CmsLinkCheck.checkUrl(targetName);
                            //}
                        }
                        checkurl=true;
                        if (checkurl ) {
                            Map targetProperties = null;
                            
                            
                            // TODO VFS links: creates an external HTTP link following the new linking paradigm
                            //linkResource = cms.createVfsLink(foldername + filename, targetName, targetProperties);
                            linkResource = cms.createResource(foldername + filename, CmsResourceTypePointer.C_RESOURCE_TYPE_ID, prop, targetName.getBytes(), targetProperties);
                        }
                        
                 
                    }
                    // now check if navigation informations have to be added to the new page.
                    if(addToNav && checkurl) {
                        cms.writeProperty(cms.readAbsolutePath(linkResource), C_PROPERTY_NAVTEXT, navtitle);
                        // update the navposition.
                        if(navpos != null) {
                            updateNavPos(cms, linkResource, navpos);
                        }
                    }

                    // remove values from session
                    session.removeValue(C_PARA_RESOURCE);
                    session.removeValue(C_PARA_VIEWFILE);
                    session.removeValue(C_PARA_LINK);
                    session.removeValue(C_PARA_NAVTEXT);
                    session.removeValue(C_PARA_NAVPOS);

                    // now return to appropriate filelist
                } catch (CmsException e){
                    error = e.getShortException();
                }

                if(checkurl && ("".equals(error.trim()))){
                    try {
                        if(lastUrl != null) {
                            cms.getRequestContext().getResponse().sendRedirect(lastUrl);
                        } else {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                    + CmsWorkplaceAction.getExplorerFileUri(cms));
                        }
                    } catch(Exception e) {
                        throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
            }
        } else {
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_VIEWFILE);
            session.removeValue(C_PARA_LINK);
            session.removeValue(C_PARA_NAVTEXT);
        }
        // set lasturl
        if(lastUrl == null) {
            lastUrl = CmsWorkplaceAction.getExplorerFileUri(cms);
        }
        xmlTemplateDocument.setData("lasturl", lastUrl);
        // set the templateselector if there was an error
        if(!checkurl){
            xmlTemplateDocument.setData("folder", foldername);
            xmlTemplateDocument.setData("newlink", notChange);
            session.putValue(C_PARA_LINK, targetName);
            session.putValue(C_PARA_RESOURCE, filename);
            session.putValue(C_PARA_NAVTEXT, navtitle);
            session.putValue(C_PARA_NAVPOS, navpos);
            templateSelector = "errorcheckurl";
        }
        if(!"".equals(error.trim())){
            xmlTemplateDocument.setData("errordetails", error);
            session.putValue(C_PARA_LINK, targetName);
            session.putValue(C_PARA_RESOURCE, filename);
            session.putValue(C_PARA_NAVTEXT, navtitle);
            session.putValue(C_PARA_NAVPOS, navpos);
            templateSelector = "error";
        }
        // process the selected template
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

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Returns the current workplace path Uri
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObject Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @throws CmsException if something goes wrong
     */
    public Object getCurrentPathUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws CmsException {
        HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession();
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute("__CmsWorkplace.WORKPLACE_SETTINGS");
        String path = settings.getExplorerResource();
        if (path == null) {
            path = "/";
        }
        if (path.indexOf("/") != -1) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        }       
        return path.getBytes();
    }

    /**
     * Sets the value of the new file input field of dialog.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is set into the new file dialod.
     * @throws CmsExeption if something goes wrong.
     */

    public String setValue(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // get a previous value from the session
        String filename = (String)session.getValue(C_PARA_RESOURCE);
        if(filename == null) {
            filename = "";
        }
        return filename;
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
        int retValue = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String preselect = (String)session.getValue(C_PARA_NAVPOS);
        // get the nav information
        Hashtable storage = getNavData(cms);
        if(storage.size() > 0) {
            String[] nicenames = (String[])storage.get("NICENAMES");
            int count = ((Integer)storage.get("COUNT")).intValue();

            // finally fill the result vectors
            for(int i = 0;i <= count;i++) {
                names.addElement(CmsEncoder.escapeHtml(nicenames[i]));
                values.addElement(CmsEncoder.escapeHtml(nicenames[i]));
                if ((preselect != null) && (preselect.equals(nicenames[i]))){
                    retValue = values.size() -1;
                }
            }
        }
        else {
            values = new Vector();
        }
        if (retValue == -1){
            // set the default value to no change
            return new Integer(values.size() - 1);
        }else{
            return new Integer(retValue);
        }
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
                            nicename = res.getName();
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
                if(newpos.equals(nicenames[i])) {
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
