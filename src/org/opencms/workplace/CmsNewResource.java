/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResource.java,v $
 * Date   : $Date: 2004/03/12 17:03:42 $
 * Version: $Revision: 1.1 $
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
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
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.3
 */
public class CmsNewResource extends CmsDialog {
    
    /** The dialog type */
    public static final String DIALOG_TYPE = "newresource";

    /** Constant for the "Next" button in the build button methods */
    public static final int BUTTON_NEXT = 20;
    
    /** Request parameter name for the new resource uri */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";
    
    private String m_paramNewResourceUri;
    
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
                // no new resource URI specified for the current settings, dont't show them
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
                    // the type has no permission for the current user to be created
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
     * 
     * @throws JspException
     */
    public void actionNext() throws JspException, IOException {
        // String nextUri = C_PATH_DIALOGS + getParamNewResourceUri();
        // getJsp().include(nextUri);
        String nextUri = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/" + getParamNewResourceUri();
        sendCmsRedirect(nextUri);
    }
    
    /**
     * Builds a button row with an "next" and a "cancel" button.<p>
     * 
     * @return the button row 
     */
    public String dialogButtonRowNextCancel() {
        return dialogButtonRow(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[2]);
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

}
