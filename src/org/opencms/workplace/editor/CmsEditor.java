/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditor.java,v $
 * Date   : $Date: 2004/02/06 20:52:43 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace.editor;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceAction;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;

/**
 * Provides basic methods for building the file editors of OpenCms.<p> 
 * 
 * The editor classes have to extend this class and implement action methods for common editor actions.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.23 $
 * 
 * @since 5.1.12
 */
public abstract class CmsEditor extends CmsDialog {

    /** Value for the action: change the body */
    public static final int ACTION_CHANGE_BODY = 124;

    /** Value for the action: exit */
    public static final int ACTION_EXIT = 122;

    /** Value for the action: show a preview */
    public static final int ACTION_PREVIEW = 126;
    
    /** Value for the action: save */
    public static final int ACTION_SAVE = 121;

    /** Value for the action: save and exit */
    public static final int ACTION_SAVEEXIT = 123;

    /** Value for the action: show the editor */
    public static final int ACTION_SHOW = 125;

    /** Value for the action: an error occured */
    public static final int ACTION_SHOW_ERRORMESSAGE = 127;
    
    /** Stores the VFS editor path */
    public static final String C_PATH_EDITORS = C_PATH_WORKPLACE + "editors/";

    /** Value for the action parameter: change the body */
    public static final String EDITOR_CHANGE_BODY = "changeelement";

    /** Value for the action parameter: exit editor */
    public static final String EDITOR_EXIT = "exit";

    /** Value for the action parameter: show a preview */
    public static final String EDITOR_PREVIEW = "preview";
    
    /** Value for the action parameter: save content */
    public static final String EDITOR_SAVE = "save";

    /** Value for the action parameter: save and exit */
    public static final String EDITOR_SAVEEXIT = "saveexit";

    /** Value for the action parameter: show the editor */
    public static final String EDITOR_SHOW = "show";

    /** Value for the action parameter: an error occured */
    public static final String EDITOR_SHOW_ERRORMESSAGE = "error";
    
    // some private members for parameter storage
    private String m_paramBackLink;
    private String m_paramContent;
    private String m_paramDirectedit;
    private String m_paramEditAsText;
    private String m_paramEditormode;
    private String m_paramEditTimeStamp;
    private String m_paramNoActiveX;
    private String m_paramTempFile;
    
