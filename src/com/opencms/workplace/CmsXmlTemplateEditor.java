/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlTemplateEditor.java,v $
* Date   : $Date: 2005/02/18 15:18:50 $
* Version: $Revision: 1.145 $
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
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsTemplateClassManager;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

/**
 * Template class for displaying the XML template editor of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.145 $ $Date: 2005/02/18 15:18:50 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsXmlTemplateEditor extends CmsWorkplaceDefault {

    protected void commitTemporaryFile(CmsObject cms, String originalFilename, String temporaryFilename,
                   int tempProject, int curProject) throws CmsException {
        // set current project to tempfileproject
        cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
        CmsFile tempFile = cms.readFile(temporaryFilename, CmsResourceFilter.IGNORE_EXPIRATION);
        Map minfos = cms.readProperties(temporaryFilename);
        // set current project
        cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
        CmsFile orgFile = cms.readFile(originalFilename, CmsResourceFilter.IGNORE_EXPIRATION);
        orgFile.setContents(tempFile.getContents());
        cms.writeFile(orgFile);
        Iterator keys = minfos.keySet().iterator();
        while(keys.hasNext()) {
            String keyName = (String)keys.next();
            cms.writeProperty(originalFilename, keyName, (String)minfos.get(keyName));
        }
        // don't forget to clear the cache.
        A_CmsXmlContent.clearFileCache(cms.getRequestContext().currentProject().getName()+":"
                        +  cms.getRequestContext().addSiteRoot(originalFilename));
    }

    protected String createTemporaryFile(CmsObject cms, CmsResource file, int tempProject, int curProject) throws CmsException {
        String temporaryFilename = CmsResource.getFolderPath(cms.getSitePath(file)) + C_TEMP_PREFIX + file.getName();
        boolean ok = true;
        
        cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
        
        try {
            cms.copyResource(cms.getSitePath(file), temporaryFilename, I_CmsConstants.C_COPY_AS_NEW);
            // cms.chmod(temporaryFilename, 91);
        } catch (CmsException e) {
            if ((e.getType() == CmsException.C_FILE_EXISTS) || (e.getType() != CmsException.C_SQL_ERROR)) {
                try {
                    // try to re-use the old temporary file
                    cms.changeLastModifiedProjectId(temporaryFilename);
                    cms.lockResource(temporaryFilename);
                    // cms.chmod(temporaryFilename, 91);
                } catch (Exception ex) {
                    ok = false;
                }
            } else {
                throw e;
            }
        }
        
        String extendedTempFile = temporaryFilename;

        int loop = 0;
        while (!ok) {
            ok = true;
            extendedTempFile = temporaryFilename + loop;
            
            try {
                cms.copyResource(cms.getSitePath(file), extendedTempFile);
                // cms.chmod(extendedTempFile, 91);
            } catch (CmsException e) {
                if ((e.getType() != CmsException.C_FILE_EXISTS) && (e.getType() != CmsException.C_SQL_ERROR)) {
                    cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
                    // This was not a file-exists-exception.
                    // Very bad. We should not continue here since we may run
                    // into an endless looooooooooooooooooooooooooooooooooop.
                    throw e;
                }

                // temp file could not be created
                loop++;
                ok = false;
            }
        }

        cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
        // Oh how lucky we are! We have found a temporary file!
        temporaryFilename = extendedTempFile;
        
        return temporaryFilename;
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

    public Integer getAvailableTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        Integer retValue = CmsHelperMastertemplates.getTemplates(cms, names, values, (String)parameters.get("template"), -1);
        if(retValue.intValue() != -1) {
            return retValue;
        } else {
            // no template found -> use the given one
            // first clean the vectors
            names.removeAllElements();
            values.removeAllElements();
            // now add the current template
            String name = (String)parameters.get("template");
            try { // to read the title of this template
                name = cms.readProperty(name, C_PROPERTY_TITLE);
            } catch(CmsException exc) {
                // ignore this exception - the title for this template was not readable
            }
            names.add(name);
            values.add(parameters.get("template"));
            return new Integer(0);
        }
    }

    /**
     * Gets all views available in the workplace screen.
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
     * @return Index representing the user's current workplace view in the vectors.
     * @throws CmsException
     */

    public Integer getBodys(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String currentBodySection = (String)parameters.get("body");
        String bodyClassName = (String)parameters.get("bodyclass");
        String tempBodyFilename = (String)session.getValue("te_tempbodyfile");
        Object tempObj = CmsTemplateClassManager.getClassInstance(bodyClassName);
        CmsXmlTemplate bodyElementClassObject = (CmsXmlTemplate)tempObj;
        CmsXmlTemplateFile bodyTemplateFile = bodyElementClassObject.getOwnTemplateFile(cms,
                tempBodyFilename, C_BODY_ELEMENT, parameters, null);
        Vector allBodys = bodyTemplateFile.getAllSections();
        int loop = 0;
        int currentBodySectionIndex = 0;
        int numBodys = allBodys.size();
        for(int i = 0;i < numBodys;i++) {
            String bodyname = (String)allBodys.elementAt(i);
            String encodedBodyname = CmsEncoder.escapeXml(bodyname);
            if(bodyname.equals(currentBodySection)) {
                currentBodySectionIndex = loop;
            }
            values.addElement(encodedBodyname);
            names.addElement(encodedBodyname);
            loop++;
        }
        return new Integer(currentBodySectionIndex);
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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = CmsXmlTemplateLoader.getRequest(reqCont).getOriginalRequest();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);

        // get the temporary file projectid
        int tempProject = 0;
        try{
            tempProject = OpenCms.getWorkplaceManager().getTempFileProjectId();
        }catch(Exception e){
            throw new CmsException("Can not read projectId of tempfileproject for creating temporary file for editing! "+e.toString());
        }
        if(tempProject == 0){
            throw new CmsException("Can not create temporary file for editing!");
        }
        int curProject = cms.getRequestContext().currentProject().getId();

        // Get the user's browser
        String browser = orgReq.getHeader("user-agent");
        String hostName = orgReq.getScheme() + "://" + orgReq.getServerName() + ":" + orgReq.getServerPort();

        // Get all URL parameters
        String content = (String)parameters.get(C_PARA_CONTENT);        
        if(content == null){
            // try to get the value from the session because we might come from errorpage
            content = (String)session.getValue(C_PARA_CONTENT);
            session.removeValue(C_PARA_CONTENT);
        }
        
        String body = (String)parameters.get("body");
        if(body == null){
            // try to get the value from the session because we might come from errorpage
            body = (String)session.getValue("body");
            session.removeValue("body");
        }
        String file = (String)parameters.get(C_PARA_RESOURCE);
        if(file == null){
            // try to get the value from the session because we might come from errorpage
            file = (String)session.getValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_RESOURCE);
        }
        String editor = (String)parameters.get("editor");
        if((editor == null) || "".equals(editor)){
            // try to get the value from the session because we might come from errorpage
            editor = (String)session.getValue("editor");
            if(editor != null){
                parameters.put("editor", editor);
            }
            session.removeValue("editor");
        }
        String title = (String)parameters.get(C_PARA_TITLE);
        if(title == null){
            // try to get the value from the session because we might come from errorpage
            title = (String)session.getValue(C_PARA_TITLE);
            session.removeValue(C_PARA_TITLE);
        }
        String bodytitle = (String)parameters.get("bodytitle");
        if(bodytitle == null){
            // try to get the value from the session because we might come from errorpage
            bodytitle = (String)session.getValue("bodytitle");
            session.removeValue("bodytitle");
        }
        String layoutTemplateFilename = (String)parameters.get("template");
        if(layoutTemplateFilename == null){
            // try to get the value from the session because we might come from errorpage
            layoutTemplateFilename = (String)session.getValue("template");
            session.removeValue("template");
        }
        String layoutTemplatFilenameRelative = layoutTemplateFilename;
        layoutTemplateFilename = CmsLinkManager.getAbsoluteUri(layoutTemplateFilename, file);
        String bodyElementClassName = (String)parameters.get("bodyclass");
        if(bodyElementClassName == null){
            // try to get the value from the session because we might come from errorpage
            bodyElementClassName = (String)session.getValue("bodyclass");
            session.removeValue("bodyclass");
        }
        String bodyElementFilename = (String)parameters.get("bodyfile");
        if(bodyElementFilename == null){
            // try to get the value from the session because we might come from errorpage
            bodyElementFilename = (String)session.getValue("bodyfile");
            session.removeValue("bodyfile");
        }
        String action = (String)parameters.get(C_PARA_ACTION);

        String startView = (String)parameters.get("startview");
        if((startView == null) || ("".equals(startView))){
            // try to get the value from the session because we might come from errorpage
            startView = (String)session.getValue("startview");
            session.removeValue("startview");
        }

        // Get all session parameters
        String oldEdit = (String)session.getValue("te_oldedit");
        String oldLayoutFilename = (String)session.getValue("te_oldlayout");
        String oldTitle = (String)session.getValue("te_title");
        String oldBody = (String)session.getValue("te_oldbody");
        String oldBodytitle = (String)session.getValue("te_oldbodytitle");
        String layoutTemplateClassName = (String)session.getValue("te_templateclass");
        String tempPageFilename = (String)session.getValue("te_temppagefile");
        String tempBodyFilename = (String)session.getValue("te_tempbodyfile");
        String style = (String)session.getValue("te_stylesheet");

        boolean existsContentParam = content != null;
        boolean existsFileParam = (file != null && (!"".equals(file)));
        boolean saveRequested = ((action != null) && (C_EDIT_ACTION_SAVE.equals(action) || C_EDIT_ACTION_SAVEEXIT.equals(action)));
        boolean exitRequested = ((action != null) && (C_EDIT_ACTION_EXIT.equals(action) || C_EDIT_ACTION_SAVEEXIT.equals(action)));
        boolean bodychangeRequested = ((oldBody != null) && (body != null) && (!(oldBody.equals(body))));
        boolean templatechangeRequested = (oldLayoutFilename != null && layoutTemplateFilename != null
               && (!(oldLayoutFilename.equals(layoutTemplateFilename))));
        boolean titlechangeRequested = (oldTitle != null && title != null && (!(oldTitle.equals(title))));
        boolean newbodyRequested = ((action != null) && "newbody".equals(action));
        boolean previewRequested = ((action != null) && "preview".equals(action));
        boolean bodytitlechangeRequested = (oldBodytitle != null && bodytitle != null && (!(oldBodytitle.equals(bodytitle))));

        String saveerror = "";

        // Check if there is a file parameter in the request
        if(!existsFileParam) {
            throwException("No \"file\" parameter given. Don't know which file should be edited.");
        }

        // If there is no content parameter this seems to be
        // a new request of the page editor.
        // So we have to read all files and set some initial values.
        parameters.put("root.pagetype", OpenCms.getResourceManager().getResourceType(cms.readResource(file).getTypeId()).getTypeName());
        parameters.put("filename_for_relative_template", file);
        
        // Simple page support
        String templateProp = cms.readProperty(file, C_PROPERTY_TEMPLATE);
        boolean isSimplePage = (templateProp != null);

        // Check, if the selected page file is locked
        CmsResource pageFileResource = cms.readResource(file);
        CmsLock lock = cms.getLock(file);
        if(lock.isNullLock()) {
            cms.lockResource(file);
        }
                                        
        if(!existsContentParam) {
            
            if (isSimplePage) {
                
                bodyElementClassName = C_XML_CONTROL_DEFAULT_CLASS;
                bodyElementFilename = file;
                layoutTemplateClassName = C_XML_CONTROL_DEFAULT_CLASS;
                layoutTemplateFilename = templateProp;
                layoutTemplatFilenameRelative = templateProp;
                
            } else {             
                
                CmsXmlControlFile originalControlFile = new CmsXmlControlFile(cms, file);
                if(originalControlFile.isElementClassDefined(C_BODY_ELEMENT)) {
                    bodyElementClassName = originalControlFile.getElementClass(C_BODY_ELEMENT);
                }
                if(originalControlFile.isElementTemplateDefined(C_BODY_ELEMENT)) {
                    bodyElementFilename = originalControlFile.getElementTemplate(C_BODY_ELEMENT);
                    bodyElementFilename = originalControlFile.validateBodyPath(cms, bodyElementFilename, cms.readResource(file));
                }
                if((bodyElementClassName == null) || (bodyElementFilename == null)) {
                    // Either the template class or the template file
                    // for the body element could not be determined.
                    // BUG: Send error here
                }

                // The content file must be locked before editing
                //CmsResource contentFileResource = cms.readFileHeader(bodyElementFilename);
                lock = cms.getLock(bodyElementFilename);
                if(lock.isNullLock()) {
                    cms.lockResource(bodyElementFilename);
                }
                // Now get the currently selected master template file
                layoutTemplateFilename = originalControlFile.getMasterTemplate();
                layoutTemplatFilenameRelative = layoutTemplateFilename;
                layoutTemplateFilename = CmsLinkManager.getAbsoluteUri(layoutTemplateFilename, originalControlFile.getAbsoluteFilename());
                layoutTemplateClassName = originalControlFile.getTemplateClass();            
            } 
            
            int browserId;
            if(browser.indexOf("MSIE") > -1) {
                browserId = 0;
            }else {
                browserId = 1;
            }

            if(editor == null || "".equals(editor)) {
                if(startView == null || "".equals(startView)){
                    editor = C_SELECTBOX_EDITORVIEWS[C_SELECTBOX_EDITORVIEWS_DEFAULT[browserId]];
                } else {
                    editor = startView;
                }
                session.putValue("te_pageeditor", editor);
                parameters.put("editor", editor);
            }

            // And finally the document title
            title = cms.readProperty(file, C_PROPERTY_TITLE);
            if(title == null) {
                title = "";
            }

            // Okay. All values are initialized. Now we can create
            // the temporary files.
            // if the parameter noactivex is set the temp file was already created,
            // so read the filename from the session
            String noactivex = (String)parameters.get("noactivex");
            
            if(noactivex == null || "".equals(noactivex.trim())){
                tempPageFilename = createTemporaryFile(cms, pageFileResource, tempProject, curProject);
            } else {
                tempPageFilename = (String)session.getValue("te_temppagefile");
            }
            
            cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
            if (isSimplePage) {
                tempBodyFilename = tempPageFilename;
            } else {
                tempBodyFilename = C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.length()-1) + tempPageFilename;
            }
            
            session.putValue("te_temppagefile", tempPageFilename);
            session.putValue("te_tempbodyfile", tempBodyFilename);
        }
        // Get the XML parsed content of the layout file.
        // This can be done by calling the getOwnTemplateFile() method of the
        // layout's template class.
        // The content is needed to determine the HTML style of the body element.
        Object tempObj = null;
        CmsXmlTemplateFile layoutTemplateFile = null;
        if (isSimplePage) {
            StringBuffer layoutBuffer = new StringBuffer(512);            
            layoutBuffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<xmltemplate>\n<stylesheet>");
            layoutBuffer.append("none");
            layoutBuffer.append("</stylesheet>\n<template><element name=\"jsptemplate\"/></template>\n<elementdef name=\"jsptemplate\">\n<class>com.opencms.flex.CmsJspTemplate</class>\n<template>");
            layoutBuffer.append(templateFile);
            layoutBuffer.append("</template>\n</elementdef>\n</xmltemplate>\n");
            layoutTemplateFile = new CmsXmlTemplateFile(cms, templateFile + C_XML_CONTROL_FILE_SUFFIX, layoutBuffer.toString());
        } else {
            tempObj = CmsTemplateClassManager.getClassInstance(layoutTemplateClassName);
            CmsXmlTemplate layoutTemplateClassObject = (CmsXmlTemplate)tempObj;
            layoutTemplateFile = layoutTemplateClassObject.getOwnTemplateFile(cms,
                    layoutTemplateFilename, null, parameters, null);
        }
        
        // Get the XML parsed content of the body file.
        // This can be done by calling the getOwnTemplateFile() method of the
        // body's template class.
        tempObj = CmsTemplateClassManager.getClassInstance(bodyElementClassName);
        CmsXmlTemplate bodyElementClassObject = (CmsXmlTemplate)tempObj;
        CmsXmlTemplateFile bodyTemplateFile = bodyElementClassObject.getOwnTemplateFile(cms,
                tempBodyFilename, C_BODY_ELEMENT, parameters, null);

        // Get the temporary page file object
        CmsXmlControlFile temporaryControlFile = null;
        if (! isSimplePage) temporaryControlFile = new CmsXmlControlFile(cms, tempPageFilename);
        if(!existsContentParam) {
            Vector allBodys = bodyTemplateFile.getAllSections();
            if(allBodys == null || allBodys.size() == 0) {
                body = "";
            }else {
                body = (String)allBodys.elementAt(0);
            }
            bodytitle = body.equals("(default)") ? "" : body;
            if (isSimplePage) {
                style = cms.readProperty(layoutTemplateFilename, C_PROPERTY_TEMPLATE);
                if (style != null) {
                    style =  hostName + OpenCms.getSystemInfo().getOpenCmsContext() + style;
                } else {
                    style = "";
                }
            } else {
                temporaryControlFile.setElementTemplSelector(C_BODY_ELEMENT, body);
                temporaryControlFile.setElementTemplate(C_BODY_ELEMENT, tempBodyFilename);
                // change the current project to temp file project
                cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
                temporaryControlFile.write();
                cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
                try {
                    style = getStylesheet(cms, null, layoutTemplateFile, null);
                } catch (Exception e) {
                    style = "";
                }
                if(style != null && !"".equals(style)) {
                    style = hostName + style;
                }                                   
            }         
            session.putValue("te_stylesheet", style);
        } else {
            // There exists a content parameter.
            // We have to check all possible changes requested by the user.
            if(titlechangeRequested) {
                // The user entered a new document title
                try {
                    cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
                    cms.writeProperty(tempPageFilename, C_PROPERTY_TITLE, title);
                    cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
                }catch(CmsException e) {
                    cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
                    if(OpenCms.getLog(this).isErrorEnabled() ) {
                        OpenCms.getLog(this).error("Could not write property " + C_PROPERTY_TITLE + " for file " + file, e);
                    }
                }
            }
            if(templatechangeRequested) {
                // The user requested a change of the layout template
                if (isSimplePage) {
                    cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
                    cms.writeProperty(tempPageFilename, C_PROPERTY_TEMPLATE, layoutTemplatFilenameRelative);
                    cms.getRequestContext().setCurrentProject(cms.readProject(curProject));         
                    style = cms.readProperty(layoutTemplateFilename, C_PROPERTY_TEMPLATE);    
                    if (style != null) {
                        style = hostName + OpenCms.getSystemInfo().getOpenCmsContext() + style;
                    } else {
                        style = "";
                    }
                } else { 
                    temporaryControlFile.setMasterTemplate(layoutTemplatFilenameRelative );
                    try {
                        style = getStylesheet(cms, null, layoutTemplateFile, null);
                    }catch(Exception e) {
                        style = "";
                    }
                    if(style != null && !"".equals(style)) {
                        style = hostName + style;
                    }   
                }             
                session.putValue("te_stylesheet", style);
            }
            if(bodytitlechangeRequested) {
                // The user entered a new title for the current body
                if((!oldBody.equals("(default)")) && (!oldBody.equals("script"))) {
                    if(bodytitle.toLowerCase().equals("script")) {
                        bodytitle = "script";
                    }
                    try {
                        bodyTemplateFile.renameSection(oldBody, bodytitle);
                        oldBody = bodytitle;
                        if(!bodychangeRequested) {
                            body = bodytitle;
                        }
                    }catch(Exception e) {
                        bodytitle = oldBodytitle;
                    }
                    if(bodytitle.equals("script")) {
                        session.putValue("te_pageeditor", editor);
                        editor = C_SELECTBOX_EDITORVIEWS[1];
                        parameters.put("editor", editor);
                    }
                }else {
                    bodytitle = oldBodytitle;
                }
            }
            if(bodychangeRequested) {
                if (! isSimplePage) temporaryControlFile.setElementTemplSelector(C_BODY_ELEMENT, body);
                bodytitle = body.equals("(default)") ? "" : body;
                if(body.equals("script")) {
                    // User wants to edit javascript code
                    // Select text editor
                    session.putValue("te_pageeditor", editor);
                    editor = C_SELECTBOX_EDITORVIEWS[1];
                    parameters.put("editor", editor);
                }else {
                    if(oldBody.equals("script")) {
                        // User wants to switch back from javascript mode
                        // Select old editor
                        editor = (String)session.getValue("te_pageeditor");
                        parameters.put("editor", editor);
                    }
                }
            }
            if(newbodyRequested) {
                body = C_BODY_ELEMENT + bodyTemplateFile.createNewSection(C_BODY_ELEMENT);
                bodytitle = body;
                if (! isSimplePage) {
                    temporaryControlFile.setElementTemplSelector(C_BODY_ELEMENT, body);
                    temporaryControlFile.setElementTemplate(C_BODY_ELEMENT, tempBodyFilename);
                }
            }
            
            // check for C_PROPERTY_RELATIVEROOT property (with directory search)
            String relativeRoot = cms.readProperty(file, C_PROPERTY_RELATIVEROOT, true);
            
            // save file contents to our temporary file.
            content = CmsEncoder.unescape(content, CmsEncoder.C_UTF8_ENCODING);
            if (! CmsEncoder.C_UTF8_ENCODING.equalsIgnoreCase(cms.getRequestContext().getEncoding())) {			
                content = CmsEncoder.escapeNonAscii(content);
            }          
			
            if((!exitRequested) || saveRequested) {
                bodyTemplateFile.setEditedTemplateContent(cms, content, oldBody, oldEdit.equals(C_SELECTBOX_EDITORVIEWS[0]), file, relativeRoot);
            }
            cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
            bodyTemplateFile.write();
            if (! isSimplePage) temporaryControlFile.write();
            cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
        }

        // If the user requested a preview then send a redirect
        // to the temporary page file.
        if(previewRequested) {
            preview(tempPageFilename, reqCont);
            return "".getBytes();
        }

        // If the user requested a "save" expilitly by pressing one of
        // the "save" buttons, copy all informations of the temporary
        // files to the original files.
        if(saveRequested) {
            try{
                commitTemporaryFile(cms, bodyElementFilename, tempBodyFilename, tempProject, curProject);
                cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
                title = cms.readProperty(tempPageFilename, C_PROPERTY_TITLE);
                cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
                if(title != null && !"".equals(title)) {
                    cms.writeProperty(file, C_PROPERTY_TITLE, title);
                }
                if (! isSimplePage) {
                    CmsXmlControlFile originalControlFile = new CmsXmlControlFile(cms, file);
                    originalControlFile.setMasterTemplate(temporaryControlFile.getMasterTemplate());
                    originalControlFile.write();
                }
            } catch (CmsException e){
                // there was an exception while the file should be saved
                // return to the editor and show the exception so the user can save the changes
                saveerror = e.getShortException();
                if(content != null){
                    session.putValue(C_PARA_CONTENT, content);
                }
                if(body != null){
                    session.putValue("body", body);
                }
                if(file != null){
                    session.putValue(C_PARA_RESOURCE, file);
                }
                if(editor != null){
                    session.putValue("editor", editor);
                }
                if(title != null){
                    session.putValue(C_PARA_TITLE, title);
                }
                if(bodytitle != null){
                    session.putValue("bodytitle", bodytitle);
                }
                if(layoutTemplatFilenameRelative != null){
                    session.putValue("template", layoutTemplatFilenameRelative);
                }
                if(bodyElementClassName != null){
                    session.putValue("bodyclass", bodyElementClassName);
                }
                if(bodyElementFilename != null){
                    session.putValue("bodyfile", bodyElementFilename);
                }
                if(startView != null){
                    session.putValue("startview", startView);
                }
            }
        }

        // Check if we should leave th editor instead of start processing
        if(exitRequested && ((saveerror == null) || "".equals(saveerror))) {

            // First delete temporary files
            if (! isSimplePage) temporaryControlFile.removeFromFileCache();
            bodyTemplateFile.removeFromFileCache();
            // deleting the pagefile will delete the bodyfile too
            cms.getRequestContext().setCurrentProject(cms.readProject(tempProject));
            cms.deleteResource(tempPageFilename, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            cms.getRequestContext().setCurrentProject(cms.readProject(curProject));
            try {
                CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(CmsWorkplaceAction.getWorkplaceUri(CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()));
            }catch(IOException e) {
                throwException("Could not send redirect to workplace main screen.", e);
            }
            return null;
        }

        // Include the datablocks of the layout file into the body file.
        // So the "bodytag" and "style" data can be accessed by the body file.
        Element bodyTag = layoutTemplateFile.getBodyTag();
        bodyTemplateFile.setBodyTag(bodyTag);
        
		// Load the body!
		content = bodyTemplateFile.getEditableTemplateContent(this, parameters, body, editor.equals(C_SELECTBOX_EDITORVIEWS[0]), style);
		
		// set the context & servlet path in editor content
		content = CmsStringUtil.substitute(content, C_MACRO_OPENCMS_CONTEXT + "/", OpenCms.getSystemInfo().getOpenCmsContext() + "/");
        
        // escape content
        content = CmsEncoder.escapeWBlanks(content, CmsEncoder.C_UTF8_ENCODING);
        
        parameters.put(C_PARA_CONTENT, content);

        // put the body parameter so that the selectbox can set the correct current value
        parameters.put("body", body);
        parameters.put("bodyfile", bodyElementFilename);
        parameters.put("bodyclass", bodyElementClassName);
        parameters.put("template", layoutTemplateFilename);

        // remove all parameters that could be relevant for the
        // included editor.
        parameters.remove(C_PARA_RESOURCE);
        parameters.remove(C_PARA_ACTION);
        int numEditors = C_SELECTBOX_EDITORVIEWS.length;
        for(int i = 0;i < numEditors;i++) {
            if(editor.equals(C_SELECTBOX_EDITORVIEWS[i])) {
                parameters.put("editor._CLASS_", C_SELECTBOX_EDITORVIEWS_CLASSES[i]);
                parameters.put("editor._TEMPLATE_", C_VFS_PATH_DEFAULT_INTERNAL + C_SELECTBOX_EDITORVIEWS_TEMPLATES[i]);
            }
        }
        session.putValue("te_file", file);
        session.putValue("te_oldedit", editor);
        session.putValue("te_oldbody", body);
        session.putValue("te_oldbodytitle", bodytitle);
        session.putValue("te_oldlayout", layoutTemplateFilename);
        if(title != null) {
            session.putValue("te_title", title);
        }else {
            session.putValue("te_title", "");
        }
        session.putValue("te_templateclass", layoutTemplateClassName);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        xmlTemplateDocument.setData("editor", editor);
        xmlTemplateDocument.setData("bodyfile", bodyElementFilename);
        xmlTemplateDocument.setData("bodyclass", bodyElementClassName);
        xmlTemplateDocument.setData("editorframe", (String)parameters.get("root.editorframe"));
        if (bodytitle != null) {
            xmlTemplateDocument.setData("bodytitle", bodytitle);
        }

        // Put the "file" datablock for processing in the template file.
        // It will be inserted in a hidden input field and given back when submitting.
        xmlTemplateDocument.setData(C_PARA_RESOURCE, file);
        if(!"".equals(saveerror)){
            templateSelector = "errorsave";
            xmlTemplateDocument.setData("errordetail", saveerror);
            xmlTemplateDocument.setData("errorlasturl", xmlTemplateDocument.getFilename()+".html");
        }
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /** Gets all editor views available in the template editor screens.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * Used to build font select boxes in editors.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current workplace view in the vectors.
     * @throws CmsException
     */

    public Integer getEditorViews(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {
        Vector names2 = new Vector();
        Vector values2 = new Vector();
        getConstantSelectEntries(names2, values2, C_SELECTBOX_EDITORVIEWS, lang);
        int browserId;
        CmsRequestContext reqCont = cms.getRequestContext();
        HttpServletRequest orgReq = CmsXmlTemplateLoader.getRequest(reqCont).getOriginalRequest();
        String browser = orgReq.getHeader("user-agent");
        if(browser.indexOf("MSIE") > -1) {
            browserId = 0;
        }
        else {
            browserId = 1;
        }
        int loop = 1;
        int allowedEditors = C_SELECTBOX_EDITORVIEWS_ALLOWED[browserId];
        if(((String)parameters.get("body")).equals("script")) {
            allowedEditors = allowedEditors & 510;
        }
        for(int i = 0;i < names2.size();i++) {
            if((allowedEditors & loop) > 0) {
                values.addElement(values2.elementAt(i));
                names.addElement(names2.elementAt(i));
            }
            loop <<= 1;
        }
        int currentIndex = values.indexOf(parameters.get("editor"));
        return new Integer(currentIndex);
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

    protected void preview(String previewPath, CmsRequestContext reqCont) throws CmsException {
        String servletPath = CmsXmlTemplateLoader.getRequest(reqCont).getServletUrl();
        try {
            CmsXmlTemplateLoader.getResponse(reqCont).sendCmsRedirect(previewPath);
        }
        catch(IOException e) {
            throwException("Could not send redirect preview file " + servletPath + previewPath, e);
        }
    }

    /**
     * User method to generate an URL for a preview.
     * The currently selected temporary file name will be considered.
     * <P>
     * In the editor template file, this method can be invoked by
     * <code>&lt;METHOD name="previewUrl"/&gt;</code>.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @throws CmsException
     */

    public Object previewUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        String servletPath = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl();
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String tempPath = (String)session.getValue("te_temppagefile");
        String result = servletPath + tempPath;
        return result;
    }

    /**
     * Pre-Sets the value of the body title input field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is pre-set into the title field.
     * @throws CmsExeption if something goes wrong.
     */

    public String setBodyTitle(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String title = (String)session.getValue("te_oldbodytitle");
        return CmsEncoder.escapeXml(title);
    }
    
    public Object getCharset(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) {
        // Try to find property on file and all parent folders
        String prop = null;
        try {
            I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
            String file = (String)session.getValue("te_file");
            prop = cms.readProperty(file, C_PROPERTY_CONTENT_ENCODING);
            while ((prop == null) && (! "".equals(file))) {
                file = file.substring(0, file.lastIndexOf("/"));
                prop = cms.readProperty(file + "/", C_PROPERTY_CONTENT_ENCODING);
            } 
        } catch (Exception e) {}
        if (prop == null) prop = OpenCms.getSystemInfo().getDefaultEncoding();
        return prop;
    }
    
    public Object setText(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable)userObj;

        String content = (String)parameters.get(C_PARA_CONTENT);
        boolean existsContentParam = (content != null && (!"".equals(content)));

        // Check the existance of the "file" parameter
        if(!existsContentParam) {
            if(OpenCms.getLog(this).isWarnEnabled() ) {
                OpenCms.getLog(this).warn("No content found");
            }
            content = "";
        }

        // Escape the text for including it in HTML text
        return content;
    }

    /**
     * Pre-Sets the value of the title input field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is pre-set into the title field.
     * @throws CmsExeption if something goes wrong.
     */

    public String setTitle(CmsObject cms, CmsXmlLanguageFile lang,
            Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String name = (String)session.getValue("te_title");
        return CmsEncoder.escapeXml(name);
    }
}
