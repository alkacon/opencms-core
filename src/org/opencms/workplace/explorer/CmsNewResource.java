/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResource.java,v $
 * Date   : $Date: 2005/04/17 18:07:16 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource entry dialog which displays the possible "new actions" for the current user.<p>
 * 
 * It handles the creation of "simple" resource types like plain or JSP resources.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @author Armen Markarian (a.markarian@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.3.3
 */
public class CmsNewResource extends CmsDialog {
    
    /** The value for the resource name form action. */
    public static final int ACTION_NEWFORM = 100;
    
    /** The value for the resource name form submission action. */
    public static final int ACTION_SUBMITFORM = 110;

    /** Constant for the "Next" button in the build button methods. */
    public static final int BUTTON_NEXT = 20;
    
    /** The name for the resource form action. */
    public static final String DIALOG_NEWFORM = "newform";
    
    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM = "submitform";
    
    /** The dialog type. */
    public static final String DIALOG_TYPE = "newresource";
    
    /** Request parameter name for the current folder name. */
    public static final String PARAM_CURRENTFOLDER = "currentfolder";
    
    /** Request parameter name for the new resource edit properties flag. */
    public static final String PARAM_NEWRESOURCEEDITPROPS = "newresourceeditprops";
    
    /** Request parameter name for the new resource type. */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";
    
    /** Request parameter name for the new resource uri. */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";
    
    private String m_page;
    private String m_paramCurrentFolder;
    private String m_paramNewResourceEditProps; 
    private String m_paramNewResourceType;    
    private String m_paramNewResourceUri;
    private String m_paramPage;
    
    /** a boolean flag that indicates if the create resource operation was successfull or not. */
    private boolean m_resourceCreated;
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResource(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResource(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }    
    
    /**
     * Creates the resource using the specified resource name and the newresourcetype parameter.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {
        try {
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            // create the folder            
            getCms().createResource(
                fullResourceName, 
                OpenCms.getResourceManager().getResourceType(getParamNewResourceType()).getTypeId());           
            setParamResource(fullResourceName);    
            setResourceCreated(true);
        } catch (CmsException e) {
            // error creating file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(CmsException.getStackTraceAsString(e));
            setParamMessage(key("error.message.newresource"));
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);            
        }
    }
    
    /**
     * Redirects to the property dialog if the resourceeditprops parameter is true.<p>
     * 
     * If the parameter is not true, the dialog will be closed.<p>
     * 
     * @throws IOException if redirecting to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    public void actionEditProperties() throws IOException, JspException {
        
        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();
        if (editProps) {
            // edit properties checkbox checked, redirect to property dialog
            String params = "?" + PARAM_RESOURCE + "=" + CmsEncoder.encode(getParamResource());
            params += "&" + CmsPropertyAdvanced.PARAM_DIALOGMODE + "=" + CmsPropertyAdvanced.MODE_WIZARD; 
            sendCmsRedirect(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER + params);
        } else {
            // edit properties not checked, close the dialog
            actionCloseDialog();
        }        
    }
    
    /**
     * Redirects to the next page of the new resource wizard after selecting the new resource type.<p>
     * 
     * @throws IOException if redirection fails
     */
    public void actionSelect() throws IOException {
        String nextUri = C_PATH_DIALOGS + getParamNewResourceUri();
        if (nextUri.indexOf("initial=true") == -1) {
            setParamAction(DIALOG_NEWFORM);
            String paramSep = "?";
            if (nextUri.indexOf("?") != -1) {
                paramSep = "&";
            }
            sendCmsRedirect(nextUri + paramSep + paramsAsRequest());
        } else {
            try {
                getJsp().include(nextUri);
            } catch (JspException e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }                
                // JSP dialog not present, display legacy XMLTemplate dialog
                nextUri = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/" + getParamNewResourceUri();
                sendCmsRedirect(nextUri);
            }
        }
    }
    
    /**
     * Builds the html for the list of possible new resources.<p>
     *  
     * @param attributes optional attributes for the radio input tags
     * @return the html for the list of possible new resources
     */
    public String buildNewList(String attributes) {

        StringBuffer result = new StringBuffer(1024);
        Iterator i = OpenCms.getWorkplaceManager().getExplorerTypeSettings().iterator();
        result.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");

        while (i.hasNext()) {
            CmsExplorerTypeSettings currSettings = (CmsExplorerTypeSettings)i.next();

            // check for the "new resource" page
            if (m_page == null) {                
                if (CmsStringUtil.isNotEmpty(currSettings.getNewResourcePage())) {
                    continue;
                }
            } else if (!m_page.equals(currSettings.getNewResourcePage())) {
                continue;
            }
            
            if (CmsStringUtil.isEmpty(currSettings.getNewResourceUri())) {
                // no new resource URI specified for the current settings, dont't show the type
                continue;
            } 
            
            // check permissions for the type
            CmsPermissionSet permissions;
            try {
                // get permissions of the current user
                permissions = currSettings.getAccess().getAccessControlList().getPermissions(
                    getSettings().getUser(),
                    getCms().getGroupsOfUser(getSettings().getUser().getName()));
            } catch (CmsException e) {
                // error reading the groups of the current user
                permissions = currSettings.getAccess().getAccessControlList().getPermissions(
                    getSettings().getUser());
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error reading groups of user " + getSettings().getUser().getName());
                }
            }
            if (permissions.getPermissionString().indexOf("+c") == -1) {
                // the type has no permission for the current user to be created, don't show the type
                continue;
            }

            result.append("<tr>\n");
            result.append("\t<td><input type=\"radio\" name=\"");
            result.append(PARAM_NEWRESOURCEURI);
            result.append("\"");
            result.append(" value=\"" + CmsEncoder.encode(currSettings.getNewResourceUri()) + "\"");
            if (attributes != null && !"".equals(attributes)) {
                result.append(" " + attributes);
            }
            result.append("></td>\n");
            result.append("\t<td><img src=\""
                + getSkinUri()
                + "filetypes/"
                + currSettings.getIcon()
                + "\" border=\"0\" title=\""
                + key(currSettings.getKey())
                + "\"></td>\n");
            result.append("\t<td>" + key(currSettings.getKey()) + "</td>\n");
            result.append("</tr>\n");

        }
        result.append("</table>\n");

        return result.toString();
    }
    
    /**
     * Builds a button row with an "next" and a "cancel" button.<p>
     * 
     * @param nextAttrs optional attributes for the next button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row 
     */
    public String dialogButtonsNextCancel(String nextAttrs, String cancelAttrs) {
        return dialogButtons(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[] {nextAttrs, cancelAttrs});
    }
    
    /**
     * Returns the current folder set by the http request.<p>
     *  
     * If the request parameter value is null/empty then returns the default computed folder.<p>
     *
     * @return the current folder set by the request param or the computed current folder
     */
    public String getParamCurrentFolder() {

        if (CmsStringUtil.isEmpty(m_paramCurrentFolder)) {
            return computeCurrentFolder();
        }
        
        return m_paramCurrentFolder;
    }
    
    /**
     * Returns the new resource edit properties flag parameter.<p>
     * 
     * @return the new resource edit properties flag parameter
     */
    public String getParamNewResourceEditProps() {
        return m_paramNewResourceEditProps;
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
     * Returns the new resource URI parameter.<p>
     * 
     * @return the new resource URI parameter
     */
    public String getParamNewResourceUri() {
        return m_paramNewResourceUri;
    }
        
    /**
     * Returns the paramPage.<p>
     *
     * @return the paramPage
     */
    public String getParamPage() {

        return m_paramPage;
    }
    
    
    /**
     * Returns true if the resource is created successfully; otherwise false.<p>
     * 
     * @return true if the resource is created successfully; otherwise false
     */
    public boolean isResourceCreated() {

        return m_resourceCreated;
    }
    /**
     * Sets the current folder.<p>
     *
     * @param paramCurrentFolder the current folder to set
     */
    public void setParamCurrentFolder(String paramCurrentFolder) {

        m_paramCurrentFolder = paramCurrentFolder;
    }
    
    /**
     * Sets the new resource edit properties flag parameter.<p>
     * 
     * @param newResourceEditProps the new resource edit properties flag parameter
     */
    public void setParamNewResourceEditProps(String newResourceEditProps) {
        m_paramNewResourceEditProps = newResourceEditProps;
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
     * Sets the new resource URI parameter.<p>
     * 
     * @param newResourceUri the new resource URI parameter
     */
    public void setParamNewResourceUri(String newResourceUri) {
        m_paramNewResourceUri = newResourceUri;
    }
    /**
     * Sets the paramPage.<p>
     *
     * @param paramPage the paramPage to set
     */
    public void setParamPage(String paramPage) {

        m_paramPage = paramPage;
    }
    
    /**
     * Sets the boolean flag successfullyCreated.<p>
     *   
     * @param successfullyCreated a boolean flag that indicates if the create resource operation was successfull or not
     */
    public void setResourceCreated(boolean successfullyCreated) {

        m_resourceCreated = successfullyCreated;
    }
    
    /**
     * Returns the full path of the current workplace folder.<p>
     * 
     * @return the full path of the current workplace folder
     */
    protected String computeCurrentFolder() {
        String currentFolder = getSettings().getExplorerResource();
        if (currentFolder == null) {
            // set current folder to root folder
            try {
                currentFolder = getCms().getSitePath(
                    getCms().readFolder(I_CmsConstants.C_ROOT, CmsResourceFilter.IGNORE_EXPIRATION));
            } catch (CmsException e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }                
                currentFolder = I_CmsConstants.C_ROOT;
            }
        }           
        if (!currentFolder.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            // add folder separator to currentFolder
            currentFolder += I_CmsConstants.C_FOLDER_SEPARATOR;
        }
        return currentFolder;
    }
    
    /**
     * Appends the full path to the new resource name given in the resource parameter.<p>
     * 
     * @return the full path of the new resource
     */
    protected String computeFullResourceName() {
       
        // return the full resource name
        // get the current folder
        String currentFolder = getParamCurrentFolder();
        if (CmsStringUtil.isEmpty(currentFolder)) {
            currentFolder = computeCurrentFolder();
        }
        return currentFolder + getParamResource();
    }
    
    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {
        attribute = appendDelimiter(attribute);

        switch (button) {
        case BUTTON_NEXT :
            result.append("<input name=\"next\" type=\"submit\" value=\"");
            result.append(key("button.nextscreen"));
            result.append("\" class=\"dialogbutton\"");
            result.append(attribute);
            result.append(">\n");
            break;
        default :
            super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        
        if (CmsStringUtil.isNotEmpty(getParamPage())) {
            m_page = getParamPage();
            setParamAction(null);
            setParamNewResourceUri(null);
            setParamPage(null);
        }
        
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);                            
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);  
        } else if (DIALOG_NEWFORM.equals(getParamAction())) {
            setAction(ACTION_NEWFORM);
            setParamTitle(key("title.new" + getParamNewResourceType()));
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {                        
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.new"));
        }      
    }
    
}
