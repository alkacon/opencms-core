/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResource.java,v $
 * Date   : $Date: 2004/03/18 16:13:59 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource entry dialog which displays the possible "new actions" for the current user.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.5 $
 * 
 * @since 5.3.3
 */
public class CmsNewResource extends CmsDialog {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "newresource";
    /** The name for the resource form action */
    public static final String DIALOG_NEWFORM = "newform";
    /** The name for the resource form submission action */
    public static final String DIALOG_SUBMITFORM = "submitform";
    
    /** The value for the resource name form action */
    public static final int ACTION_NEWFORM = 100;
    /** The value for the resource name form submission action */
    public static final int ACTION_SUBMITFORM = 110;

    /** Constant for the "Next" button in the build button methods */
    public static final int BUTTON_NEXT = 20;
    
    /** Request parameter name for the new resource uri */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";
    /** Request parameter name for the new resource type */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";
    /** Request parameter name for the new resource edit properties flag */
    public static final String PARAM_NEWRESOURCEEDITPROPS = "newresourceeditprops";
    
    private String m_paramNewResourceUri;
    private String m_paramNewResourceType;
    private String m_paramNewResourceEditProps;
    
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
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
            if ("".equals(currSettings.getNewResourceUri())) {
                // no new resource URI specified for the current settings, dont't show the type
                continue;
            } else {
                // check permissions for the type
                CmsPermissionSet permissions;
                try {
                    // get permissions of the current user
                    permissions = currSettings.getAccessControlList().getPermissions(getSettings().getUser(), getCms().getGroupsOfUser(getSettings().getUser().getName()));
                } catch (CmsException e) {
                    // error reading the groups of the current user
                    permissions = currSettings.getAccessControlList().getPermissions(getSettings().getUser());
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error reading groups of user " + getSettings().getUser().getName());
                    }      
                }
                if (permissions.getPermissionString().indexOf("+c") == -1) {
                    // the type has no permission for the current user to be created, don't show the type
                    continue;
                }
            }
            result.append("<tr>\n");
            result.append("\t<td><input type=\"radio\" name=\"" + PARAM_NEWRESOURCEURI + "\"");
            result.append(" value=\"" + CmsEncoder.encode(currSettings.getNewResourceUri()) + "\"");
            if (attributes != null && !"".equals(attributes)) {
                result.append(" " + attributes);
            }
            result.append("></td>\n");
            result.append("\t<td><img src=\"" + getSkinUri() + "filetypes/" + currSettings.getIcon() + "\" border=\"0\" title=\"" + key(currSettings.getKey()) + "\"></td>\n");
            result.append("\t<td>" + key(currSettings.getKey()) + "</td>\n");
            result.append("</tr>\n");
            
        }
        result.append("</table>\n");
        return result.toString();
    }
    
    /**
     * Creates the resource using the specified resource name and the newresourcetype parameter.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {
        try {
            // store the new resource name
            String resourceName = getParamResource();
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            // create the folder            
            getCms().createResource(CmsResource.getParentFolder(fullResourceName), resourceName, getCms().getResourceTypeId(getParamNewResourceType()));           
            setParamResource(fullResourceName);          
        } catch (CmsException e) {
            // error creating file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
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
            params += "&" + CmsProperty.PARAM_DIALOGMODE + "=" + CmsProperty.MODE_WIZARD; 
            sendCmsRedirect(CmsWorkplace.C_PATH_DIALOGS + "property.html" + params);
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
                // JSP dialog not present, display legacy XMLTemplate dialog
                nextUri = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/" + getParamNewResourceUri();
                sendCmsRedirect(nextUri);
            }
        }
    }
    
    /**
     * Appends the full path to the new resource name given in the resource parameter.<p>
     * 
     * @return the full path of the new resource
     */
    protected String computeFullResourceName() {
        String currentFolder = getSettings().getExplorerResource();
        if (currentFolder == null) {
            // set current folder to root folder
            try {
                currentFolder = getCms().readAbsolutePath(getCms().rootFolder());
            } catch (CmsException e) {
                currentFolder = I_CmsConstants.C_ROOT;
            }
        }           
        if (!currentFolder.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            // add folder separator to currentFolder
            currentFolder += I_CmsConstants.C_FOLDER_SEPARATOR;
        }
        // return the full resource name
        return currentFolder + getParamResource();
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
     * Returns the new resource URI parameter.<p>
     * 
     * @return the new resource URI parameter
     */
    public String getParamNewResourceUri() {
        return m_paramNewResourceUri;
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
     * Returns the new resource edit properties flag parameter.<p>
     * 
     * @return the new resource edit properties flag parameter
     */
    public String getParamNewResourceEditProps() {
        return m_paramNewResourceEditProps;
    }
    
    /**
     * Sets the new resource edit properties flag parameter.<p>
     * 
     * @param newResourceEditProps the new resource edit properties flag parameter
     */
    public void setParamNewResourceEditProps(String newResourceEditProps) {
        m_paramNewResourceEditProps = newResourceEditProps;
    }

}
