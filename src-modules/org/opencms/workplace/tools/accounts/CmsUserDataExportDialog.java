/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsUserDataExportDialog.java,v $
 * Date   : $Date: 2008/02/27 12:05:26 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.accounts;

import org.opencms.db.CmsUserExportSettings;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to export user data.<p>
 * 
 * @author Raphael Schnuck 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.7.1
 */
public class CmsUserDataExportDialog extends A_CmsUserDataImexportDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "userdata.export";

    /** Stores the value of the request parameter for the export file. */
    private String m_paramExportfile;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsUserDataExportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserDataExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#actionCommit()
     */
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        // key CmsUser uuid, value CmsUser object
        Map exportUsers = new HashMap();
        try {
            if (((getGroups() == null) || (getGroups().size() < 1))
                && ((getRoles() == null) || (getRoles().size() < 1))) {
                exportUsers = getExportAllUsers(exportUsers);
            } else {
                exportUsers = getExportUsersFromGroups(exportUsers);
                exportUsers = getExportUsersFromRoles(exportUsers);
            }
        } catch (CmsException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_GET_EXPORT_USERS_0), e);
        }

        BufferedWriter bufferedWriter;
        File downloadFile;
        try {
            downloadFile = File.createTempFile("export_users", ".csv");
            FileWriter fileWriter = new FileWriter(downloadFile);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            throw e;
        }

        CmsUserExportSettings settings = OpenCms.getImportExportManager().getUserExportSettings();

        String separator = CmsStringUtil.substitute(settings.getSeparator(), "\\t", "\t");
        List values = settings.getColumns();

        String headline = "";
        headline += "name";
        Iterator itValues = values.iterator();
        while (itValues.hasNext()) {
            headline += separator;
            headline += (String)itValues.next();
        }
        headline += "\n";
        try {
            bufferedWriter.write(headline);
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_WRITE_TO_EXPORT_FILE_0), e);
        }

        Object[] users = exportUsers.values().toArray();

        for (int i = 0; i < users.length; i++) {
            CmsUser exportUser = (CmsUser)users[i];
            if (exportUser.getOuFqn().equals(getParamOufqn())) {
                String output = "";
                output += exportUser.getSimpleName();
                itValues = values.iterator();
                while (itValues.hasNext()) {
                    output += separator;
                    String curValue = (String)itValues.next();
                    try {
                        Method method = CmsUser.class.getMethod("get"
                            + curValue.substring(0, 1).toUpperCase()
                            + curValue.substring(1), null);
                        String curOutput = (String)method.invoke(exportUser, null);
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) || curOutput.equals("null")) {
                            curOutput = (String)exportUser.getAdditionalInfo(curValue);
                        }

                        if (curValue.equals("password")) {
                            curOutput = OpenCms.getPasswordHandler().getDigestType() + "_" + curOutput;
                        }

                        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(curOutput) && !curOutput.equals("null")) {
                            output += curOutput;
                        }
                    } catch (NoSuchMethodException e) {
                        String curOutput = (String)exportUser.getAdditionalInfo(curValue);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(curOutput)) {
                            output += curOutput;
                        }
                    } catch (IllegalAccessException e) {
                        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ILLEGAL_ACCESS_0), e);
                    } catch (InvocationTargetException e) {
                        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_INVOCATION_TARGET_0), e);
                    }
                }
                output += "\n";
                try {
                    bufferedWriter.write(output);
                } catch (IOException e) {
                    throw new CmsRuntimeException(Messages.get().container(Messages.ERR_WRITE_TO_EXPORT_FILE_0), e);
                }
            }
        }

        try {
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_WRITE_TO_EXPORT_FILE_0), e);
        }

        Map params = new HashMap();
        params.put("exportfile", downloadFile.getAbsolutePath().replace('\\', '/'));
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getParamOufqn());
        params.put(CmsDialog.PARAM_CLOSELINK, getParamCloseLink());
        getToolManager().jspForwardTool(this, getCurrentToolPath(), params);
        setCommitErrors(errors);
    }

    /**
     * Returns a map with the users to export added.<p>
     * 
     * @param exportUsers the map to add the users
     * @return a map with the users to export added
     * @throws CmsException if getting users failed
     */
    public Map getExportAllUsers(Map exportUsers) throws CmsException {

        List users = OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false);
        if ((users != null) && (users.size() > 0)) {
            Iterator itUsers = users.iterator();
            while (itUsers.hasNext()) {
                CmsUser user = (CmsUser)itUsers.next();
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
     * @param exportUsers the map to add the users
     * @return a map with the users to export added
     * @throws CmsException if getting groups or users of group failed
     */
    public Map getExportUsersFromGroups(Map exportUsers) throws CmsException {

        List groups = getGroups();
        if ((groups != null) && (groups.size() > 0)) {
            Iterator itGroups = groups.iterator();
            while (itGroups.hasNext()) {
                List groupUsers = getCms().getUsersOfGroup((String)itGroups.next());
                Iterator itGroupUsers = groupUsers.iterator();
                while (itGroupUsers.hasNext()) {
                    CmsUser groupUser = (CmsUser)itGroupUsers.next();
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
     * @param exportUsers the map to add the users
     * @return a map with the users to export added
     * @throws CmsException if getting roles or users of role failed
     */
    public Map getExportUsersFromRoles(Map exportUsers) throws CmsException {

        List roles = getRoles();
        if ((roles != null) && (roles.size() > 0)) {
            Iterator itRoles = roles.iterator();
            while (itRoles.hasNext()) {
                List roleUsers = OpenCms.getRoleManager().getUsersOfRole(
                    getCms(),
                    CmsRole.valueOfGroupName((String)itRoles.next()),
                    true,
                    false);
                Iterator itRoleUsers = roleUsers.iterator();
                while (itRoleUsers.hasNext()) {
                    CmsUser roleUser = (CmsUser)itRoleUsers.next();
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
     * Returns the JavaScript code to execute during the load of the dialog.<p>
     * 
     * @return the JavaScript code to execute
     */
    public String getOnloadJavaScript() {

        StringBuffer result = new StringBuffer(256);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_paramExportfile)) {
            result.append("javascript:window.open(\"");
            result.append(getJsp().link(getDownloadPath()));
            result.append("?servletUrl=");
            result.append(getJsp().link("/system/workplace/admin/workplace/logfileview/downloadTrigger.jsp"));
            result.append("&filePath=").append(getParamExportfile());
            result.append("\", \"download\", \"width=300,height=130,left=100,top=100,menubar=no,status=no,toolbar=no\");");
        }
        return result.toString();
    }

    /**
     * Returns the export file.<p>
     *
     * @return the export file
     */
    public String getParamExportfile() {

        return m_paramExportfile;
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#pageBody(int, java.lang.String, java.lang.String)
     */
    public String pageBody(int segment, String className, String parameters) {

        if (parameters != null) {
            if (parameters.lastIndexOf("onload") != -1) {
                // get substring until onload='
                String subParameters = parameters.substring(0, parameters.lastIndexOf("onload") + 8);
                subParameters += getOnloadJavaScript();
                // get the rest of the old parameter string
                subParameters += parameters.substring(parameters.lastIndexOf("onload") + 8);
                parameters = subParameters;
            }
        }
        return super.pageBody(segment, className, parameters);
    }

    /**
     * Sets the export file.<p>
     *
     * @param paramExportfile the export file to set
     */
    public void setParamExportfile(String paramExportfile) {

        m_paramExportfile = paramExportfile;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_HINT_BLOCK_0)));
            result.append(key(Messages.GUI_USERDATA_EXPORT_LABEL_HINT_TEXT_0));
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_GROUPS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_ROLES_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(1, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#defineWidgets()
     */
    protected void defineWidgets() {

        initExportObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(
            this,
            "groups",
            PAGES[0],
            new CmsGroupWidget(null, null, getParamOufqn())));
        addWidget(new CmsWidgetDialogParameter(this, "roles", PAGES[0], new CmsSelectWidget(getSelectRoles())));

    }

    /**
     * Returns the path to the download jsp.<p>
     * 
     * @return the path to the download jsp
     */
    protected String getDownloadPath() {

        return "/system/workplace/admin/accounts/imexport_user_data/dodownload.jsp";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initExportObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new list
                setGroups(new ArrayList());
                setRoles(new ArrayList());
            } else {
                // this is not the initial call, get the message info object from session
                setGroups((List)((Map)getDialogObject()).get("groups"));
                setRoles((List)((Map)getDialogObject()).get("roles"));
            }
        } catch (Exception e) {
            // create a new list
            setGroups(new ArrayList());
            setRoles(new ArrayList());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        HashMap objectsMap = new HashMap();
        objectsMap.put("groups", getGroups());
        objectsMap.put("roles", getRoles());

        // save the current state of the message (may be changed because of the widget values)
        setDialogObject(objectsMap);
    }
}
