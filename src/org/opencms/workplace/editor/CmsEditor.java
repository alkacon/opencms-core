/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editor/Attic/CmsEditor.java,v $
 * Date   : $Date: 2003/12/02 16:25:57 $
 * Version: $Revision: 1.7 $
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Provides basic methods for building the file editors of OpenCms.<p> 
 * 
 * The editor classes have to extend this class and implement action methods for common editor actions.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.1.12
 */
public abstract class CmsEditor extends CmsDialog {
    
    public static final String C_PATH_EDITORS = C_PATH_WORKPLACE + "editors/";
    
    public static final String BROWSER_IE = "IE";
    public static final String BROWSER_NS = "NS";
    
    public static final String EDITOR_SAVE = "save";
    public static final String EDITOR_EXIT = "exit";
    public static final String EDITOR_SAVEEXIT = "saveexit";
    public static final String EDITOR_CHANGE_TEMPLATE = "changetemplate";
    public static final String EDITOR_CHANGE_BODY = "changebody";
    public static final String EDITOR_SHOW = "show";
    public static final String EDITOR_PREVIEW = "preview";
    public static final String EDITOR_NEW_BODY = "newbody";
    public static final String EDITOR_SHOW_ERRORMESSAGE = "error";
    
    public static final int ACTION_SAVE = 121;
    public static final int ACTION_EXIT = 122;
    public static final int ACTION_SAVEEXIT = 123;
    public static final int ACTION_CHANGE_TEMPLATE = 124;
    public static final int ACTION_CHANGE_BODY = 125;
    public static final int ACTION_SHOW = 126;
    public static final int ACTION_PREVIEW = 127;
    public static final int ACTION_NEW_BODY = 128;
    public static final int ACTION_SHOW_ERRORMESSAGE = 129;
    
    private String m_paramEditormode;
    private String m_paramDirectedit;
    private String m_paramPageTitle;
    private String m_paramTempFile;
    private String m_paramContent;
    private String m_paramNoActiveX;
    private String m_paramEditAsText;
       
    /** Helper variable to store the clients browser type.<p> */
    private String m_browserType = null;
    
    /** Helper variable to store the id of the current project */
    private int m_currentProjectId = -1;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditor(CmsJspActionElement jsp) {
        super(jsp);
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
     * Sets the  edit as text parameter.<p>
     * 
     * @param editAsText "true" if the resource should be handled like a text file
     */
    public void setParamEditastext(String editAsText) {
        m_paramEditAsText = editAsText;
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
     * Sets the editor mode parameter.<p>
     * 
     * @param mode the editor mode parameter
     */
    public void setParamEditormode(String mode) {
        m_paramEditormode = mode;
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
     * Sets the direct edit flag parameter.<p>
     * 
     * @param direct the direct edit flag parameter
     */
    public void setParamDirectedit(String direct) {
        m_paramDirectedit = direct;
    }
    
    /**
     * Returns the page title.<p>
     * 
     * @return the page title
     */
    public String getParamPagetitle() {
        if (m_paramPageTitle == null) {
            m_paramPageTitle = "";
        }
        return m_paramPageTitle;
    }
    
    /**
     * Sets the page title.<p>
     * 
     * @param pageTitle the page title
     */
    public void setParamPagetitle(String pageTitle) {
        m_paramPageTitle = pageTitle;
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
     * Sets the name of the temporary file.<p>
     * 
     * @param fileName the name of the temporary file
     */
    public void setParamTempfile(String fileName) {
        m_paramTempFile = fileName;
    }
    
    /**
     * Returns the content of the editor.<p>
     * @return the content of the editor
     */
    public String getParamContent() {
        return m_paramContent;
    }
    
    /**
     * Sets the content of the editor.<p>
     * 
     * @param content the content of the editor
     */
    public void setParamContent(String content) {
        m_paramContent = content;
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
     * Sets the "no ActiveX" parameter to determine the presence of ActiveX functionality.<p>
     * 
     * @param noActiveX the "no ActiveX" parameter
     */
    public void setParamNoactivex(String noActiveX) {
        m_paramNoActiveX = noActiveX;
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
        //      get the current project id
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
     * Returns the browser type currently used by the client.<p>
     * 
     * @return the brwoser type
     */
    public String getBrowserType() {
        if (m_browserType == null) {
            HttpServletRequest orgReq = (HttpServletRequest)getCms().getRequestContext().getRequest().getOriginalRequest();
            String browser = orgReq.getHeader("user-agent");
            if (browser.indexOf("MSIE") > -1) {
                m_browserType = BROWSER_IE;
            } else {
                m_browserType = BROWSER_NS;
            }
        }
        return m_browserType; 
    }
    
    /**
     * Determines if the online help is available in the currently selected user language.<p>
     * 
     * @return true if the online help is found, otherwise false
     */
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getSettings().getLanguage());
            return true;
        } catch (CmsException e) {
            // help folder is not available
            return false;         
        }
    }
    
    /**
     * Shows the common error page in case of an exception.<p>
     * 
     * @param theClass initialized instance of the editor class
     * @param cmsException the current exception
     * @param keySuffix the suffix for the localized error messages, e.g. "save" for key "error.message.editorsave"
     * @throws JspException if inclusion of the error page fails
     */
    public void showErrorPage(Object theClass, CmsException cmsException, String keySuffix) throws JspException {
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
    
    /**
     * Helper method to change back from the temporary project to the current project.<p>
     * 
     * @throws CmsException if switching back fails
     */
    protected void switchToCurrentProject() throws CmsException {
        if (m_currentProjectId != -1) {
            // switch back to the current users project
            getCms().getRequestContext().setCurrentProject(m_currentProjectId); 
        }
    }
    
    /**
     * Helper method to change the current project to the temporary file project.<p>
     * 
     * The id of the old project is stored in a member variable to switch back.<p>
     * 
     * @return the id of the tempfileproject
     * @throws CmsException if getting the tempfileproject id fails
     */
    protected int switchToTempProject() throws CmsException {
        // store the current project id in member variable
        m_currentProjectId = getSettings().getProject();
        // get the temporary file project id
        int tempProject = 0;
        try {
            tempProject = Integer.parseInt(getCms().getRegistry().getSystemValue("tempfileproject"));
        } catch (Exception e) {
            throw new CmsException("Can not read projectId of tempfileproject for creating temporary file for editing! "+e.toString());
        }
        getCms().getRequestContext().setCurrentProject(tempProject);
        return tempProject;
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
     * Initializes the editor content when openening the editor for the first time.<p>
     */
    protected abstract void initContent();

}