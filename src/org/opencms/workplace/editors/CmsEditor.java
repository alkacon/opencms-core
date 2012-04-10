/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsFrameset;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Provides basic methods for building the file editors of OpenCms.<p> 
 * 
 * The editor classes have to extend this class and implement action methods for common editor actions.<p>
 * 
 * @since 6.0.0 
 */
public abstract class CmsEditor extends CmsEditorBase {

    /** Value for the action: change the body. */
    public static final int ACTION_CHANGE_BODY = 124;

    /** Value for the action: delete the current locale. */
    public static final int ACTION_DELETELOCALE = 140;

    /** Value for the action: exit. */
    public static final int ACTION_EXIT = 122;

    /** Value for the action: show a preview. */
    public static final int ACTION_PREVIEW = 126;

    /** Value for the action: save. */
    public static final int ACTION_SAVE = 121;

    /** Constant value for the customizable action button. */
    public static final int ACTION_SAVEACTION = 130;

    /** Value for the action: save and exit. */
    public static final int ACTION_SAVEEXIT = 123;

    /** Value for the action: show the editor. */
    public static final int ACTION_SHOW = 125;

    /** Value for the action: an error occurred. */
    public static final int ACTION_SHOW_ERRORMESSAGE = 127;

    /** Value for the action parameter: change the element. */
    public static final String EDITOR_CHANGE_ELEMENT = "changeelement";

    /** Value for the action parameter: cleanup content. */
    public static final String EDITOR_CLEANUP = "cleanup";

    /** Value for the action parameter: close browser window (accidentally). */
    public static final String EDITOR_CLOSEBROWSER = "closebrowser";

    /** Value for the action parameter: delete the current locale. */
    public static final String EDITOR_DELETELOCALE = "deletelocale";

    /** Value for the action parameter: exit editor. */
    public static final String EDITOR_EXIT = "exit";

    /** Value for the action parameter: show a preview. */
    public static final String EDITOR_PREVIEW = "preview";

    /** Value for the action parameter: save content. */
    public static final String EDITOR_SAVE = "save";

    /** Value for the customizable action button. */
    public static final String EDITOR_SAVEACTION = "saveaction";

    /** Value for the action parameter: save and exit. */
    public static final String EDITOR_SAVEEXIT = "saveexit";

    /** Value for the action parameter: show the editor. */
    public static final String EDITOR_SHOW = "show";

    /** Value for the action parameter: an error occurred. */
    public static final String EDITOR_SHOW_ERRORMESSAGE = "error";

    /** Marker for empty locale in locale selection. */
    public static final String EMPTY_LOCALE = " [-]";

    /** Parameter name for the request parameter "backlink". */
    public static final String PARAM_BACKLINK = "backlink";

    /** Parameter name for the request parameter "content". */
    public static final String PARAM_CONTENT = "content";

    /** Parameter name for the request parameter "directedit". */
    public static final String PARAM_DIRECTEDIT = "directedit";

    /** Parameter name for the request parameter "editastext". */
    public static final String PARAM_EDITASTEXT = "editastext";

    /** Parameter name for the request parameter "editormode". */
    public static final String PARAM_EDITORMODE = "editormode";

    /** Parameter name for the request parameter "element language". */
    public static final String PARAM_ELEMENTLANGUAGE = "elementlanguage";

    /** Parameter name for the request parameter "loaddefault". */
    public static final String PARAM_LOADDEFAULT = "loaddefault";

    /** Parameter name for the request parameter "modified". */
    public static final String PARAM_MODIFIED = "modified";

    /** Parameter name for the request parameter "old element language". */
    public static final String PARAM_OLDELEMENTLANGUAGE = "oldelementlanguage";

    /** Parameter name for the request parameter "tempfile". */
    public static final String PARAM_TEMPFILE = "tempfile";

    /** Stores the VFS editor path. */
    public static final String PATH_EDITORS = PATH_WORKPLACE + "editors/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditor.class);

    /** A cloned cms instance that prevents the broken link remotion during unmarshalling. */
    private CmsObject m_cloneCms;

