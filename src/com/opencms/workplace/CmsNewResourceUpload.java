/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceUpload.java,v $
* Date   : $Date: 2004/02/04 17:18:07 $
* Version: $Revision: 1.55 $
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

import org.opencms.db.CmsImportFolder;
import org.opencms.locale.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResourceTypeImage;
import com.opencms.file.CmsUser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Template class for displaying the new resource upload screen
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.55 $ $Date: 2004/02/04 17:18:07 $
 */
public class CmsNewResourceUpload extends CmsWorkplaceDefault {
    
    private static final String C_PARAM_OVERWRITE = "overwrite";
    private static final String C_PARAM_CANCEL = "cancel";

    private static final int DEBUG = 0;    

    /** Vector containing all names of the radiobuttons */
    private Vector m_names = null;

    /** Vector containing all links attached to the radiobuttons */
    private Vector m_values = null;

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource upload page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The upload template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

         
        I_CmsSession session = cms.getRequestContext().getSession(true);
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        // the template to be displayed
        String template = null;
        String appletMode="";      
        
        try {

       // I_CmsSession session = cms.getRequestContext().getSession(true);
        
        // get the file size upload limitation value (value is in kB)
        int maxFileSize = OpenCms.getWorkplaceManager().getFileMaxUploadSize();                          
        // check if current user belongs to Admin group, if so no file upload limit
        if ((maxFileSize <= 0) || cms.userInGroup(cms.getRequestContext().currentUser().getName(), OpenCms.getDefaultUsers().getGroupAdministrators())) {
            maxFileSize = -1;
        }        
        
        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if (initial != null) {
            // remove all session values
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_FILECONTENT);
            session.removeValue(C_PARA_NEWTYPE);
            session.removeValue(C_PARA_TITLE);
            session.removeValue(CmsNewResourceUpload.C_PARAM_OVERWRITE);
            session.removeValue("lasturl");
        }
        String lastUrl = getLastUrl(cms, parameters);

        // get the parameters from the request and session
        String step = (String)parameters.get("STEP");

        String unzip = (String) parameters.get("unzip");
        String nofolder = (String) parameters.get("NOFOLDER");

        String error = (String) parameters.get("error");


        String currentFolder = (String)parameters.get(C_PARA_FILELIST);
        //String currentFolder = CmsWorkplaceAction.getCurrentFolder(cms);
        if(currentFolder != null) {
            // session.putValue(C_PARA_FILELIST, currentFolder);
            CmsWorkplaceAction.setCurrentFolder(cms, currentFolder);
        }
        // currentFolder = (String)session.getValue(C_PARA_FILELIST);
        currentFolder = CmsWorkplaceAction.getCurrentFolder(cms);
        if(currentFolder == null) {
            currentFolder = cms.readAbsolutePath(cms.rootFolder());
        }

        String title = (String) parameters.get(C_PARA_TITLE);
        if (title!=null && !"".equals(title.trim())) {
            session.putValue(C_PARA_TITLE, title);
        }
        else {
            title = (String)session.getValue(C_PARA_TITLE);
        }
        
        String newname = (String)parameters.get(C_PARA_NAME);

        // get filename and file content if available
        String filename = null;
        byte[] filecontent = new byte[0];

