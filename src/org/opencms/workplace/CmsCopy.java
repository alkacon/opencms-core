/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsCopy.java,v $
 * Date   : $Date: 2004/04/28 22:29:02 $
 * Version: $Revision: 1.29 $
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
package org.opencms.workplace;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.site.CmsSiteManager;
import org.opencms.staticexport.CmsLinkManager;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the copy resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/copy_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.29 $
 * 
 * @since 5.1
 */
public class CmsCopy extends CmsDialog {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "copy";
    
    /** Value for the action: copy the resource */
    public static final int ACTION_COPY = 100;
    
    /** Request parameter name for the keep rights flag */
    public static final String PARAM_KEEPRIGHTS = "keeprights";    

    private String m_paramTarget;
    private String m_paramCopymode;
    private String m_paramKeeprights;    
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsCopy(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsCopy(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }        

    /**
     * Returns the value of the target parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The target parameter selects the target name 
     * of the operation.<p>
     * 
     * @return the value of the target parameter
     */    
    public String getParamTarget() {
        return m_paramTarget;
    }
    
    /**
     * Sets the value of the target parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamTarget(String value) {
        m_paramTarget = value;
    }    

    /**
     * Returns the value of the keeprights parameter.<p>
     * 
     * @return the value of the keeprights parameter
     */    
    public String getParamKeeprights() {
        return m_paramKeeprights;
    }
    
    /**
     * Sets the value of the "keeprights" parameter.<p>
     * 
     * @param value the value of the "keeprights" parameter
     */
    public void setParamKeeprights(String value) {
        m_paramKeeprights = value;
    } 
    
    /**
     * Returns the value of the copymode parameter.<p>
     * 
     * @return the value of the copymode parameter
     */
    public String getParamCopymode() {
        return m_paramCopymode;   
    }
    
    /**
     * Sets the value of the copymode parameter.<p>
     * 
     * @param value the value of the copymode parameter
     */
    public void setParamCopymode(String value) {
        m_paramCopymode = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_COPY);                            
        } else if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {          
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for copy dialog     
            setParamTitle(key("title.copy") + ": " + CmsResource.getName(getParamResource()));
        }      
    }
    
    /**
     * Builds the input radio buttons to select between preserving links or creating new resources when copying.<p>
     * 
     * @return the HTML code for the radio buttons
     */
    public String buildRadioCopyMode() {
        StringBuffer retValue = new StringBuffer(256);
        
        // check if the current resource is a folder
        boolean isFolder = false;
        try {
            CmsResource curRes = getCms().readFileHeader(getParamResource());
            if (curRes.isFolder()) {
                isFolder = true;        
            }
        } catch (CmsException e) {
            // ignore
        }
        String checkedAttr = " checked=\"checked\"";
        
        if (isFolder) {
            // for folders, show an additional option "preserve links"
            int defaultMode = getSettings().getUserSettings().getDialogCopyFolderMode();
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"" + I_CmsConstants.C_COPY_AS_SIBLING + "\"");
            if (defaultMode == I_CmsConstants.C_COPY_AS_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(getSettings().getMessages().key("messagebox.option.folder.assibling.copy") + "<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"" + I_CmsConstants.C_COPY_PRESERVE_SIBLING + "\"");
            if (defaultMode == I_CmsConstants.C_COPY_PRESERVE_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(getSettings().getMessages().key("messagebox.option.folder.preserve.copy") + "<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"" + I_CmsConstants.C_COPY_AS_NEW + "\"");
            if (defaultMode == I_CmsConstants.C_COPY_AS_NEW) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(getSettings().getMessages().key("messagebox.option.folder.asnewresource.copy") + "<br>\n");       
            
        } else {
            // for files, show copy option "copy as sibling" and "copy as new resource"
            int defaultMode = getSettings().getUserSettings().getDialogCopyFileMode();
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"" + I_CmsConstants.C_COPY_AS_SIBLING + "\"");
            if (defaultMode == I_CmsConstants.C_COPY_AS_SIBLING) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(getSettings().getMessages().key("messagebox.option.file.assibling.copy") + "<br>\n");
            retValue.append("<input type=\"radio\" name=\"copymode\" value=\"" + I_CmsConstants.C_COPY_AS_NEW + "\"");
            if (defaultMode == I_CmsConstants.C_COPY_AS_NEW) {
                retValue.append(checkedAttr);
            }
            retValue.append("> ");
            retValue.append(getSettings().getMessages().key("messagebox.option.file.asnewresource.copy") + "<br>\n");       
        }
        
        return retValue.toString();
    }

    /**
     * Performs the copy action, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionCopy() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        CmsResource resource = null;
        try {
            resource = getCms().readFileHeader(getParamResource());
            boolean isFolder = resource.isFolder();
            if (performCopyOperation(isFolder))  {
                // if no exception is caused and "true" is returned copy operation was successful
                if (isFolder) {
                    // set request attribute to reload the explorer tree view
                    List folderList = new ArrayList(2);
                    String target = CmsResource.getParentFolder(getParamTarget());
                    folderList.add(target);
                    getJsp().getRequest().setAttribute(C_REQUEST_ATTRIBUTE_RELOADTREE, folderList);              
                }
                actionCloseDialog(); 
            } else  {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }    
        } catch (CmsException e) {
            // prepare common message part
            String message = "<p>\n" 
                + key("source") + ": " + getParamResource() + "<br>\n" 
                + key("target") + ": " + getParamTarget() + "\n</p>\n";
            // check if this exception requires a confirmation or error screen
            if ((e.getType() == CmsException.C_FILE_EXISTS) 
            && !(resource.isFolder())) {
                // file copy but file already exists, now check target file type
                int targetType = -1;
                boolean restoreSiteRoot = false;
                try {
                    if (CmsSiteManager.getSiteRoot(getParamTarget()) != null) { 
                        getCms().getRequestContext().saveSiteRoot();
                        getCms().getRequestContext().setSiteRoot("/");
                        restoreSiteRoot = true;
                    }
                    CmsResource targetRes = getCms().readFileHeader(getParamTarget());
                    targetType = targetRes.getType();
                } catch (CmsException exc) { 
                    // ignore
                } finally {
                    if (restoreSiteRoot) {
                        getCms().getRequestContext().restoreSiteRoot();
                    }
                }
                if (resource.getType() == targetType) {               
                    // file type of target is the same as source, show confirmation dialog
                    setParamMessage(message + key("confirm.message." + getParamDialogtype()));
                    getJsp().include(C_FILE_DIALOG_SCREEN_CONFIRM); 
                } else {
                    // file type is different, create error message
                    setParamErrorstack(e.getStackTraceAsString());
                    setParamMessage(message);
                    setParamReasonSuggestion(key("error.reason.copyexists") + "<br>\n" + key("error.suggestion.copyexists") + "\n");
                    getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);      
                }
            } else {                
                // error during copy, show error dialog
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(message + key("error.message." + getParamDialogtype()));
                setParamReasonSuggestion(getErrorSuggestionDefault());
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            }
        }
    } 
    
    /**
     * Performs the resource copying.<p>
     * 
     * @param isFolder true, if the resource to copy is a folder, otherwise false
     * @return true, if the resource was copied, otherwise false
     * @throws CmsException if copying is not successful
     */
    private boolean performCopyOperation(boolean isFolder) throws CmsException {

        // on folder copy display "please wait" screen, not for simple file copy
        if (isFolder && ! DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }
        
        // get the copy mode from request parameter
        int copyMode = I_CmsConstants.C_COPY_PRESERVE_SIBLING;
        try {
            copyMode = Integer.parseInt(getParamCopymode());
        } catch (Exception e) {
            // ignore
        }

        // calculate the target name
        String target = getParamTarget();
        if (target == null) {
            target = "";
        }
        
        boolean restoreSiteRoot = false;
        try {
            // check if a site root was added to the target name
            String sitePrefix = "";
            if (CmsSiteManager.getSiteRoot(target) != null) { 
                String siteRootFolder = getCms().getRequestContext().getSiteRoot();
                if (siteRootFolder.endsWith("/")) {
                    siteRootFolder = siteRootFolder.substring(0, siteRootFolder.length()-1);
                }  
                sitePrefix = siteRootFolder;
                getCms().getRequestContext().saveSiteRoot();
                getCms().getRequestContext().setSiteRoot("/");
                restoreSiteRoot = true;
            }
            
            // calculate the target name
            target = CmsLinkManager.getAbsoluteUri(target, CmsResource.getParentFolder(getParamResource()));
    
            if (target.equals(getParamResource())) {
                throw new CmsException("Can't copy resource onto itself.", CmsException.C_FILESYSTEM_ERROR);
            }            
            
            try {
                CmsResource res = getCms().readFileHeader(target);
                if (res.isFolder()) {
                    // target folder already exists, so we add the current folder name
                    if (! target.endsWith("/")) {
                        target += "/";
                    }
                    target = target + CmsResource.getName(getParamResource());
                }
            } catch (CmsException exc) {
                // target folder does not already exist, so target name is o.k.
            }
            
            // set the target parameter value
            setParamTarget(target);        
             
            // delete existing target resource if confirmed by the user
            if (DIALOG_CONFIRMED.equals(getParamAction())) {
                getCms().deleteResource(target, I_CmsConstants.C_DELETE_OPTION_IGNORE_SIBLINGS);
            }            
            
            // copy the resource       
            getCms().copyResource(sitePrefix + getParamResource(), target, "true".equals(getParamKeeprights()), true, copyMode);
        } finally {
            if (restoreSiteRoot) {
                getCms().getRequestContext().restoreSiteRoot();
            }
        }
        return true;
    }
}