    /** The editor session info bean. */
    private CmsEditorSessionInfo m_editorSessionInfo;
    /** The encoding to use (will be read from the file property). */
    private String m_fileEncoding;
    // some private members for parameter storage
    private String m_paramBackLink;
    private String m_paramContent;
    private String m_paramDirectedit;
    private String m_paramEditAsText;
    private String m_paramEditormode;
    private String m_paramElementlanguage;
    private String m_paramLoadDefault;
    private String m_paramModified;

    private String m_paramOldelementlanguage;

    private String m_paramTempFile;

    /** Helper variable to store the uri to the editors pictures. */
    private String m_picsUri;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditor(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Unlocks the edited resource when in direct edit mode or when the resource was not modified.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    public abstract void actionClear(boolean forceUnlock);

    /**
     * Performs the exit editor action.<p>
     * 
     * @throws CmsException if something goes wrong
     * @throws IOException if a forward fails
     * @throws ServletException if a forward fails
     * @throws JspException if including an element fails
     */
    public abstract void actionExit() throws CmsException, IOException, ServletException, JspException;

    /**
     * Performs the save content action.<p>
     * 
     * @throws IOException if a redirection fails
     * @throws JspException if including an element fails
     */
    public abstract void actionSave() throws IOException, JspException;

    /**
     * Builds the html String for the element language selector.<p>
     *  
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @param resourceName the name of the resource to edit
     * @param selectedLocale the currently selected Locale
     * @return the html for the element language selectbox
     */
    public String buildSelectElementLanguage(String attributes, String resourceName, Locale selectedLocale) {

        // get locale names based on properties and global settings
        List locales = OpenCms.getLocaleManager().getAvailableLocales(getCms(), resourceName);
        List options = new ArrayList(locales.size());
        List selectList = new ArrayList(locales.size());
        int currentIndex = -1;

        //get the locales already used in the resource
        List contentLocales = new ArrayList();
        try {

            CmsResource res = getCms().readResource(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
            String temporaryFilename = CmsWorkplace.getTemporaryFileName(resourceName);
            if (getCms().existsResource(temporaryFilename, CmsResourceFilter.IGNORE_EXPIRATION)) {
                res = getCms().readResource(temporaryFilename, CmsResourceFilter.IGNORE_EXPIRATION);
            }
            CmsFile file = getCms().readFile(res);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
            contentLocales = xmlContent.getLocales();
        } catch (CmsException e) {
            // to nothing here in case the resource could not be opened
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_GET_LOCALES_1, resourceName), e);
            }
        }

        for (int counter = 0; counter < locales.size(); counter++) {
            // create the list of options and values
            Locale curLocale = (Locale)locales.get(counter);
            selectList.add(curLocale.toString());
            StringBuffer buf = new StringBuffer();
            buf.append(curLocale.getDisplayName(getLocale()));
            if (!contentLocales.contains(curLocale)) {
                buf.append(EMPTY_LOCALE);
            }
            options.add(buf.toString());
            if (curLocale.equals(selectedLocale)) {
                // set the selected index of the selector
                currentIndex = counter;
            }
        }

        if (currentIndex == -1) {
            // no matching element language found, use first element language in list
            if (selectList.size() > 0) {
                currentIndex = 0;
                setParamElementlanguage((String)selectList.get(0));
            }
        }