        // get the filename
        Enumeration files = cms.getRequestContext().getRequest().getFileNames();
        while(files.hasMoreElements()) {
            filename = (String)files.nextElement();
        }
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);

        // get the filecontent
        if(filename != null) {
            filecontent = cms.getRequestContext().getRequest().getFile(filename);
        }
        if(filecontent != null) {
            session.putValue(C_PARA_FILECONTENT, filecontent);
        }
        filecontent = (byte[])session.getValue(C_PARA_FILECONTENT);

        //get the filetype
        String newtype = (String)parameters.get(C_PARA_NEWTYPE);
        if(newtype != null) {
            session.putValue(C_PARA_NEWTYPE, newtype);
        }
        newtype = (String)session.getValue(C_PARA_NEWTYPE);


        xmlTemplateDocument.setData("lasturl", lastUrl);
        
        // get the overwrite parameter
        String dummy = (String) parameters.get(CmsNewResourceUpload.C_PARAM_OVERWRITE);
        boolean replaceResource = false;
        if (dummy != null) {
            replaceResource = "yes".equals(dummy);
            session.putValue(CmsNewResourceUpload.C_PARAM_OVERWRITE, dummy);
        }
        else {
            replaceResource = "yes".equals(session.getValue(CmsNewResourceUpload.C_PARAM_OVERWRITE));
        }

        // get the cancel parameter
        boolean cancelUpload = "yes".equals(parameters.get(CmsNewResourceUpload.C_PARAM_CANCEL));
        
        // display upload form with limitation of file size
        if (maxFileSize > 0) {
            xmlTemplateDocument.setData("maxfilesize", "" + maxFileSize);
            String limitation = xmlTemplateDocument.getProcessedDataValue("filesize_limited");
            xmlTemplateDocument.setData("limitation", limitation);
        }
        else {
            xmlTemplateDocument.setData("limitation", "");
        }

        if (cancelUpload) {
            // the resource to upload exists, the user choosed
            // cancel in the warning dialogue... clear the session
            // values now.
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_FILECONTENT);
            session.removeValue(C_PARA_NEWTYPE);
            session.removeValue(C_PARA_TITLE);
            session.removeValue(CmsNewResourceUpload.C_PARAM_OVERWRITE);
        }
        
        if (DEBUG > 0) {
            System.out.println("\nstep: " + step);
            System.out.println("filename: " + filename);
            System.out.println("currentFolder: " + currentFolder);
            System.out.println("title: " + title);
            System.out.println("newtype: " + newtype);
            System.out.println("overwrite: " + replaceResource);
            System.out.println("cancel: " + cancelUpload);
        }                

       

        // now we have to check if we use the applet upload or the old one
        // get the startsettings for the user
        CmsUser user=cms.getRequestContext().currentUser();
        Hashtable startsettings =(Hashtable)user.getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);     
         
        if (startsettings !=null) {
            // test if the user has selected the applet dialog    
            appletMode = (String)startsettings.get(C_START_UPLOADAPPLET);
        } 
        
        if (appletMode!=null && appletMode.equals("on")) {
            // we are in applet mode, so display the applet
            xmlTemplateDocument.setData("applet",this.createAppletCode(cms,currentFolder));
            xmlTemplateDocument.setData("uploadform",xmlTemplateDocument.getProcessedDataValue(("appletform")));
        } else {
            // display the old upload dialog
            xmlTemplateDocument.setData("uploadform",xmlTemplateDocument.getProcessedDataValue(("oldform"),this));

        }

        if (error!=null) {
            template = "error";
            xmlTemplateDocument.setData("details", error);
        } else 
                
        // there was a file uploaded, so select its type
        if(step != null ) {
            if(step.equals("1")) {
                
                 // display the select filetype screen
                if(filename != null) {

                    // check if the file size is 0
                    if(filecontent.length == 0) {
                        template = "error";
                        xmlTemplateDocument.setData("details", filename);
                    }
                    
                    // check if the file size is larger than the maximum allowed upload size 
                    else if ((maxFileSize > 0) && (filecontent.length > (maxFileSize * 1024))) {
                        template = "errorfilesize";
                        xmlTemplateDocument.setData("details", filename+": "+(filecontent.length/1024)+" kb, max. "+maxFileSize+" kb.");
                    }                                                        
                    else {
                        if(unzip != null) {
                            // try to unzip the file here ...
                            boolean noSubFolder = (nofolder != null ? true : false);
                            CmsImportFolder zip = new CmsImportFolder(
                                filecontent, currentFolder, cms, noSubFolder);
                            if( zip.isValidZipFile() ) {

                                // remove the values form the session
                                session.removeValue(C_PARA_RESOURCE);
                                session.removeValue(C_PARA_FILECONTENT);
                                session.removeValue(C_PARA_NEWTYPE);
                                session.removeValue(C_PARA_TITLE);
                                session.removeValue(CmsNewResourceUpload.C_PARAM_OVERWRITE);
                                
                                // return to the filelist
                                try {
                                    //cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                                    if((lastUrl != null) && (lastUrl != "")) {
                                        cms.getRequestContext().getResponse().sendRedirect(lastUrl);
                                    }
                                    else {
                                        cms.getRequestContext().getResponse().sendCmsRedirect(
                                            getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
                                    }
                                } catch(Exception ex) {
                                    throw new CmsException(
                                        "Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                                        + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                                }
                                return null;
                            }
                        } // else, zip was not valid, so continue ...
                        template = "step1";
                    }
                }
            }
            else {
                if(step.equals("2")) {

                    // get the selected resource and check if it is an image
                    int type = cms.getResourceTypeId(newtype);
                    if(newtype.equals(CmsResourceTypeImage.C_RESOURCE_TYPE_NAME)) {
                        // check if the resource already exists to get its title
                        try {
                            String oldTitle = cms.readProperty( currentFolder + filename, I_CmsConstants.C_PROPERTY_TITLE );
                            
                            if (oldTitle!=null) {
                                xmlTemplateDocument.setData("TITLE", CmsEncoder.escapeXml(oldTitle));
                            }
                        }
                        catch (CmsException e) {
                            // the resource doesn't exists yet...
                            xmlTemplateDocument.setData("TITLE", "");
                        }                        

                        // the file type is an image
                        template = "image";
                        xmlTemplateDocument.setData("MIME", filename);
                        xmlTemplateDocument.setData("SIZE", "Not yet available");
                        xmlTemplateDocument.setData("FILESIZE", new Integer(filecontent.length).toString() + " Bytes");
                    }
                    else {

                        // create the new file.
                        try {
                            cms.createResource(currentFolder + filename, type, new Hashtable(), filecontent, null);
                        }
                        catch (CmsException e) {
                            if (replaceResource) {
                                cms.lockResource(currentFolder + filename, true);
                                cms.replaceResource(currentFolder + filename, type, null, filecontent);
                                //cms.unlockResource( currentFolder + filename );
                                session.putValue(CmsNewResourceUpload.C_PARAM_OVERWRITE, "no");
                            }
                            else {
                                xmlTemplateDocument.setData("LAST_STEP", step);
                                xmlTemplateDocument.setData("FILENAME", filename);
                                return startProcessing(cms, xmlTemplateDocument, "", parameters, "overwrite");
                            }
                        }

                        // remove the values form the session
                        session.removeValue(C_PARA_RESOURCE);
                        session.removeValue(C_PARA_FILECONTENT);
                        session.removeValue(C_PARA_NEWTYPE);
                        session.removeValue(C_PARA_TITLE);
                        session.removeValue(CmsNewResourceUpload.C_PARAM_OVERWRITE);

                        // return to the filelist
                        try {

                            cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+CmsWorkplaceAction.getExplorerFileUri(cms));
//                            if((lastUrl != null) && (lastUrl != "")) {
//                                cms.getRequestContext().getResponse().sendRedirect(lastUrl);
//                            }
//                            else {
//                                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
//                            }
                        }
                        catch(Exception ex) {
                            throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                        }
                        return null;
                    }
                }
                else {
                    if(step.equals("3")) {

                        // get the data from the special image upload dialog

                        // check if a new filename is given
                        if(newname != null) {
                            filename = newname;
                        }

                        // create the new file.

                        // todo: error handling if file already exits
                        int type = cms.getResourceTypeId(newtype);
                        Hashtable prop = new Hashtable();
                        // check if a file title was given
                        if (title != null) {
                            prop.put(C_PROPERTY_TITLE, title);
                        }

                        try {
                            cms.createResource(currentFolder + filename, type, prop, filecontent, null);
                        }
                        catch (CmsException e) {
                            if (replaceResource) {
                                cms.lockResource(currentFolder + filename, true);
                                cms.replaceResource(currentFolder + filename, type, prop, filecontent);
                                //cms.unlockResource( currentFolder + filename );
                                session.putValue(CmsNewResourceUpload.C_PARAM_OVERWRITE, "no");
                            }
                            else {
                                xmlTemplateDocument.setData("LAST_STEP", step);
                                xmlTemplateDocument.setData("FILENAME", filename);
                                return startProcessing(cms, xmlTemplateDocument, "", parameters, "overwrite");
                            }
                        }

                        // remove the values form the session
                        session.removeValue(C_PARA_RESOURCE);
                        session.removeValue(C_PARA_FILECONTENT);
                        session.removeValue(C_PARA_NEWTYPE);
                        session.removeValue(C_PARA_TITLE);
                        session.removeValue(CmsNewResourceUpload.C_PARAM_OVERWRITE);
                        session.removeValue("lasturl");

                        // return to the filelist
                        try {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
//                            if((lastUrl != null) && (lastUrl != "")) {
//                                cms.getRequestContext().getResponse().sendRedirect(lastUrl);
//                            }
//                            else {
//                                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
//                            }
                        }
                        catch(Exception ex) {
                            throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                        }
                        return null;
                    }
                }
            }
        }
        if(filename != null) {
            xmlTemplateDocument.setData("FILENAME", filename);
        }

        } catch (CmsException generalException) {

        try {            
            // send an error header if in applet mode
            if (appletMode!=null && appletMode.equals("on")) {
                cms.getRequestContext().getResponse().sendError(409, generalException.toString());
            }
        } catch (Exception e) {
           
        }            
            throw generalException;
        }
    

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }


    /**
     * Gets the resources displayed in the Radiobutton group on the chtype dialog.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @param descriptions Description that will be displayed for the new resource.
     * @return The vectors names and values are filled with the information found in the workplace.ini.
     * @return the number of the preselected item, -1 if none preselected
     * @throws Throws CmsException if something goes wrong.
     */
    public int getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Vector descriptions, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_RESOURCE);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        suffix = suffix.toLowerCase(); // file extension of filename

        // read the known file extensions from the database
        Hashtable extensions = cms.readFileExtensions();
        String resType = new String();
        if(extensions != null) {
            resType = (String)extensions.get(suffix);
        }
        if(resType == null) {
            resType = "";
        }
        int ret = 0;

        // Check if the list of available resources is not yet loaded from the workplace.ini
        if(m_names == null || m_values == null) {
            m_names = new Vector();
            m_values = new Vector();
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            configFile.getWorkplaceIniData(m_names, m_values, "RESOURCETYPES", "RESOURCE");
        }

        // Check if the temportary name and value vectors are not initialized, create
        // them if nescessary.
        if(names == null) {
            names = new Vector();
        }
        if(values == null) {
            values = new Vector();
        }
        if(descriptions == null) {
            descriptions = new Vector();
        }

        // OK. Now m_names and m_values contain all available
        // resource information.
        // Loop through the vectors and fill the result vectors.
        int numViews = m_names.size();
        for(int i = 0;i < numViews;i++) {
            String loopValue = (String)m_values.elementAt(i);
            String loopName = (String)m_names.elementAt(i);
            values.addElement(loopValue);
            names.addElement("file_" + loopName);
            String descr;
            if(lang != null) {
                descr = lang.getLanguageValue("fileicon." + loopName);
            }
            else {
                descr = loopName;
            }
            descriptions.addElement(descr);
            if(resType.equals(loopName)) {

                // known file extension
                ret = i;
            }
        }
        return ret;
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
    
    
    /**
     * Returns the path to the currently selected skin.<p>
     * 
     * @return the path to the currently selected skin
     */
    public String getSkinUri(CmsObject cms) {
        return cms.getRequestContext().getRequest().getWebAppUrl() + "/skins/modern/";        
    }
    

    /**
     * Creates the HTML code of the file upload applet with all required parameters.<p>
     * @param cms the current CmsObject
     * @return string containing the applet HTML code
     */
    private String createAppletCode(CmsObject cms, String currentFolder) throws CmsException {
       
        StringBuffer applet=new StringBuffer();
        //collect some required server data first

        String scheme=cms.getRequestContext().getRequest().getScheme();
        String host=cms.getRequestContext().getRequest().getServerName();
        String path=cms.getRequestContext().getRequest().getServletUrl();
        String webapp=cms.getRequestContext().getRequest().getWebAppUrl();
        int port=cms.getRequestContext().getRequest().getServerPort();
        String fileExtensions=new String("");
        
        //get all file extensions
        Hashtable extensions = cms.readFileExtensions();
        Enumeration keys = extensions.keys();
        while (keys.hasMoreElements()) {
            String key=(String)keys.nextElement();
            String value=(String)extensions.get(key);
            fileExtensions+=key+"="+value+",";           
        }
        fileExtensions=fileExtensions.substring(0,fileExtensions.length()-1);
        
        //get the file size upload limitation value (value is in kB)
        int maxFileSize = OpenCms.getWorkplaceManager().getFileMaxUploadSize();                          
          
        // define the required colors.
        // currently this is hard coded here
        
        String colors="bgColor=#C0C0C0,outerBorderRightBottom=#333333,outerBorderLeftTop=#C0C0C0";
        colors+=",innerBorderRightBottom=#777777,innerBorderLeftTop=#F0F0F0";  
        colors+=",bgHeadline=#000066,colorHeadline=#FFFFFF";
        colors+=",colorText=#000000,progessBar=#E10050";
          
                       
        // get the workplace settings
        HttpSession session = null;
        HttpServletRequest request = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
        session = request.getSession(false);  
        if (session != null) {
            CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute("__CmsWorkplace.WORKPLACE_SETTINGS");        
            if (settings!=null) {
                CmsWorkplaceMessages messages=settings.getMessages();                 
                    
            
                
                applet.append("<applet code=\"org.opencms.applet.upload.FileUploadApplet.class\" archive=\"");
                applet.append(webapp);
                applet.append("/skins/components/upload/applet/upload.jar\" width=\"500\" height=\"100\">\n");                
                applet.append("<param name=\"opencms\" value=\"");
                applet.append(scheme);
                applet.append("://");
                applet.append(host);
                applet.append(":");
                applet.append(port);
                applet.append(getSkinUri(cms));
                applet.append("filetypes/\">\n");
                applet.append("<param name=\"target\" value=\"");
                applet.append(scheme);
                applet.append("://");
                applet.append(host);
                applet.append(":");
                applet.append(port);
                applet.append(path);
                applet.append("/system/workplace/action/explorer_files_new_upload.html\">\n");
                applet.append("<param name=\"redirect\" value=\"");
                applet.append(scheme);
                applet.append("://");
                applet.append(host);
                applet.append(":");
                applet.append(port);
                applet.append(path);
                applet.append("/system/workplace/jsp/explorer_files.html\">\n");
                applet.append("<param name=error value=\"");
                applet.append(scheme);
                applet.append("://");
                applet.append(host);
                applet.append(":");
                applet.append(port);
                applet.append(path);
                applet.append("/system/workplace/action/explorer_files_new_upload.html\">\n");
                applet.append("<param name=\"browserCookie\" value=\"JSESSIONID=");
                applet.append(cms.getRequestContext().getSession(true).getId());
                applet.append("\">\n");
                applet.append("<param name=\"filelist\" value=\"");
                applet.append(currentFolder);
                applet.append("\">\n");
                applet.append("<param name=\"colors\" value=\"");
                applet.append(colors);
                applet.append("\">\n");                
                applet.append("<param name=\"fileExtensions\" value=\"");
                applet.append(fileExtensions);
                applet.append("\">\n\n");
                applet.append("<param name=\"maxsize\" value=\"");
                applet.append(maxFileSize);
                applet.append("\">\n");
                applet.append("<param name=\"actionOutputSelect\" value=\"");
                applet.append(messages.key("uploadapplet.action.select"));
                applet.append("\">\n");
                applet.append("<param name=\"actionOutputCount\"value=\"");
                applet.append(messages.key("uploadapplet.action.count"));
                applet.append("\">\n");
                applet.append("<param name=\"actionOutputCreate\" value=\"");
                applet.append(messages.key("uploadapplet.action.create"));
                applet.append("\">\n");
                applet.append("<param name=\"actionOutputUpload\" value=\"");
                applet.append(messages.key("uploadapplet.action.upload"));
                applet.append("\">\n");
                applet.append("<param name=\"messageOutputUpload\" value=\"");
                applet.append(messages.key("uploadapplet.message.upload"));
                applet.append("\">\n");
                applet.append("<param name=\"messageOutputErrorZip\" value=\"");
                applet.append(messages.key("uploadapplet.message.error.zip"));
                applet.append("\">\n");
                applet.append("<param name=\"messageOutputErrorSize\" value=\"");
                applet.append(messages.key("uploadapplet.message.error.size"));
                applet.append("\">\n");
                applet.append("<param name=\"messageNoPreview\" value=\"");
                applet.append(messages.key("uploadapplet.message.nopreview"));
                applet.append("\">\n");
                applet.append("<param name=\"messageOutputAdding\" value=\"");
                applet.append(messages.key("uploadapplet.message.adding"));
                applet.append(" \">\n");
                applet.append("<param name=\"errorTitle\" value=\"");
                applet.append(messages.key("uploadapplet.error.title"));
                applet.append(" \">\n");
                applet.append("<param name=\"errorLine1\" value=\"");
                applet.append(messages.key("uploadapplet.error.line1"));
                applet.append(" \">\n");
                applet.append("</applet>\n");
            }
        }
        return applet.toString();
        
    }


    
}
