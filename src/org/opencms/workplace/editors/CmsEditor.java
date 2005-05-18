/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsEditor.java,v $
 * Date   : $Date: 2005/05/18 07:34:41 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceAction;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Provides basic methods for building the file editors of OpenCms.<p> 
 * 
 * The editor classes have to extend this class and implement action methods for common editor actions.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.14 $
 * 
 * @since 5.1.12
 */
public abstract class CmsEditor extends CmsDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditor.class);  
    
    /** Value for the action: change the body. */
    public static final int ACTION_CHANGE_BODY = 124;

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

    /** Value for the action: an error occured. */
    public static final int ACTION_SHOW_ERRORMESSAGE = 127;
    
    /** Stores the VFS editor path. */
    public static final String C_PATH_EDITORS = C_PATH_WORKPLACE + "editors/";
    
    /** Constant for the Editor special "save error" confirmation dialog. */
    public static final String C_FILE_DIALOG_EDITOR_CONFIRM = C_PATH_EDITORS + "dialogs/confirm.jsp";
        
    /** Value for the action parameter: change the element. */
    public static final String EDITOR_CHANGE_ELEMENT = "changeelement";

    /** Value for the action parameter: cleanup content. */
    public static final String EDITOR_CLEANUP = "cleanup";
    
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

    /** Value for the action parameter: an error occured. */
    public static final String EDITOR_SHOW_ERRORMESSAGE = "error";
    
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
    
    /** Parameter name for the request parameter "old element language". */
    public static final String PARAM_OLDELEMENTLANGUAGE = "oldelementlanguage";
    
    /** Parameter name for the request parameter "tempfile". */
    public static final String PARAM_TEMPFILE = "tempfile";

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
     * Unlocks the edited resource when in direct edit mode.<p>
     * 
     * @param forceUnlock if true, the resource will be unlocked anyway
     */
    public abstract void actionClear(boolean forceUnlock);
        
    /**
     * Performs the exit editor action.<p>
     * 
     * @throws CmsException if something goes wrong
     * @throws IOException if a redirection fails
     * @throws JspException if including an element fails
     */
    public abstract void actionExit() throws CmsException, IOException, JspException;
    
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
     * @param resource the name of the resource to edit
     * @param selectedLocale the currently selected Locale
     * @return the html for the element language selectbox
     */
    public String buildSelectElementLanguage(String attributes, String resource, Locale selectedLocale) {
        // get locale names based on properties and global settings
        List locales = OpenCms.getLocaleManager().getAvailableLocales(getCms(), resource);
        List options = new ArrayList(locales.size());
        List selectList = new ArrayList(locales.size());        
        int currentIndex = -1;
        for (int counter = 0; counter < locales.size(); counter++) {
            // create the list of options and values
            Locale curLocale = (Locale)locales.get(counter);
            selectList.add(curLocale.toString());
            options.add(curLocale.getDisplayName(getLocale()));
            if (curLocale.equals(selectedLocale)) {
                // set the selected index of the selector
                currentIndex = counter;
            }
        }
       
        if (currentIndex == -1) {
            // no matching element language found, use first element language in list
            if (selectList != null && selectList.size() > 0) {
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
     * @param image the image name for the button, skin path will be automattically added as prefix
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
        if ("true".equals(getParamDirectedit())) {
            // editor is in direct edit mode
            if (!"".equals(getParamBacklink())) {
                // set link to the specified back link target
                target = getParamBacklink();
            } else {
                // set link to the edited resource
                target = getParamResource();
            }
        } else {
            // in workplace mode, show explorer view
            target = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
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
            url = getSkinUri() + "buttons/publish_in";
            name = "explorer.context.publish";
        }
        String image = url.substring(url.lastIndexOf("/") + 1);
        if (url.endsWith(".gif")) {
            image = image.substring(0, image.length() - 4);
        }
        
        if (active) {
            // create the link for the button
            return button("javascript:" + jsFunction, null, image, name, type, url.substring(0, url.lastIndexOf("/") + 1));
        } else {
            // create the inactive button
            return button(null, null, image, name + "_in", type, url.substring(0, url.lastIndexOf("/") + 1));
        }
    }
    
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
     * Returns the instanciated editor display option class from the workplace manager.<p>
     * 
     * This is a convenience method to be used on editor JSPs.<p>
     * 
     * @return the instanciated editor display option class
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
     * Returns the back link when closing the editor.<p>
     * 
     * @return the back link
     */
    public String getParamBacklink() {
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
     * Determines if the online help is available in the currently selected user language.<p>
     * 
     * @return true if the online help is found, otherwise false
     */
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getLocale() + "/", CmsResourceFilter.IGNORE_EXPIRATION);
            return true;
        } catch (CmsException e) {
            // help folder is not available
            return false;         
        }
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
     * @param editAsText "true" if the resource should be handled like a text file
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
     * Writes the content of a temporary file back to the original file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void commitTempFile() throws CmsException {
        switchToTempProject();
        CmsFile tempFile;
        List properties;
        try {
            tempFile = getCms().readFile(getParamTempfile(), CmsResourceFilter.ALL);
            properties = getCms().readPropertyObjects(getParamTempfile(), false);
        } finally {
            // make sure the project is reset in case of any exception
            switchToCurrentProject();
        }
        // update properties of original file first (required if change in encoding occured)
        getCms().writePropertyObjects(getParamResource(), properties);
        // now replace the content of the original file
        CmsFile orgFile = getCms().readFile(getParamResource(), CmsResourceFilter.ALL);
        orgFile.setContents(tempFile.getContents());
        getCms().writeFile(orgFile);
    }
    
    /**
     * Creates a temporary file which is needed while working in an editor with preview option.<p>
     * 
     * @return the file name of the temporary file
     * @throws CmsException if something goes wrong
     */
    protected String createTempFile() throws CmsException {
        // read the selected file
        CmsResource file = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
        
        // Create the filename of the temporary file
        String temporaryFilename = CmsResource.getFolderPath(getCms().getSitePath(file)) + I_CmsConstants.C_TEMP_PREFIX + file.getName();
        boolean ok = true;
        
        // switch to the temporary file project
        switchToTempProject();
        
        try {
            getCms().copyResource(getCms().getSitePath(file), temporaryFilename, I_CmsConstants.C_COPY_AS_NEW);
            getCms().touch(temporaryFilename, System.currentTimeMillis(), CmsResource.DATE_RELEASED_DEFAULT, CmsResource.DATE_EXPIRED_DEFAULT, false);
        } catch (CmsVfsResourceNotFoundException e) {
            try {
                CmsLock tempFileLock = getCms().getLock(temporaryFilename);
                if (!tempFileLock.equals(CmsLock.getNullLock())) {
                    if (!tempFileLock.getUserId().equals(getCms().getRequestContext().currentUser().getId())) {
                        // the resource is locked by another user- change the lock to the current user
                        getCms().changeLock(temporaryFilename);
                    }
                } else {
                    // the resource is not locked- create a lock for the current user
                    getCms().lockResource(temporaryFilename);
                }
                
                // try to re-use the old temporary file
                getCms().changeLastModifiedProjectId(temporaryFilename);
            } catch (Exception ex) {
                // should usually never happen
                if (LOG.isInfoEnabled()) {
                    LOG.info(ex);
                }                           
                ok = false;
            }
        } catch (CmsException e) {
            switchToCurrentProject();
            throw e;
        }

        String extendedTempFile = temporaryFilename;
        int loop = 0;
        
        while (!ok) {
            // default temporary file could not be created, try other file names
            ok = true;
            extendedTempFile = temporaryFilename + loop;
    
            try {
                getCms().copyResource(getCms().getSitePath(file), extendedTempFile);
            } catch (CmsVfsResourceNotFoundException e) {
                // temp file could not be created, try again
                loop++;
                ok = false;                
            } catch (CmsException e) {
                switchToCurrentProject();
                // This was not a "file exists" exception. Very bad.
                // We should not continue here since we may run into an endless loop.
                throw e;
            }
        }

        switchToCurrentProject();
        // We have found a temporary file!
        temporaryFilename = extendedTempFile;

        return temporaryFilename;
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
        return CmsEncoder.unescape(content, CmsEncoder.C_UTF8_ENCODING);
    }
    
    /**
     * Deletes a temporary file from the OpenCms VFS, needed when exiting an editor.<p> 
     */
    protected void deleteTempFile() {       
        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            getCms().deleteResource(getParamTempfile(), I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            // switch back to the current project
            switchToCurrentProject();
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
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
        return CmsEncoder.escapeWBlanks(content, CmsEncoder.C_UTF8_ENCODING);
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
                return CmsEncoder.decode(paramValue, CmsEncoder.C_UTF8_ENCODING);
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
            return cms.readPropertyObject(filename, I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, false).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            return OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }    
    
    /**
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected abstract void initContent();
    
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
     * @param key the suffix for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Exception exception, String key) throws JspException {
        // reset the action parameter            
        setParamAction("");                               
        showErrorPage(this, exception, key, C_FILE_DIALOG_EDITOR_CONFIRM);
        // save not successful, set cancel action 
        setAction(ACTION_CANCEL);
        return;        
    }
    
    /**
     * Shows the common error page in case of an exception.<p>
     * 
     * @param editor initialized instance of the editor class
     * @param exception the current exception
     * @param key the suffix key for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Object editor, Exception exception, String key) throws JspException {
        showErrorPage(editor, exception, key, C_FILE_DIALOG_SCREEN_ERROR);
    }
    
    /**
     * Shows the selected error page in case of an exception.<p>
     * 
     * @param editor initialized instance of the editor class
     * @param exception the current exception
     * @param key the suffix for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @param dialogUri the URI of the error dialog to use in the VFS
     * @throws JspException if inclusion of the error page fails
     */    
    protected void showErrorPage(Object editor, Exception exception, String key, String dialogUri) throws JspException {
        // save initialized instance of the editor class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, editor);
        // reading of file contents failed, show error dialog
        setAction(ACTION_SHOW_ERRORMESSAGE);
        if (exception != null) {
            setParamErrorstack(CmsException.getStackTraceAsString(exception));
        } else {
            setParamErrorstack(null);
        }

        setParamTitle(key("error.title.editor" + key));
        setParamMessage(key("error.message.editor" + key));
        String reasonSuggestion = key("error.reason.editor" + key) + "<br>\n" + key("error.suggestion.editor" + key) + "\n";
        setParamReasonSuggestion(reasonSuggestion);
        // log the error 
        if (exception != null) {
            String errorMessage = "Error while trying to " + key + " file " + getParamResource() + ": " + exception;
            if (CmsLog.getLog(editor).isWarnEnabled()) {
                CmsLog.getLog(editor).warn(errorMessage, exception);
            } else if (CmsLog.getLog(editor).isErrorEnabled()) {
                CmsLog.getLog(editor).error(errorMessage);
            }
        }
        // include the common error dialog
        getJsp().include(dialogUri);
    }
}