        return buildSelect(attributes, options, selectList, currentIndex, false);
    }

    /**
     * Generates a button for the OpenCms editor.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automatically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * @param useCustomImage if true, the button has to be placed in the editors "custom pics" folder
     * 
     * @return a button for the OpenCms editor
     */
    public String button(String href, String target, String image, String label, int type, boolean useCustomImage) {

        if (useCustomImage) {
            // search the picture in the "custom pics" folder
            return button(href, target, image, label, type, getPicsUri());
        } else {
            // search the picture in the common "buttons" folder
            return button(href, target, image, label, type);
        }
    }

    /**
     * Returns the editor action for a "cancel" button.<p>
     * 
     * This overwrites the cancel method of the CmsDialog class.<p>
     * 
     * Always use this value, do not write anything directly in the html page.<p>
     * 
     * @return the default action for a "cancel" button
     */
    public String buttonActionCancel() {

        String target = null;
        if (Boolean.valueOf(getParamDirectedit()).booleanValue()) {
            // editor is in direct edit mode
            if (CmsStringUtil.isNotEmpty(getParamBacklink())) {
                // set link to the specified back link target
                target = getParamBacklink();
            } else {
                // set link to the edited resource
                target = getParamResource();
            }
        } else {
            // in workplace mode, show explorer view
            target = OpenCms.getLinkManager().substituteLink(getCms(), CmsFrameset.JSP_WORKPLACE_URI);
        }
        return "onclick=\"top.location.href='" + getJsp().link(target) + "';\"";
    }

    /**
     * Builds the html to display the special action button for the direct edit mode of the editor.<p>
     * 
     * @param jsFunction the JavaScript function which will be executed on the mouseup event 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * @return the html to display the special action button
     */
    public String buttonActionDirectEdit(String jsFunction, int type) {

        // get the action class from the OpenCms runtime property
        I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
        String url;
        String name;
        boolean active = false;
        if (actionClass != null) {
            // get button parameters and state from action class
            url = actionClass.getButtonUrl(getJsp(), getParamResource());
            name = actionClass.getButtonName();
            active = actionClass.isButtonActive(getJsp(), getParamResource());
        } else {
            // action class not defined, display inactive button
            url = getSkinUri() + "buttons/publish_in.png";
            name = Messages.GUI_EXPLORER_CONTEXT_PUBLISH_0;
        }
        String image = url.substring(url.lastIndexOf("/") + 1);
        if (url.endsWith(".gif")) {
            image = image.substring(0, image.length() - 4);
        }

        if (active) {
            // create the link for the button
            return button(
                "javascript:" + jsFunction,
                null,
                image,
                name,
                type,
                url.substring(0, url.lastIndexOf("/") + 1));
        } else {
            // create the inactive button
            return button(null, null, image, name, type, url.substring(0, url.lastIndexOf("/") + 1));
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#checkLock(String, CmsLockType)
     */
    public void checkLock(String resource, CmsLockType type) throws CmsException {

        CmsResource res = getCms().readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = getCms().getLock(res);
        if (!lock.isNullLock()) {
            setParamModified(Boolean.TRUE.toString());
        }

        // for resources with siblings make sure all sibling have at least a
        // temporary lock
        if ((res.getSiblingCount() > 1) && (lock.isInherited())) {
            super.checkLock(resource, CmsLockType.TEMPORARY);
        } else {
            super.checkLock(resource, type);
        }
    }

    /**
     * Generates a button for delete locale.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automatically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * 
     * @return a button for the OpenCms workplace
     */
    public String deleteLocaleButton(String href, String target, String image, String label, int type) {

        String filename = getParamResource();

        try {
            CmsResource res = getCms().readResource(filename, CmsResourceFilter.IGNORE_EXPIRATION);

            String temporaryFilename = CmsWorkplace.getTemporaryFileName(filename);
            if (getCms().existsResource(temporaryFilename, CmsResourceFilter.IGNORE_EXPIRATION)) {
                res = getCms().readResource(temporaryFilename, CmsResourceFilter.IGNORE_EXPIRATION);
            }
            CmsFile file = getCms().readFile(res);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
            int locales = xmlContent.getLocales().size();
            // there are less than 2 locales, so disable the delete locale button
            if (locales < 2) {
                href = null;
                target = null;
                image += "_in";
            }
        } catch (CmsException e) {
            // to nothing here in case the resource could not be opened
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_GET_LOCALES_1, filename), e);
            }
        }
        return button(href, target, image, label, type, getSkinUri() + "buttons/");

    }

    /**
     * Returns the instantiated editor display option class from the workplace manager.<p>
     * 
     * This is a convenience method to be used on editor JSPs.<p>
     * 
     * @return the instantiated editor display option class
     */
    public CmsEditorDisplayOptions getEditorDisplayOptions() {

        return OpenCms.getWorkplaceManager().getEditorDisplayOptions();
    }

    /**
     * Returns the URI to the editor resource folder where button images and javascripts are located.<p>
     * 
     * @return the URI to the editor resource folder
     */
    public abstract String getEditorResourceUri();

    /**
     * Returns the OpenCms request context path.<p>
     * 
     * This is a convenience method to use in the editor.<p>
     * 
     * @return the OpenCms request context path
     */
    public String getOpenCmsContext() {

        return OpenCms.getSystemInfo().getOpenCmsContext();
    }

    /**
     * Returns the back link when closing the editor.<p>
     * 
     * @return the back link
     */
    public String getParamBacklink() {

        if ((m_editorSessionInfo != null)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_editorSessionInfo.getBackLink())) {
            m_paramBackLink = m_editorSessionInfo.getBackLink();
        }
        if (m_paramBackLink == null) {
            m_paramBackLink = "";
        }
        return m_paramBackLink;
    }

    /**
     * Returns the content of the editor.<p>
     * @return the content of the editor
     */
    public String getParamContent() {

        if (m_paramContent == null) {
            m_paramContent = "";
        }
        return m_paramContent;
    }

    /**
     * Returns the direct edit flag parameter.<p>
     *  
     * @return the direct edit flag parameter
     */
    public String getParamDirectedit() {

        if (m_editorSessionInfo != null) {
            return String.valueOf(m_editorSessionInfo.isDirectEdit());
        }
        return m_paramDirectedit;
    }

    /**
     * Returns the edit as text parameter.<p>
     * 
     * @return the edit as text parameter
     */
    public String getParamEditastext() {

        return m_paramEditAsText;
    }

    /**
     * Returns the editor mode parameter.<p>
     *  
     * @return the editor mode parameter
     */
    public String getParamEditormode() {

        return m_paramEditormode;
    }

    /**
     * Returns the current element language.<p>
     * 
     * @return the current element language
     */
    public String getParamElementlanguage() {

        if (m_paramElementlanguage == null) {
            if ((m_editorSessionInfo != null) && (m_editorSessionInfo.getElementLocale() != null)) {
                m_paramElementlanguage = m_editorSessionInfo.getElementLocale().toString();
            }
        }
        return m_paramElementlanguage;
    }

    /**
     * Returns the "loaddefault" parameter to determine if the default editor should be loaded.<p>
     * 
     * @return the "loaddefault" parameter
     */
    public String getParamLoaddefault() {

        return m_paramLoadDefault;
    }

    /**
     * Returns the modified parameter indicating if the resource has been saved.<p>
     *
     * @return the modified parameter indicating if the resource has been saved
     */
    public String getParamModified() {

        return m_paramModified;
    }

    /**
     * Returns the old element language.<p>
     * 
     * @return the old element language
     */
    public String getParamOldelementlanguage() {

        return m_paramOldelementlanguage;
    }

    /**
     * Returns the name of the temporary file.<p>
     * 
     * @return the name of the temporary file
     */
    public String getParamTempfile() {

        return m_paramTempFile;
    }

    /**
     * Returns the path to the images used by this editor.<p>
     * 
     * @return the path to the images used by this editor
     */
    public String getPicsUri() {

        if (m_picsUri == null) {
            m_picsUri = getEditorResourceUri() + "pics/";
        }
        return m_picsUri;
    }

    /**
     * Sets the back link when closing the editor.<p>
     * 
     * @param backLink the back link
     */
    public void setParamBacklink(String backLink) {

        m_paramBackLink = backLink;
    }

    /**
     * Sets the content of the editor.<p>
     * 
     * @param content the content of the editor
     */
    public void setParamContent(String content) {

        if (content == null) {
            content = "";
        }
        m_paramContent = content;
    }

    /**
     * Sets the direct edit flag parameter.<p>
     * 
     * @param direct the direct edit flag parameter
     */
    public void setParamDirectedit(String direct) {

        m_paramDirectedit = direct;
    }

    /**
     * Sets the  edit as text parameter.<p>
     * 
     * @param editAsText <code>"true"</code> if the resource should be handled like a text file
     */
    public void setParamEditastext(String editAsText) {

        m_paramEditAsText = editAsText;
    }

    /**
     * Sets the editor mode parameter.<p>
     * 
     * @param mode the editor mode parameter
     */
    public void setParamEditormode(String mode) {

        m_paramEditormode = mode;
    }

    /**
     * Sets the current element language.<p>
     * 
     * @param elementLanguage the current element language
     */
    public void setParamElementlanguage(String elementLanguage) {

        m_paramElementlanguage = elementLanguage;
    }

    /**
     * Sets the "loaddefault" parameter to determine if the default editor should be loaded.<p>
     * 
     * @param loadDefault the "loaddefault" parameter
     */
    public void setParamLoaddefault(String loadDefault) {

        m_paramLoadDefault = loadDefault;
    }

    /**
     * Sets the modified parameter indicating if the resource has been saved.<p>
     *
     * @param modified the modified parameter indicating if the resource has been saved
     */
    public void setParamModified(String modified) {

        m_paramModified = modified;
    }

    /**
     * Sets the old element language.<p>
     * 
     * @param oldElementLanguage the old element language
     */
    public void setParamOldelementlanguage(String oldElementLanguage) {

        m_paramOldelementlanguage = oldElementLanguage;
    }

    /**
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {

        m_paramTempFile = fileName;
    }

    /**
     * Closes the editor and forwards to the workplace or the resource depending on the editor mode.<p>
     * 
     * @throws IOException if forwarding fails 
     * @throws ServletException if forwarding fails
     * @throws JspException if including a JSP fails
     */
    protected void actionClose() throws IOException, JspException, ServletException {

        try {
            if (Boolean.valueOf(getParamDirectedit()).booleanValue()) {
                // editor is in direct edit mode
                if (CmsStringUtil.isNotEmpty(getParamBacklink())) {
                    // set link to the specified back link target
                    setParamCloseLink(getJsp().link(getParamBacklink()));
                } else {
                    // set link to the edited resource
                    setParamCloseLink(getJsp().link(getParamResource()));
                }
                // save initialized instance of this class in request attribute for included sub-elements
                getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
                // load the common JSP close dialog
                getJsp().include(FILE_DIALOG_CLOSE);
            } else {
                if (CmsStringUtil.isNotEmpty(getParamBacklink())) {
                    // set link to the specified back link target
                    setParamCloseLink(getJsp().link(getParamBacklink()));
                    // save initialized instance of this class in request attribute for included sub-elements
                    getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
                    // load the common JSP close dialog
                    getJsp().include(FILE_DIALOG_CLOSE);
                } else {
                    // forward to the workplace explorer view
                    sendForward(CmsFrameset.JSP_WORKPLACE_URI, new HashMap());
                }
            }
        } finally {
            clearEditorSessionInfo();
        }
    }

    /**
     * Clears the editor session info bean.<p>
     */
    protected void clearEditorSessionInfo() {

        if (m_editorSessionInfo != null) {
            getSession().removeAttribute(m_editorSessionInfo.getEditorSessionInfoKey());
        }
        m_editorSessionInfo = null;
    }

    /**
     * Writes the content of a temporary file back to the original file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void commitTempFile() throws CmsException {

        CmsObject cms = getCms();
        CmsFile tempFile;
        List properties;
        try {
            switchToTempProject();
            tempFile = cms.readFile(getParamTempfile(), CmsResourceFilter.ALL);
            properties = cms.readPropertyObjects(getParamTempfile(), false);
        } finally {
            // make sure the project is reset in case of any exception
            switchToCurrentProject();
        }
        if (cms.existsResource(getParamResource(), CmsResourceFilter.ALL)) {
            // update properties of original file first (required if change in encoding occurred)
            cms.writePropertyObjects(getParamResource(), properties);
            // now replace the content of the original file
            CmsFile orgFile = cms.readFile(getParamResource(), CmsResourceFilter.ALL);
            orgFile.setContents(tempFile.getContents());
            getCloneCms().writeFile(orgFile);
        } else {
            // original file does not exist, remove visibility permission entries and copy temporary file

            // switch to the temporary file project
            cms.getRequestContext().setCurrentProject(cms.readProject(getSettings().getProject()));
            // lock the temporary file
            cms.changeLock(getParamTempfile());
            // remove visibility permissions for everybody on temporary file if possible
            if (cms.hasPermissions(tempFile, CmsPermissionSet.ACCESS_CONTROL)) {
                cms.rmacc(getParamTempfile(), I_CmsPrincipal.PRINCIPAL_GROUP, OpenCms.getDefaultUsers().getGroupUsers());
                cms.rmacc(
                    getParamTempfile(),
                    I_CmsPrincipal.PRINCIPAL_GROUP,
                    OpenCms.getDefaultUsers().getGroupProjectmanagers());
            }

            cms.copyResource(getParamTempfile(), getParamResource(), CmsResource.COPY_AS_NEW);
            // ensure the content handler is called 
            CmsFile orgFile = cms.readFile(getParamResource(), CmsResourceFilter.ALL);
            getCloneCms().writeFile(orgFile);

        }
        // remove the temporary file flag
        int flags = cms.readResource(getParamResource(), CmsResourceFilter.ALL).getFlags();
        if ((flags & CmsResource.FLAG_TEMPFILE) == CmsResource.FLAG_TEMPFILE) {
            flags ^= CmsResource.FLAG_TEMPFILE;
            cms.chflags(getParamResource(), flags);
        }
    }

    /**
     * Creates a temporary file which is needed while working in an editor with preview option.<p>
     * 
     * @return the file name of the temporary file
     * @throws CmsException if something goes wrong
     */
    protected String createTempFile() throws CmsException {

        return OpenCms.getWorkplaceManager().createTempFile(getCms(), getParamResource(), getSettings().getProject());
    }

    /**
     * Decodes the given content the same way the client would do it.<p>
     * 
     * Content is decoded as if it was encoded using the JavaScript
     * "encodeURIComponent()" function.<p>
     * 
     * @param content the content to decode
     * @return the decoded content
     */
    protected String decodeContent(String content) {

        return CmsEncoder.unescape(content, CmsEncoder.ENCODING_UTF_8);
    }

    /**
     * Decodes an individual parameter value, ensuring the content is always decoded in UTF-8.<p>
     * 
     * For editors the content is always encoded using the 
     * JavaScript encodeURIComponent() method on the client,
     * which always encodes in UTF-8.<p> 
     * 
     * @param paramName the name of the parameter 
     * @param paramValue the unencoded value of the parameter
     * @return the encoded value of the parameter
     */
    protected String decodeParamValue(String paramName, String paramValue) {

        if ((paramName != null) && (paramValue != null)) {
            if (PARAM_CONTENT.equals(paramName)) {
                // content will be always encoded in UTF-8 unicode by the editor client
                return CmsEncoder.decode(paramValue, CmsEncoder.ENCODING_UTF_8);
            } else if (PARAM_RESOURCE.equals(paramName) || PARAM_TEMPFILE.equals(paramName)) {
                String filename = CmsEncoder.decode(paramValue, getCms().getRequestContext().getEncoding());
                if (PARAM_TEMPFILE.equals(paramName) || CmsStringUtil.isEmpty(getParamTempfile())) {
                    // always use value from temp file if it is available
                    setFileEncoding(getFileEncoding(getCms(), filename));
                }
                return filename;
            } else {
                return CmsEncoder.decode(paramValue, getCms().getRequestContext().getEncoding());
            }
        } else {
            return null;
        }
    }

    /**
     * Deletes a temporary file from the OpenCms VFS, needed when exiting an editor.<p> 
     */
    protected void deleteTempFile() {

        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            getCms().deleteResource(getParamTempfile(), CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        } finally {
            try {
                // switch back to the current project
                switchToCurrentProject();
            } catch (CmsException e) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
        }
    }

    /**
     * Encodes the given content so that it can be transfered to the client.<p>
     * 
     * Content is encoded so that it is compatible with the JavaScript
     * "decodeURIComponent()" function.<p>
     * 
     * @param content the content to encode
     * @return the encoded content
     */
    protected String encodeContent(String content) {

        return CmsEncoder.escapeWBlanks(content, CmsEncoder.ENCODING_UTF_8);
    }

    /** 
     * Returns a cloned cms instance that prevents the time range resource filter check.<p> 
     * 
     * Use it always for unmarshalling and file writing.<p>
     * 
     * @return a cloned cms instance that prevents the time range resource filter check
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsObject getCloneCms() throws CmsException {

        if (m_cloneCms == null) {
            m_cloneCms = OpenCms.initCmsObject(getCms());
            m_cloneCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        }
        return m_cloneCms;
    }

    /**
     * Returns the editor session info bean.<p>
     * 
     * @return the editor session info bean
     */
    protected CmsEditorSessionInfo getEditorSessionInfo() {

        return m_editorSessionInfo;
    }

    /**
     * Returns the encoding parameter.<p>
     *
     * @return the encoding parameter
     */
    protected String getFileEncoding() {

        return m_fileEncoding;
    }

    /**
     * Helper method to determine the encoding of the given file in the VFS,
     * which must be set using the "content-encoding" property.<p>
     * 
     * @param cms the CmsObject
     * @param filename the name of the file which is to be checked
     * @return the encoding for the file
     */
    protected String getFileEncoding(CmsObject cms, String filename) {

        try {
            return cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true).getValue(
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            return OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }

    /**
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected abstract void initContent();

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        initSessionInfo();
        super.initMessages();
    }

    /**
     * Initializes the editor session info bean.<p>
     */
    protected void initSessionInfo() {

        CmsResource editedResource = null;
        try {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamResource())) {
                editedResource = getCms().readResource(getParamResource());
            }
        } catch (CmsException e) {
            // ignore
        }

        CmsEditorSessionInfo info = null;
        if (editedResource != null) {
            HttpSession session = getSession();
            info = (CmsEditorSessionInfo)session.getAttribute(CmsEditorSessionInfo.getEditorSessionInfoKey(editedResource));
            if (info == null) {
                info = new CmsEditorSessionInfo(editedResource.getStructureId());
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_paramBackLink)) {
                info.setBackLink(m_paramBackLink);
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_paramElementlanguage)) {
                info.setElementLocale(CmsLocaleManager.getLocale(m_paramElementlanguage));
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_paramDirectedit)) {
                info.setDirectEdit(Boolean.parseBoolean(m_paramDirectedit));
            }
            session.setAttribute(info.getEditorSessionInfoKey(), info);
        }
        m_editorSessionInfo = info;
    }

    /**
     * Sets the encoding parameter.<p>
     *
     * @param value the encoding value to set
     */
    protected void setFileEncoding(String value) {

        m_fileEncoding = CmsEncoder.lookupEncoding(value, value);
    }

    /**
     * Shows the selected error page in case of an exception.<p>
     * 
     * @param exception the current exception
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Exception exception) throws JspException {

        // reset the action parameter            
        setParamAction("");
        showErrorPage(this, exception);
        // save not successful, set cancel action 
        setAction(ACTION_CANCEL);
        return;
    }

    /**
     * Shows the selected error page in case of an exception.<p>
     * 
     * @param editor initialized instance of the editor class
     * @param exception the current exception
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Object editor, Exception exception) throws JspException {

        // save initialized instance of the editor class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, editor);

        // reading of file contents failed, show error dialog
        setAction(ACTION_SHOW_ERRORMESSAGE);
        setParamTitle(key(Messages.GUI_TITLE_EDIT_1, new Object[] {CmsResource.getName(getParamResource())}));
        if (exception != null) {
            getJsp().getRequest().setAttribute(ATTRIBUTE_THROWABLE, exception);
            if (CmsLog.getLog(editor).isWarnEnabled()) {
                CmsLog.getLog(editor).warn(exception.getLocalizedMessage(), exception);
            }
        }
        // include the common error dialog
        getJsp().include(FILE_DIALOG_SCREEN_ERRORPAGE);
    }
}