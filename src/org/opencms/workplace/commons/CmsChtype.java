/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsChtype.java,v $
 * Date   : $Date: 2005/03/09 16:51:03 $
 * Version: $Revision: 1.6 $
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

package org.opencms.workplace.commons;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The change resource type dialog handles the change of a resource type of a single VFS file.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/chtype.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.6 $
 * 
 * @since 5.5.0
 */
public class CmsChtype extends CmsDialog {
    
    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "chtype";
       
    /** Request parameter name for the new resource type.<p> */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";
    
    private String m_paramNewResourceType;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsChtype(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChtype(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }    
    
    /**
     * Builds the html for the list of possible types for the uploaded file.<p>
     * 
     * This method can be used by all workplace dialog classes to build 
     * radio input buttons to select a resource type.<p>
     * 
     * @param dialog the dialog class instance which creates the type list
     * @param useTypeId if true, the resource type ID will be used for value attributes, otherwise the resource type names 
     * @return the list of possible files for the uploaded resource
     */
    public static String buildTypeList(CmsDialog dialog, boolean useTypeId) {
        StringBuffer result = new StringBuffer(512);        
        try {
            // get current Cms object
            CmsObject cms = dialog.getCms();
            // determine resource type id of resource to change
            CmsResource res = cms.readResource(dialog.getParamResource(), CmsResourceFilter.ALL);
            int currentResTypeId = res.getTypeId();
            // get all available explorer type settings
            List resTypes = OpenCms.getWorkplaceManager().getExplorerTypeSettings();
            boolean isFolder = res.isFolder();
            // loop through all visible resource types
            for (int i=0; i<resTypes.size(); i++) {                
                boolean changeable = false;                
                // get explorer type settings for current resource type
                CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)resTypes.get(i);
                
                // only if settings is a real resourcetype
                boolean isResourceType;
                try {
                    OpenCms.getResourceManager().getResourceType(settings.getName());
                    isResourceType = true;
                } catch (CmsLoaderException e) {
                    isResourceType = false;
                }
                                
                if (isResourceType) {
                    int resTypeId = OpenCms.getResourceManager().getResourceType(settings.getName()).getTypeId();
                    // determine if this resTypeId is changeable by currentResTypeId
                    
                    // changeable is true if current resource is a folder and this resource type also
                    if (isFolder && OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {
                        changeable = true;
                    } else if (!isFolder && !OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {
                        // changeable is true if current resource is NOT a folder and this resource type also NOT                    
                        changeable = true;
                    }   
                    
                    if (changeable) {
                        // determine if this resource type is editable for the current user
                        CmsPermissionSet permissions;
                        try {
                            // get permissions of the current user
                            permissions = settings.getAccess().getAccessControlList().getPermissions(cms.getRequestContext().currentUser(), cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()));
                        } catch (CmsException e) {
                            // error reading the groups of the current user
                            permissions = settings.getAccess().getAccessControlList().getPermissions(cms.getRequestContext().currentUser());
                            if (OpenCms.getLog(dialog.getClass()).isErrorEnabled()) {
                                OpenCms.getLog(dialog.getClass()).error("Error reading groups of user " + cms.getRequestContext().currentUser().getName());
                            }      
                        }
                        
                        String permString = permissions.getPermissionString();
                        if (permString.indexOf("+w") == -1 || permString.indexOf("+c") == -1) {
                            // skip resource types without needed write or create permissions
                            continue;    
                        }
                        // create table row with input radio button
                        result.append("<tr><td>");
                        result.append("<input type=\"radio\" name=\"");
                        result.append(PARAM_NEWRESOURCETYPE);
                        result.append("\" value=\"");
                        if (useTypeId) {
                            // use resource type id as value
                            result.append(resTypeId);
                        } else {
                            // use resource type name as value
                            result.append(settings.getName());    
                        }
                        result.append("\"");
                        if (resTypeId == currentResTypeId) {
                            result.append(" checked=\"checked\"");
                        }
                        result.append("></td>");
                        result.append("\t<td><img src=\"");
                        result.append(getSkinUri());
                        result.append("filetypes/");
                        result.append(settings.getIcon());
                        result.append("\" border=\"0\" title=\"");
                        result.append(dialog.key(settings.getKey()));
                        result.append("\"></td>\n");
                        result.append("<td>");
                        result.append(dialog.key(settings.getKey()));
                        result.append("</td></tr>\n");
                    }
                }
            }
        } catch (CmsException e) {
            // error reading the VFS resource, log error
            if (OpenCms.getLog(dialog.getClass()).isErrorEnabled()) {
                OpenCms.getLog(dialog.getClass()).error("Error building resource type list for " + dialog.getParamResource());
            }
        }      
        return result.toString();
    }
    
    /**
     * Uploads the specified file and replaces the VFS file.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionChtype() throws JspException {
        try {
            int newType = CmsResourceTypePlain.C_RESOURCE_TYPE_ID;
            try {
                // get new resource type id from request
                newType = Integer.parseInt(getParamNewResourceType());
            } catch (NumberFormatException nf) {
                throw new CmsException("Error getting new resource type id from request", CmsException.C_BAD_NAME);    
            }
            // check the resource lock state
            checkLock(getParamResource());
            // change the resource type
            getCms().chtype(getParamResource(), newType);
            // close the dialog window
            actionCloseDialog();
        } catch (CmsException e) {
            // error changing resource type, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message." + DIALOG_TYPE));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Builds the html for the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {
        return buildTypeList(this, true);
    }
    
    /**
     * Returns the new resource type parameter.<p>
     * 
     * @return the new resource type parameter
     */
    public String getParamNewResourceType() {
        return m_paramNewResourceType;
    }
    
    /**
     * Sets the new resource type parameter.<p>
     * 
     * @param newResourceType the new resource type parameter
     */
    public void setParamNewResourceType(String newResourceType) {
        m_paramNewResourceType = newResourceType;
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
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed, change file type
            setAction(ACTION_OK);                            
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else {
            // first call of dialog
            setAction(ACTION_DEFAULT);
            // build title for change file type dialog     
            setParamTitle(key("title.chtype") + ": " + CmsResource.getName(getParamResource()));
        }   
    }
   
}
