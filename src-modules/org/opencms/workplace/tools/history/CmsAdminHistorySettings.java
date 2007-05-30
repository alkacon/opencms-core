/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/history/Attic/CmsAdminHistorySettings.java,v $
 * Date   : $Date: 2007/05/30 15:35:53 $
 * Version: $Revision: 1.12.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.history;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

/**
 * Provides methods for the history settings dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/system/workplace/administration/history/settings/index.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.12.4.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminHistorySettings extends CmsDialog {

    /** Value for the action: save the settings. */
    public static final int ACTION_SAVE_EDIT = 300;

    /** Request parameter value for the action: save the settings. */
    public static final String DIALOG_SAVE_EDIT = "saveedit";

    /** The dialog type. */
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
     * Performs the change of the history settings, this method is called by the JSP.<p>
     * 
     * @param request the HttpServletRequest
     * @throws JspException if something goes wrong
     */
    public void actionEdit(HttpServletRequest request) throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        try {
            performEditOperation(request);
            // set the request parameters before returning to the overview
            actionCloseDialog();
        } catch (CmsIllegalArgumentException e) {
            // error setting history values, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds the HTML for the history settings input form.<p>
     * 
     * @return the HTML code for the history settings input form
     */
    public String buildSettingsForm() {

        StringBuffer retValue = new StringBuffer();
        boolean histEnabled = OpenCms.getSystemInfo().isHistoryEnabled();
        int historyVersionsAfterDeletion = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
        CmsMessages messages = Messages.get().getBundle(getLocale());
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("<td>" + messages.key(Messages.GUI_INPUT_HISTENABLED_0) + "</td>\n");
        retValue.append("<td><input type=\"radio\" name=\"enable\" id=\"enabled\" value=\"true\" onclick=\"checkEnabled();\"");
        if (histEnabled) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append("></td>\n");
        retValue.append("<td>" + messages.key(Messages.GUI_INPUT_HISTENABLE_YES_0) + "</td>\n");
        retValue.append("<td>&nbsp;</td>\n");
        retValue.append("<td><input type=\"radio\" name=\"enable\" id=\"disabled\" value=\"false\" onclick=\"checkEnabled();\"");
        if (!histEnabled) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append("></td>\n");
        retValue.append("<td>" + messages.key(Messages.GUI_INPUT_HISTENABLE_NO_0) + "</td>\n");
        retValue.append("</tr>\n");
        retValue.append("</table>\n");

        retValue.append("<div class=\"show\" >\n");
        retValue.append("<table border=\"0\">\n");
        retValue.append("<tr>\n");
        retValue.append("<td>" + messages.key(Messages.GUI_INPUT_HISTNUMBER_0) + "</td>\n");
        retValue.append("<td colspan=\"5\">\n" + buildSelectVersionNumbers("versions", null) + "</td>\n");
        retValue.append("</tr>\n");

        retValue.append("<tr>\n");
        retValue.append("<td colspan=\"5\">"
            + messages.key(Messages.GUI_INPUT_HISTORY_RESTORE_DELETED_RESOURCES_0)
            + "</td>\n");
        retValue.append("<td><input type=\"checkbox\" name=\"restoreDeleted\" id=\"settingsRestore\" value=\"true\" onclick=\"checkDeletedVersionsEnabled();\"");
        if (historyVersionsAfterDeletion != 0) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append(">");
        retValue.append("<input type=\"hidden\" name=\"restoreDeletedHidden\" id=\"restoreDeletedHidden\" value=\"\"></td>\n");
        retValue.append("</tr>\n");

        retValue.append("<tr id=\"keepDeleted\">\n");
        retValue.append("<td colspan=\"5\">"
            + "&nbsp;&nbsp;"
            + messages.key(Messages.GUI_INPUT_HISTORY_KEEP_DELETED_RESOURCES_0)
            + "</td>\n");
        retValue.append("<td><input type=\"checkbox\" name=\"versionsDeleted\" id=\"settingsKeep\" value=\"true\" onclick=\"updateHiddenFields();\"");
        if (historyVersionsAfterDeletion > 1 || historyVersionsAfterDeletion == -1) {
            retValue.append(" checked=\"checked\"");
        }
        retValue.append(">");
        retValue.append("<input type=\"hidden\" name=\"versionsDeletedHidden\" id=\"versionsDeletedHidden\" value=\"\"></td>\n");
        retValue.append("</tr>\n");

        retValue.append("</table>\n");
        retValue.append("</div>\n");

        return retValue.toString();
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
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
            setParamTitle(Messages.get().getBundle(getLocale()).key(Messages.GUI_LABEL_ADMNIN_HISTORY_SETTINGS_0));
        }
    }

    /**
     * Creates the HTML code for a select box with integer values.<p>
     * 
     * @param fieldName the name of the select box
     * @param attributes the optional tag attributes
     * 
     * @return the HTML code for the select box
     */
    private String buildSelectVersionNumbers(String fieldName, String attributes) {

        StringBuffer retValue = new StringBuffer();

        retValue.append("<select id=\"settingsSelect\" name=\"" + fieldName + "\"");
        if (attributes != null) {
            retValue.append(" " + attributes);
        }
        retValue.append(">\n");
        int defaultHistoryVersions = OpenCms.getSystemInfo().getHistoryVersions();
        int historyVersions = 0;
        boolean hasSelected = false;
        String selected = "";
        // Iterate from 1 to 50 with a stepping of 1 for the first 10 entries and a stepping of five for the entries from 10 to 50
        while (historyVersions < 50) {
            historyVersions++; // increment the history version
            // Check if the current history version is the default history version and mark it as selected                        
            if (historyVersions == defaultHistoryVersions) {
                selected = " selected=\"selected\" ";
                hasSelected = true;
            }
            if (historyVersions % 5 == 0 || historyVersions <= 10 || hasSelected) {
                retValue.append("\t<option value=\""
                    + historyVersions
                    + "\""
                    + selected
                    + ">"
                    + historyVersions
                    + "</option>\n");
            }
            selected = ""; // Reset the selected string
            hasSelected = false;

        }
        // If the default setting for the version history is more than 50
        if (defaultHistoryVersions > historyVersions) {
            hasSelected = true;
            selected = " selected=\"selected\" ";
            retValue.append("\t<option value=\""
                + defaultHistoryVersions
                + "\""
                + selected
                + ">"
                + defaultHistoryVersions
                + "</option>\n");
        }

        // Add the unlimited value
        if (defaultHistoryVersions == -1) {
            hasSelected = true;
            selected = " selected=\"selected\" ";
        }
        // Add the option for unlimited version history
        retValue.append("\t<option value=\""
            + (-1)
            + "\""
            + selected
            + ">"
            + Messages.get().getBundle(getLocale()).key(Messages.GUI_INPUT_HISTORY_SELECT_VERSIONS_0)
            + "</option>\n");
        retValue.append("</select>\n"); // End the selection

        return retValue.toString();
    }

    /**
     * Performs the change of the history settings.<p>
     * 
     * @param request the HttpServletRequest
     * 
     * @return true if everything was ok
     * 
     * @throws CmsIllegalArgumentException if the entered number is no positive integer
     */
    private boolean performEditOperation(HttpServletRequest request) throws CmsIllegalArgumentException {

        // get the new settings from the request parameters
        String paramEnabled = request.getParameter("enable");
        String paramVersions = request.getParameter("versions");
        String paramRestoreDeleted = request.getParameter("restoreDeletedHidden");
        String paramVersionsDeleted = request.getParameter("versionsDeletedHidden");

        // check the submitted values
        boolean enabled = Boolean.valueOf(paramEnabled).booleanValue();
        int versions = 0;
        try {
            versions = Integer.parseInt(paramVersions);
        } catch (NumberFormatException e) {
            // no int value submitted, throw exception
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NO_INT_ENTERED_0), e);
        }
        if (versions < -1) {
            // version value too low, throw exception
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NO_POSITIVE_INT_0));
        }

        int versionsDeleted = 0;
        boolean restoreDeleted = Boolean.valueOf(paramRestoreDeleted).booleanValue();
        // Check if the checkbox to restore deleted resources is enabled
        if (restoreDeleted) {
            versionsDeleted = 1;
        }

        boolean keepDeletedHistory = false;
        // Check if the checkbox for keeping deleted resources in the history is enabled 
        keepDeletedHistory = Boolean.valueOf(paramVersionsDeleted).booleanValue();
        if (keepDeletedHistory && restoreDeleted) {
            versionsDeleted = versions;
        }

        OpenCms.getSystemInfo().setVersionHistorySettings(enabled, versions, versionsDeleted);
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        return true;
    }

}
