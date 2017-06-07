/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.accounts;

import org.opencms.db.CmsUserExportSettings;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates a CSV file for a given list.<p>
 *
 * @since 6.0.0
 */
public class CmsUsersCsvDownloadDialog extends A_CmsUserDataImexportDialog {

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUsersCsvDownloadDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUsersCsvDownloadDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // empty
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
     */
    @Override
    public String dialogButtonsCustom() {

        return dialogButtons(new int[] {BUTTON_CLOSE}, new String[1]);
    }

    /**
     * Generates the CSV file for the given users.<p>
     *
     * @return CSV file
     */
    public String generateCsv() {

        Map<String, List<String>> objects = getData();

        // get the data object from session
        List<String> groups = objects.get("groups");
        List<String> roles = objects.get("roles");

        Map<CmsUUID, CmsUser> exportUsers = new HashMap<CmsUUID, CmsUser>();
        try {
            if (((groups == null) || (groups.size() < 1)) && ((roles == null) || (roles.size() < 1))) {
                exportUsers = getExportAllUsers(exportUsers);
            } else {
                exportUsers = getExportUsersFromGroups(groups, exportUsers);
                exportUsers = getExportUsersFromRoles(roles, exportUsers);
            }
        } catch (CmsException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_GET_EXPORT_USERS_0), e);
        }

        StringBuffer buffer = new StringBuffer();
        CmsUserExportSettings settings = OpenCms.getImportExportManager().getUserExportSettings();

        String separator = CmsStringUtil.substitute(settings.getSeparator(), "\\t", "\t");
        List<String> values = settings.getColumns();

        buffer.append("name");
        Iterator<String> itValues = values.iterator();
        while (itValues.hasNext()) {
            buffer.append(separator);
            buffer.append(itValues.next());
        }
        buffer.append("\n");

        Object[] users = exportUsers.values().toArray();

        for (int i = 0; i < users.length; i++) {
            CmsUser exportUser = (CmsUser)users[i];
            if (!exportUser.getOuFqn().equals(getParamOufqn())) {
                // skip users of others ous
                continue;
            }
            if (!isExportable(exportUser)) {
                continue;
            }
            buffer.append(exportUser.getSimpleName());
            itValues = values.iterator();
            while (itValues.hasNext()) {
                buffer.append(separator);
                String curValue = itValues.next();
                try {
                    Method method = CmsUser.class.getMethod(
                        "get" + curValue.substring(0, 1).toUpperCase() + curValue.substring(1));
                    String curOutput = (String)method.invoke(exportUser);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) || curOutput.equals("null")) {
                        curOutput = (String)exportUser.getAdditionalInfo(curValue);
                    }

                    if (curValue.equals("password")) {
                        curOutput = OpenCms.getPasswordHandler().getDigestType() + "_" + curOutput;
                    }

                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) && !curOutput.equals("null")) {
                        buffer.append(curOutput);
                    }
                } catch (NoSuchMethodException e) {
                    Object obj = exportUser.getAdditionalInfo(curValue);
                    if (obj != null) {
                        String curOutput = String.valueOf(obj);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(curOutput)) {
                            buffer.append(curOutput);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ILLEGAL_ACCESS_0), e);
                } catch (InvocationTargetException e) {
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_INVOCATION_TARGET_0), e);
                }
            }
            buffer.append("\n");
        }
        HttpServletResponse res = CmsFlexController.getController(getJsp().getRequest()).getTopResponse();
        res.setContentType("text/comma-separated-values");
        String filename = "export_users" + new Random().nextInt(1024) + ".csv";
        res.setHeader(
            "Content-Disposition",
            new StringBuffer("attachment; filename=\"").append(filename).append("\"").toString());
        return buffer.toString();
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append("<script type=\"text/javascript\">\n");
            result.append("function download(){\n");
            result.append("\twindow.open(\"").append(
                getJsp().link(
                    CmsRequestUtil.appendParameter(
                        getDownloadPath(),
                        A_CmsOrgUnitDialog.PARAM_OUFQN,
                        getParamOufqn()))).append("\", \"usecvs\");\n");
            result.append("}\n");
            result.append("window.setTimeout(\"download()\",500);\n");
            result.append("</script>\n");
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_HINT_BLOCK_0)));
            result.append(key(Messages.GUI_USERDATA_DOWNLOAD_LABEL_HINT_TEXT_0));
            result.append(" <a href='javascript:download()'>");
            result.append(key(Messages.GUI_USERDATA_DOWNLOAD_LABEL_HINT_CLICK_0));
            result.append("</a>.");
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // empty
    }

    /**
     * Returns the export options data.<p>
     *
     * @return the export options data
     */
    protected Map<String, List<String>> getData() {

        return (Map<String, List<String>>)((Map<String, Object>)getSettings().getDialogObject()).get(
            CmsUserDataExportDialog.class.getName());
    }

    /**
     * Returns the download path.<p>
     *
     * @return the download path
     */
    protected String getDownloadPath() {

        return "/system/workplace/admin/accounts/imexport_user_data/csvdownload.jsp";
    }

    /**
     * Returns a map with the users to export added.<p>
     *
     * @param exportUsers the map to add the users
     *
     * @return a map with the users to export added
     *
     * @throws CmsException if getting users failed
     */
    protected Map<CmsUUID, CmsUser> getExportAllUsers(Map<CmsUUID, CmsUser> exportUsers) throws CmsException {

        List<CmsUser> users = OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false);
        if ((users != null) && (users.size() > 0)) {
            Iterator<CmsUser> itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = itUsers.next();
                if (!exportUsers.containsKey(user.getId())) {
                    exportUsers.put(user.getId(), user);
                }
            }
        }
        return exportUsers;
    }

    /**
     * Returns a map with the users to export added.<p>
     *
     * @param groups the selected groups
     * @param exportUsers the map to add the users
     *
     * @return a map with the users to export added
     *
     * @throws CmsException if getting groups or users of group failed
     */
    protected Map<CmsUUID, CmsUser> getExportUsersFromGroups(List<String> groups, Map<CmsUUID, CmsUser> exportUsers)
    throws CmsException {

        if ((groups != null) && (groups.size() > 0)) {
            Iterator<String> itGroups = groups.iterator();
            while (itGroups.hasNext()) {
                List<CmsUser> groupUsers = getCms().getUsersOfGroup(itGroups.next());
                Iterator<CmsUser> itGroupUsers = groupUsers.iterator();
                while (itGroupUsers.hasNext()) {
                    CmsUser groupUser = itGroupUsers.next();
                    if (!exportUsers.containsKey(groupUser.getId())) {
                        exportUsers.put(groupUser.getId(), groupUser);
                    }
                }
            }
        }
        return exportUsers;
    }

    /**
     * Returns a map with the users to export added.<p>
     *
     * @param roles the selected roles
     * @param exportUsers the map to add the users
     *
     * @return a map with the users to export added
     *
     * @throws CmsException if getting roles or users of role failed
     */
    protected Map<CmsUUID, CmsUser> getExportUsersFromRoles(List<String> roles, Map<CmsUUID, CmsUser> exportUsers)
    throws CmsException {

        if ((roles != null) && (roles.size() > 0)) {
            Iterator<String> itRoles = roles.iterator();
            while (itRoles.hasNext()) {
                List<CmsUser> roleUsers = OpenCms.getRoleManager().getUsersOfRole(
                    getCms(),
                    CmsRole.valueOfGroupName(itRoles.next()).forOrgUnit(getParamOufqn()),
                    true,
                    false);
                Iterator<CmsUser> itRoleUsers = roleUsers.iterator();
                while (itRoleUsers.hasNext()) {
                    CmsUser roleUser = itRoleUsers.next();
                    // contains
                    if (exportUsers.get(roleUser.getId()) == null) {
                        exportUsers.put(roleUser.getId(), roleUser);
                    }
                }
            }
        }
        return exportUsers;
    }

    /**
     * Checks if the user can be exported.<p>
     *
     * @param exportUser the suer to check
     *
     * @return <code>true</code> if the user can be exported
     */
    protected boolean isExportable(CmsUser exportUser) {

        return exportUser.getFlags() < I_CmsPrincipal.FLAG_CORE_LIMIT;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if (getParamOufqn() == null) {
            setParamOufqn("");
        }
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
    }
}
