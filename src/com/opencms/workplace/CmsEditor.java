/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsEditor.java,v $
* Date   : $Date: 2004/02/22 13:52:26 $
* Version: $Revision: 1.67 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlTemplateFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

/**
 * Template class for displaying the text editor of the OpenCms workplace.<P>
 * Reads the edirtor layout from a editor template file of the content type
 * <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.67 $ $Date: 2004/02/22 13:52:26 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsEditor extends CmsWorkplaceDefault {

    /**
     * Get the name of the section that should be loaded from the editor's
     * template file for displaying the editor.
     * MS IE and Netscape Navigator use different ways to display
     * the text editor, so we must distinguish here.
     * @param cms cms object for accessing the original HTTP request
     * @param templateFile the editor's template file containing different sections
     * @return name of the browser specific section in <code>templateFile</code>
     */
    protected String getBrowserSpecificSection(CmsObject cms, CmsXmlTemplateFile templateFile, Hashtable parameters) {
        HttpServletRequest orgReq = (HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest();
        String browser = orgReq.getHeader("user-agent");
        String result = null;
        // if the following parameter is given and MSIE is used,
        // the template "ns" must be selected because there might be no ActiveX control
        String noactivex = (String)parameters.get("noactivex");

        // check browser and return a valid template selector
        if(browser.indexOf("MSIE") > -1) {
            if(templateFile.hasSection("ie")) {
                if(!"true".equalsIgnoreCase(noactivex)){
                    result = "ie";
                } else {
                    // there is no ActiveX Control so use the textarea
                    if(templateFile.hasSection("ns")) {
                        result = "ns";
                    }
                }
            }
        }
        else {
            if(templateFile.hasSection("ns")) {
                result = "ns";
            }
        }
        return result;
    }

    /**
     * Displays the editor described by the template file <code>templateFile</code>.
     * This can be either the HTML editor or the text editor.
     * <p>
     * The given template file will be scanned for special section "ie" and "ns"
     * that can be used to generate browser specific versions of the editors
     * (MS IE or Netscape Navigator). If no such section exists, the default
     * section will be displayed.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String saveerror = "";
        // Get all editor parameters
        String file = (String)parameters.get(C_PARA_RESOURCE);
        // try to get the value from the session because we might come from the error page
        if((file == null) || ("".equals(file))){
            file = (String)session.getValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_RESOURCE);
        }        
        if((file != null) && (! "".equals(file))) {
            session.putValue("te_file", file);
        }
        String content = (String)parameters.get(C_PARA_CONTENT);
        // try to get the value from the session because we might come from the error page
        if((content == null) || ("".equals(content))){
            content = (String)session.getValue(C_PARA_CONTENT);
            if(content != null){
                parameters.put(C_PARA_CONTENT, content);
            }
            session.removeValue(C_PARA_CONTENT);
        }
        String action = (String)parameters.get(C_PARA_ACTION);
        String jsfile = (String)parameters.get(C_ROOT_TEMPLATE_NAME + "." + C_PARA_JSFILE);
        // try to get the value from the session because we might come from the error page
        if((jsfile == null) || ("".equals(jsfile))){
            jsfile = (String)session.getValue(C_PARA_JSFILE);
            session.removeValue(C_PARA_JSFILE);
        }
        String editorframe = (String)parameters.get("root.editorframe");
        if((editorframe == null) || ("".equals(editorframe))){
            editorframe = (String)session.getValue("editorframe");
            session.removeValue("editorframe");
        }

        boolean checkit = false;
        boolean existsFileParam = ((file != null) && (!"".equals(file)));
        boolean saveRequested = ((action != null) && (C_EDIT_ACTION_SAVE.equals(action)
                || C_EDIT_ACTION_SAVEEXIT.equals(action)));
        boolean exitRequested = ((action != null) && (C_EDIT_ACTION_EXIT.equals(action)
                || C_EDIT_ACTION_SAVEEXIT.equals(action)));

        // CmsFile object of the file to be edited
        CmsFile editFile = null;
        CmsLock lock = null;

        // If there is a file parameter and no content, try to read the file.
        // If the user requested a "save file", also load the file.
        if (existsFileParam && (content == null || saveRequested)) {
            editFile = readFile(cms, file);
            lock = cms.getLock(file);
            
            // block any editing immediately in case of insuffient locks
            if (lock.isNullLock()) {
                // check the autolock resource setting and lock the resource if necessary
                if (OpenCms.getWorkplaceManager().autoLockResources()) {
                    // resource is not locked, lock it automatically
                    cms.lockResource(file);  
                    lock = cms.getLock(file);
                } else {
                    throw new CmsLockException("Resource is not locked", CmsLockException.C_RESOURCE_UNLOCKED);
                }
            }
            if (lock.getType() == CmsLock.C_TYPE_INHERITED) {
                cms.lockResource(file);
                lock = cms.getLock(file);
            }
            if (lock.getType() != CmsLock.C_TYPE_EXCLUSIVE) {
                throw new CmsLockException("Insufficient lock to edit content of resource", CmsLockException.C_RESOURCE_LOCKED_NON_EXCLUSIVE);
            }
            if (!lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
                throw new CmsLockException("Resource locked by another user", CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER);
            }
            
            checkit = true;

            // Read file encoding from the property of the file 
            String encoding = cms.getRequestContext().getEncoding();
            encoding = cms.readProperty(file, C_PROPERTY_CONTENT_ENCODING, true, encoding);

            // If there is no content set, this is the first request of the editor.
            // So load the file content and set the "content" parameter.
            if(content == null) {
                try {
                    content = new String(editFile.getContents(), encoding);
                } catch (UnsupportedEncodingException e) {
                    content = new String(editFile.getContents());
                }
                content = CmsEncoder.escapeWBlanks(content, CmsEncoder.C_UTF8_ENCODING);
                parameters.put(C_PARA_CONTENT, content);
            }

            // If the user requested a file save, write the file content
            // back to the database.
            if(saveRequested) {
                try{
                    String decodedContent = CmsEncoder.unescape(content, CmsEncoder.C_UTF8_ENCODING);
                    try {
                        editFile.setContents(decodedContent.getBytes(encoding));
                    } catch (UnsupportedEncodingException e) {
                        editFile.setContents(decodedContent.getBytes());
                    }
                    cms.writeFile(editFile);
                } catch (CmsException e){
                    saveerror = e.getShortException();
                }
            }
        }

        // Check if we should leave th editor instead of start processing
        if(exitRequested && ((saveerror == null) || ("".equals(saveerror)))) {
            try {
                CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(CmsWorkplaceAction.getWorkplaceUri(CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()));
            } catch(IOException e) {
                throwException("Could not send redirect to workplace main screen.", e);
            }

            //return "".getBytes();
            return null;
        }

        // Load the template file and get the browser specific section name
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        String sectionName = getBrowserSpecificSection(cms, xmlTemplateDocument, parameters);
        
        // Put the "file" datablock for processing in the template file.
        // It will be inserted in a hidden input field and given back when submitting.
        xmlTemplateDocument.setData(C_PARA_RESOURCE, file);
        xmlTemplateDocument.setData(C_PARA_JSFILE, jsfile);
        xmlTemplateDocument.setData("editorframe", editorframe);
        xmlTemplateDocument.setData("OpenCmsContext", OpenCms.getSystemInfo().getOpenCmsContext());
        // Announcement of path and file name in the header of the browser.
        if(checkit==true){
            xmlTemplateDocument.setData("fileName", editFile.getName());
            String parent = CmsResource.getParentFolder(cms.readAbsolutePath(editFile));
            xmlTemplateDocument.setData("pathName", parent);
        }
        String lasturlname = null;
        if(!"".equals(saveerror)){
            if(file != null){
                session.putValue(C_PARA_RESOURCE, file);
            }
            if(content != null){
                session.putValue(C_PARA_CONTENT, content);
            }
            if(jsfile != null){
                session.putValue(C_PARA_JSFILE, jsfile);
            }
            if(editorframe != null){
                session.putValue("editorframe", editorframe);
            }
            sectionName = "errorsave";
            xmlTemplateDocument.setData("errordetail", saveerror);
            lasturlname=(String)parameters.get("editor._TEMPLATE_");
            if (lasturlname != null){
                lasturlname=lasturlname.substring(lasturlname.lastIndexOf("/")+1, lasturlname.length());
            }
            xmlTemplateDocument.setData("errorlasturl", lasturlname+".html");
        }
        
        // test if the "help"- button has to be displayed for the user's current language
        String userLanguage = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        xmlTemplateDocument.setData("LOCALE", "" + userLanguage);

        try {
            cms.readFolder(C_VFS_PATH_HELP + userLanguage);
            // the localized help- folder exists
            xmlTemplateDocument.setData("HELP", xmlTemplateDocument.getProcessedDataValue("HELP_ENABLED", this));
        }
        catch (CmsException e) {
            // the localized help- folder does not exist
            try {
                xmlTemplateDocument.setData("HELP", xmlTemplateDocument.getProcessedDataValue("HELP_DISABLED", this));
            }
            catch (Exception ex) {
                // ignore this error so the workplace can still be used
            }
        }
                
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, sectionName);
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
     * Reads in the requested file to be edited by calling the corresponding
     * method in the cms object.
     * @param cms Cms object for accessing system resources
     * @param filename Name of the file to be loaded
     * @return CmsFile object of the loaded file
     */
    protected CmsFile readFile(CmsObject cms, String filename) throws CmsException {
        CmsFile result = null;
        try {
            result = cms.readFile(filename);
        }
        catch(Exception e) {

            // Anything is wrong. Perhaps a wrong file name ???
            String errorMessage = "Error while reading file " + filename + ": " + e;
            if(OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(errorMessage, e);
            }

            // throw this exception again, so it can be displayed in the servlet.
            if(e instanceof CmsException) {
                throw (CmsException)e;
            }
            else {
                throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        return result;
    }

    /**
     * User method for setting the editable text in the editor window.
     * <P>
     * This method can be called in the editor's template file using
     * <code>&lt;METHOD name="setText"/&gt></code>. This call will be replaced
     * by the content of the file that should be edited.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of the file that should be edited.
     */
    public Object setText(CmsObject cms, String tagcontent, A_CmsXmlContent doc,
            Object userObj) {
        Hashtable parameters = (Hashtable)userObj;
        String content = (String)parameters.get(C_PARA_CONTENT);
        if(content == null) {
            content = "";
        }
        return content;
    }
}