    /** Helper variable to store the uri to the editors pictures */
    private String m_picsUri = null;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditor(CmsJspActionElement jsp) {
        super(jsp);
    }
        
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
     * @throws CmsException if something goes wrong
     * @throws IOException if a redirection fails
     * @throws JspException if including an element fails
     */
    public abstract void actionSave() throws CmsException, IOException, JspException;
    
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
        String target = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
        return "onClick=\"top.location.href='" + target + "';\"";
    }
       
    /**
     * Writes the content of a temporary file back to the original file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void commitTempFile() throws CmsException {
        // get the current project id
        int curProject = getSettings().getProject();
        // get the temporary file project id
        int tempProject = 0;
        try {
            tempProject = Integer.parseInt(getCms().getRegistry().getSystemValue("tempfileproject"));
        } catch (Exception e) {
            throw new CmsException("Can not read projectId of tempfileproject for creating temporary file for editing! "+e.toString());
        }
        // set current project to tempfileproject
        getCms().getRequestContext().setCurrentProject(tempProject);
        CmsFile tempFile = null;
        Map properties = null;
        try {
            tempFile = getCms().readFile(getParamTempfile());
            properties = getCms().readProperties(getParamTempfile());
        } catch (CmsException e) {
            getCms().getRequestContext().setCurrentProject(curProject);
            throw e;
        }
        // set current project
        getCms().getRequestContext().setCurrentProject(curProject);
        CmsFile orgFile = getCms().readFile(getParamResource());
        orgFile.setContents(tempFile.getContents());
        getCms().writeFile(orgFile);
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String keyName = (String)keys.next();
            getCms().writeProperty(getParamResource(), keyName, (String)properties.get(keyName));
        }
    }

    
    /**
     * Creates a temporary file which is needed while working in an editor with preview option.<p>
     * 
     * @return the file name of the temporary file
     * @throws CmsException if something goes wrong
     */
    protected String createTempFile() throws CmsException {
        // read the selected file
        CmsResource file = getCms().readFileHeader(getParamResource());
        
        // Create the filename of the temporary file
        String temporaryFilename = CmsResource.getFolderPath(getCms().readAbsolutePath(file)) + I_CmsConstants.C_TEMP_PREFIX + file.getName();
        boolean ok = true;
        
        // switch to the temporary file project
        int tempProject = switchToTempProject();
        
        try {
            getCms().copyResource(getCms().readAbsolutePath(file), temporaryFilename, false, true, I_CmsConstants.C_COPY_AS_NEW);
        } catch (CmsException e) {
            if ((e.getType() == CmsException.C_FILE_EXISTS) || (e.getType() != CmsException.C_SQL_ERROR)) {
                try {
                    // try to re-use the old temporary file
                    getCms().changeLockedInProject(tempProject, temporaryFilename);
                    getCms().lockResource(temporaryFilename, true);
                } catch (Exception ex) {
                    ok = false;
                }
            } else {
                switchToCurrentProject();
                throw e;
            }
        }

        String extendedTempFile = temporaryFilename;
        int loop = 0;
        
        while (!ok) {
            // default temporary file could not be created, try other file names
            ok = true;
            extendedTempFile = temporaryFilename + loop;
    
            try {
                getCms().copyResource(getCms().readAbsolutePath(file), extendedTempFile);
            } catch (CmsException e) {
                if ((e.getType() != CmsException.C_FILE_EXISTS) && (e.getType() != CmsException.C_SQL_ERROR)) {
                    switchToCurrentProject();
                    // This was not a "file exists" exception. Very bad.
                    // We should not continue here since we may run into an endless loop.
                    throw e;
                }
                // temp file could not be created, try again
                loop++;
                ok = false;
            }
        }

        switchToCurrentProject();
        // We have found a temporary file!
        temporaryFilename = extendedTempFile;

        return temporaryFilename;
    }
    
    /**
     * Deletes a temporary file from the OpenCms VFS, needed when exiting an editor.<p> 
     */
    protected void deleteTempFile() {       
        try {
            // switch to the temporary file project
            switchToTempProject();
            // delete the temporary file
            getCms().deleteResource(getParamTempfile(), I_CmsConstants.C_DELETE_OPTION_IGNORE_VFS_LINKS);
            // switch back to the current project
            switchToCurrentProject();
        } catch (CmsException e) {
            // ignore this exception
        }
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
     * Returns the time stamp parameter.<p>
     * 
     * @return the time stamp parameter
     */
    public String getParamEdittimestamp() {
        if (m_paramEditTimeStamp == null) {
            m_paramEditTimeStamp = "";
        }
        return m_paramEditTimeStamp;
    }
    
    /**
     * Returns the "no ActiveX" parameter to determine the presence of ActiveX functionality.<p>
     * 
     * @return the "no ActiveX" parameter
     */
    public String getParamNoactivex() {
        return m_paramNoActiveX;
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
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected abstract void initContent();
    
    /**
     * Determines if the online help is available in the currently selected user language.<p>
     * 
     * @return true if the online help is found, otherwise false
     */
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getLocale() + "/");
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
     * Sets the edit time stamp parameter.<p>
     * 
     * @param editTimeStamp the current time in milliseconds
     */
    public void setParamEdittimestamp(String editTimeStamp) {
        m_paramEditTimeStamp = editTimeStamp;
    }
    
    /**
     * Sets the "no ActiveX" parameter to determine the presence of ActiveX functionality.<p>
     * 
     * @param noActiveX the "no ActiveX" parameter
     */
    public void setParamNoactivex(String noActiveX) {
        m_paramNoActiveX = noActiveX;
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
     * Shows the common error page in case of an exception.<p>
     * 
     * @param theClass initialized instance of the editor class
     * @param cmsException the current exception
     * @param keySuffix the suffix for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @throws JspException if inclusion of the error page fails
     */
    protected void showErrorPage(Object theClass, CmsException cmsException, String keySuffix) throws JspException {
        // save initialized instance of the editor class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, theClass);
        // reading of file contents failed, show error dialog
        setAction(ACTION_SHOW_ERRORMESSAGE);
        setParamErrorstack(cmsException.getStackTraceAsString());
        setParamTitle(key("error.title.editor" + keySuffix));
        setParamMessage(key("error.message.editor" + keySuffix));
        String reasonSuggestion = key("error.reason.editor" + keySuffix) + "<br>\n" + key("error.suggestion.editor" + keySuffix) + "\n";
        setParamReasonSuggestion(reasonSuggestion);
        // log the error 
        String errorMessage = "Error while trying to " + keySuffix + " file " + getParamResource() + ": " + cmsException;
        if (OpenCms.getLog(theClass).isErrorEnabled()) {
            OpenCms.getLog(theClass).error(errorMessage, cmsException);
        }
        // include the common error dialog
        getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
    }

}