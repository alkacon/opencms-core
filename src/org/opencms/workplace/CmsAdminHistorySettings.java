/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsAdminHistorySettings.java,v $
 * Date   : $Date: 2003/09/11 12:04:49 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsRegistry;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the history settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/system/workplace/administration/history/settings/index.html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsAdminHistorySettings extends CmsDialog {
    
    public static final int ACTION_SAVE_EDIT = 300;
    
    public static final String DIALOG_SAVE_EDIT = "saveedit";
    
    public static final String DIALOG_TYPE = "historysettings";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminHistorySettings(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminHistorySettings(PageContext context, HttpServletRequest req, HttpServletResponse res) {
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
        if (DIALOG_SAVE_EDIT.equals(getParamAction())) {
            setAction(ACTION_SAVE_EDIT);
        } else { 
            // set the default action               
            setAction(ACTION_DEFAULT); 
            setParamTitle(key("label.admin.history.settings"));
        }      
    } 
    
    /**
     * Builds the HTML for the history settings input form.<p>
     * 
     * @return the HTML code for the history settings input form
     */
    public String buildSettingsForm() {
        StringBuffer retValue = new StringBuffer(512);
        CmsRegistry reg = null;
        int maxVersions = -1;
        boolean histEnabled = false;
        try {
            reg = getCms().getRegistry();
            histEnabled = reg.getBackupEnabled();
            maxVersions = reg.getMaximumBackupVersions();
        } catch (CmsException e) { }
        
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("<td>" + key("input.histenabled") + "</td>\n");
        retValue.append("<td><input type=\"radio\" name=\"enable\" id=\"enabled\" value=\"true\" onclick=\"checkEnabled();\"");
        if (histEnabled) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append("></td>\n");
        retValue.append("<td>" + key("input.histenabled.yes") + "</td>\n");
        retValue.append("<td>&nbsp;</td>\n");
        retValue.append("<td><input type=\"radio\" name=\"enable\" id=\"disabled\" value=\"false\" onclick=\"checkEnabled();\"");
        if (!histEnabled) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append("></td>\n");
        retValue.append("<td>" + key("input.histenabled.no") + "</td>\n");
        retValue.append("</tr>\n");
        retValue.append("</table>\n");

        retValue.append("<div class=\"hide\" id=\"settings\">\n");
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("<td>" + key("input.histnumber") + "</td>\n");
        retValue.append("<td colspan=\"5\"><input type=\"text\" name=\"versions\" value=\"");
        if (maxVersions != -1) {
            retValue.append(maxVersions);
        }
        retValue.append("\"></td>\n");
        retValue.append("</tr>\n");
        retValue.append("</table>\n");
        retValue.append("</div>\n");
        
        return retValue.toString(); 
    }
    
    /**
     * Performs the change of the history settings, this method is called by the JSP.<p>
     * 
     * @param request the HttpServletRequest
     * @throws JspException if something goes wrong
     */
    public void actionEdit(HttpServletRequest request) throws JspException {
        try {
            performEditOperation(request);
            // set the request parameters before returning to the overview
            getCms().getRequestContext().getResponse().sendCmsRedirect(getAdministrationBackLink());              
        } catch (CmsException e) {
            // error defining property, show error dialog
            setParamErrorstack(e.getStackTraceAsString());
            setParamReasonSuggestion(getErrorSuggestionDefault());
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } catch (IOException exc) {
          getJsp().include(C_FILE_EXPLORER_FILELIST);
        }
    }
    
    /**
     * Performs the change of the history settings.<p>
     * 
     * @param request the HttpServletRequest
     * @return true if everything was ok
     * @throws CmsException if something goes wrong
     */
    private boolean performEditOperation(HttpServletRequest request) throws CmsException {
        // get the new settings from the request parameters
        String paramEnabled = request.getParameter("enable");
        String paramVersions = request.getParameter("versions");
        
        // check the submitted values
        boolean enabled = "true".equals(paramEnabled);
        int versions = 0;
        try {
            versions = Integer.parseInt(paramVersions);
        } catch (NumberFormatException e) {
            // no int value submitted, throw exception
            throw new CmsException("No integer value entered", CmsException.C_BAD_NAME, e);
        }
        if (versions < 1) {
            // version value too low, throw exception
            throw new CmsException("The entered version value must not be smaller than 1", CmsException.C_BAD_NAME);
        }
        
        // write the changes to the registry
        CmsRegistry reg = getCms().getRegistry();
        reg.setBackupEnabled(enabled);
        reg.setMaximumBackupVersions(versions);
             
        return true;
    }

}
