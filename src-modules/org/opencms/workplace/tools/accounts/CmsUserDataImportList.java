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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXsltUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Main system user account management view.<p>
 *
 * @since 6.5.6
 */
public class CmsUserDataImportList extends A_CmsUsersList {

    /** Value for the delete action. */
    public static final int ACTION_IMPORT = 121;

    /** Request parameter value for the import action. */
    public static final String IMPORT_ACTION = "import";

    /** list action id constant. */
    public static final String LIST_ACTION_VALIDATION = "av";

    /** list column id constant. */
    public static final String LIST_COLUMN_VALIDATION = "cv";

    /** list column id constant. */
    public static final String LIST_COLUMN_VALIDATION_HIDDEN = "cvh";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_REASON = "dre";

    /** list id constant. */
    public static final String LIST_ID = "lsudi";

    /** list action id constant. */
    public static final String LIST_MACTION_SELECT = "ms";

    /** Stores the value of the request parameter for the group list. */
    private String m_paramGroups;

    /** Stores the value of the request parameter for the import file. */
    private String m_paramImportfile;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the default password. */
    private String m_paramPassword;

    /** Stores the value of the request parameter for the role list. */
    private String m_paramRoles;

    /** Stores the reasons why users may not be imported. */
    private Map m_reasons;

    /** The file to upload. */
    private File m_uploadFile;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUserDataImportList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_NAME_0), false);
        getList().setSortedColumn(LIST_COLUMN_VALIDATION_HIDDEN);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserDataImportList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        switch (getAction()) {
            case ACTION_IMPORT:
                List users = getUsers();
                Iterator itUsers = users.iterator();
                while (itUsers.hasNext()) {
                    CmsUser user = (CmsUser)itUsers.next();
                    if (((m_reasons == null) || !m_reasons.containsKey(user.getName()))
                        && !isAlreadyAvailable(user.getName())) {

                        String password = user.getPassword();
                        if (password.indexOf("_") == -1) {
                            try {
                                password = OpenCms.getPasswordHandler().digest(password);
                            } catch (CmsPasswordEncryptionException e) {
                                throw new CmsRuntimeException(
                                    Messages.get().container(Messages.ERR_DIGEST_PASSWORD_0),
                                    e);
                            }
                        } else {
                            password = password.substring(password.indexOf("_") + 1);
                        }
                        CmsUser createdUser;
                        try {
                            createdUser = getCms().importUser(
                                new CmsUUID().toString(),
                                getParamOufqn() + user.getName(),
                                password,
                                user.getFirstname(),
                                user.getLastname(),
                                user.getEmail(),
                                user.getFlags(),
                                System.currentTimeMillis(),
                                user.getAdditionalInfo());
                        } catch (CmsException e) {
                            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_IMPORT_USER_0), e);
                        }

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamGroups())) {
                            List groups = CmsStringUtil.splitAsList(getParamGroups(), ",");
                            Iterator itGroups = groups.iterator();
                            while (itGroups.hasNext()) {
                                try {
                                    getCms().addUserToGroup(createdUser.getName(), (String)itGroups.next());
                                } catch (CmsException e) {
                                    throw new CmsRuntimeException(
                                        Messages.get().container(Messages.ERR_ADD_USER_TO_GROUP_0),
                                        e);
                                }
                            }
                        }

                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamRoles())) {
                            List roles = CmsStringUtil.splitAsList(getParamRoles(), ",");
                            Iterator itRoles = roles.iterator();
                            while (itRoles.hasNext()) {
                                try {
                                    OpenCms.getRoleManager().addUserToRole(
                                        getCms(),
                                        CmsRole.valueOfGroupName((String)itRoles.next()),
                                        createdUser.getName());
                                } catch (CmsException e) {
                                    throw new CmsRuntimeException(
                                        Messages.get().container(Messages.ERR_ADD_USER_TO_ROLE_0),
                                        e);
                                }
                            }
                        }
                    }
                }
                setAction(ACTION_CANCEL);
                actionCloseDialog();
                break;
            default:
                super.actionDialog();
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_SELECT)) {
            Iterator itItems = getSelectedItems().iterator();
            while (itItems.hasNext()) {
                CmsListItem listItem = (CmsListItem)itItems.next();
                String userName = listItem.get(LIST_COLUMN_DISPLAY).toString();
                List users = getUsers();
                Iterator itUsers = users.iterator();
                while (itUsers.hasNext()) {
                    CmsUser user = (CmsUser)itUsers.next();
                    try {
                        if (user.getName().equals(userName)
                            && ((m_reasons == null) || !m_reasons.containsKey(userName))
                            && !isAlreadyAvailable(user.getName())) {

                            String password = user.getPassword();
                            if (password.indexOf("_") == -1) {
                                password = OpenCms.getPasswordHandler().digest(password);
                            } else {
                                password = password.substring(password.indexOf("_") + 1);
                            }

                            CmsUser createdUser = getCms().importUser(
                                new CmsUUID().toString(),
                                getParamOufqn() + user.getName(),
                                password,
                                user.getFirstname(),
                                user.getLastname(),
                                user.getEmail(),
                                user.getFlags(),
                                System.currentTimeMillis(),
                                user.getAdditionalInfo());

                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamGroups())) {
                                List groups = CmsStringUtil.splitAsList(getParamGroups(), ",");
                                Iterator itGroups = groups.iterator();
                                while (itGroups.hasNext()) {
                                    getCms().addUserToGroup(createdUser.getName(), (String)itGroups.next());
                                }
                            }

                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamRoles())) {
                                List roles = CmsStringUtil.splitAsList(getParamRoles(), ",");
                                Iterator itRoles = roles.iterator();
                                while (itRoles.hasNext()) {
                                    OpenCms.getRoleManager().addUserToRole(
                                        getCms(),
                                        CmsRole.valueOfGroupName((String)itRoles.next()),
                                        createdUser.getName());
                                }
                            }

                            break;
                        }
                    } catch (CmsException e) {
                        // noop
                    }
                }
            }
            Map params = new HashMap();
            params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, getParamOufqn());
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

            try {
                getToolManager().jspForwardTool(this, getParentPath(), params);
            } catch (ServletException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_FORWARDING_TO_PARENT_TOOL_0), e);
            } catch (IOException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_FORWARDING_TO_PARENT_TOOL_0), e);
            }

        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * Returns the paramGroups.<p>
     *
     * @return the paramGroups
     */
    public String getParamGroups() {

        return m_paramGroups;
    }

    /**
     * Returns the paramImportfile.<p>
     *
     * @return the paramImportfile
     */
    public String getParamImportfile() {

        return m_paramImportfile;
    }

    /**
     * Returns the organizational unit fqn parameter value.<p>
     *
     * @return the organizational unit fqn parameter value
     */
    @Override
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the paramPassword.<p>
     *
     * @return the paramPassword
     */
    public String getParamPassword() {

        return m_paramPassword;
    }

    /**
     * Returns the paramRoles.<p>
     *
     * @return the paramRoles
     */
    public String getParamRoles() {

        return m_paramRoles;
    }

    /**
     * Returns the reasons.<p>
     *
     * @return the reasons
     */
    public Map getReasons() {

        return m_reasons;
    }

    /**
     * Sets the paramGroups.<p>
     *
     * @param paramGroups the paramGroups to set
     */
    public void setParamGroups(String paramGroups) {

        m_paramGroups = paramGroups;
    }

    /**
     * Sets the paramImportfile.<p>
     *
     * @param paramImportfile the paramImportfile to set
     */
    public void setParamImportfile(String paramImportfile) {

        m_paramImportfile = paramImportfile;
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     *
     * @param ouFqn the organizational unit fqn parameter value
     */
    @Override
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the paramPassword.<p>
     *
     * @param paramPassword the paramPassword to set
     */
    public void setParamPassword(String paramPassword) {

        m_paramPassword = paramPassword;
    }

    /**
     * Sets the paramRoles.<p>
     *
     * @param paramRoles the paramRoles to set
     */
    public void setParamRoles(String paramRoles) {

        m_paramRoles = paramRoles;
    }

    /**
     * Sets the reasons.<p>
     *
     * @param reasons the reasons to set
     */
    public void setReasons(Map reasons) {

        m_reasons = reasons;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlEnd()
     */
    @Override
    protected String customHtmlEnd() {

        StringBuffer result = new StringBuffer(512);
        result.append(super.customHtmlEnd());
        result.append("<form name='actions' method='post' action='");
        result.append(getDialogRealUri());
        result.append("' class='nomargin' onsubmit=\"return submitAction('ok', null, 'actions');\">\n");
        result.append(allParamsAsHidden());
        result.append(dialogButtonRow(HTML_START));
        result.append("<input name='");
        result.append(IMPORT_ACTION);
        result.append("' type='button' value='");
        result.append(key(org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_OK_0));
        result.append("' onclick=\"submitAction('");
        result.append(IMPORT_ACTION);
        result.append("', form);\" class='dialogbutton'>\n");
        dialogButtonsHtml(result, BUTTON_CANCEL, "");
        result.append(dialogButtonRow(HTML_END));
        result.append("</form>\n");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(1024);
        result.append(dialogBlockStart(key(Messages.GUI_USERDATA_IMPORT_LABEL_HINT_BLOCK_0)));
        result.append(key(Messages.GUI_IMPORTLISTCSV_IMPORT_LABEL_HINT_TEXT_0));
        result.append(dialogBlockEnd());
        result.append("<div class=\"dialogspacer\" unselectable=\"on\">&nbsp;</div>");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List users = getList().getAllContent();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsListItem item = (CmsListItem)itUsers.next();
            String userName = item.get(LIST_COLUMN_DISPLAY).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_REASON) && (m_reasons != null) && m_reasons.containsKey(userName)) {
                    html.append(m_reasons.get(userName));
                } else {
                    html.append(key(Messages.GUI_IMPORTLISTCSV_VALID_USER_0));
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getGroupIcon()
     */
    @Override
    protected String getGroupIcon() {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getListItems()
     */
    @Override
    protected List getListItems() {

        List ret = new ArrayList();

        // get content
        List users = getUsers();
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            CmsListItem item = getList().newItem(user.getName());
            item.set(LIST_COLUMN_DISPLAY, user.getName());

            if (isAlreadyAvailable(user.getName())) {
                if (m_reasons == null) {
                    m_reasons = new HashMap();
                }
                m_reasons.put(
                    user.getName(),
                    Messages.get().container(Messages.GUI_IMPORTLISTCSV_ALREADY_EXISTS_0).key(getLocale()));
            }
            if ((m_reasons != null) && m_reasons.containsKey(user.getName())) {
                item.set(LIST_COLUMN_VALIDATION_HIDDEN, "invalid");
            } else {
                item.set(LIST_COLUMN_VALIDATION_HIDDEN, "valid");
            }
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#getUsers()
     */
    @Override
    protected List getUsers() {

        String separator = null;
        List values = null;

        m_uploadFile = new File(m_paramImportfile);
        FileReader fileReader;
        BufferedReader bufferedReader;
        List users = null;

        try {
            fileReader = new FileReader(m_uploadFile);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            boolean headline = true;

            while ((line = bufferedReader.readLine()) != null) {
                if (users == null) {
                    users = new ArrayList();
                }
                if (separator == null) {
                    separator = CmsXsltUtil.getPreferredDelimiter(line);
                }
                List lineValues = CmsStringUtil.splitAsList(line, separator);
                if (headline) {
                    values = new ArrayList();
                    Iterator itLineValues = lineValues.iterator();
                    while (itLineValues.hasNext()) {
                        values.add(itLineValues.next());
                    }
                    headline = false;
                } else if (values != null) {
                    CmsUser curUser = new CmsUser();
                    try {
                        for (int i = 0; i < values.size(); i++) {
                            String curValue = (String)values.get(i);
                            try {
                                Method method = CmsUser.class.getMethod(
                                    "set" + curValue.substring(0, 1).toUpperCase() + curValue.substring(1),
                                    new Class[] {String.class});
                                String value = "";
                                if ((lineValues.size() > i) && (lineValues.get(i) != null)) {
                                    value = (String)lineValues.get(i);

                                }
                                if (curValue.equals("password")) {
                                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                                        value = m_paramPassword;
                                    }
                                }
                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value) && !value.equals("null")) {
                                    method.invoke(curUser, new Object[] {value});
                                }
                            } catch (NoSuchMethodException ne) {
                                curUser.setAdditionalInfo(curValue, lineValues.get(i));
                            } catch (IllegalAccessException le) {
                                if (m_reasons == null) {
                                    m_reasons = new HashMap();
                                }
                                m_reasons.put(curUser.getName(), le);
                            } catch (InvocationTargetException te) {
                                if (m_reasons == null) {
                                    m_reasons = new HashMap();
                                }
                                m_reasons.put(curUser.getName(), te);
                            }
                        }
                    } catch (CmsRuntimeException e) {
                        if (m_reasons == null) {
                            m_reasons = new HashMap();
                        }
                        if (curUser.getName() == null) {
                            m_reasons.put(lineValues.get(0), e);
                        } else {
                            m_reasons.put(curUser.getName(), e);
                        }
                    }
                    users.add(curUser);
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            //noop
        }

        // m_reasons

        return users;
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initExportObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new list
                m_reasons = new HashMap();
            } else {
                Map objects = (Map)getSettings().getDialogObject();
                if (objects == null) {
                    // using hashtable as most efficient version of a synchronized map
                    objects = new Hashtable();
                    getSettings().setDialogObject(objects);
                }
                m_reasons = (Map)objects.get(getClass().getName());
            }
        } catch (Exception e) {
            // create a new list
            m_reasons = new HashMap();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (IMPORT_ACTION.equals(getParamAction())) {
            setAction(ACTION_IMPORT);
        }

        // save the current state of the message (may be changed because of the widget values)

        if (m_reasons == null) {
            // null object: remove the entry from the map

            Map objects = (Map)getSettings().getDialogObject();
            if (objects == null) {
                // using hashtable as most efficient version of a synchronized map
                objects = new Hashtable();
                getSettings().setDialogObject(objects);
            }
            objects.remove(getClass().getName());
        } else {
            Map objects = (Map)getSettings().getDialogObject();
            if (objects == null) {
                // using hashtable as most efficient version of a synchronized map
                objects = new Hashtable();
                getSettings().setDialogObject(objects);
            }
            objects.put(getClass().getName(), m_reasons);
        }
    }

    /**
     * Checks if the given user name is already available inside the current ou.<p>
     *
     * @param userName the user name to check
     * @return <code>true</code> if the user name is already available, otherwise return <code>false</code>
     */
    protected boolean isAlreadyAvailable(String userName) {

        List availableUsers;
        try {
            availableUsers = OpenCms.getOrgUnitManager().getUsers(getCms(), getParamOufqn(), false);
        } catch (CmsException e) {
            availableUsers = new ArrayList();
        }
        Iterator itAvailableUsers = availableUsers.iterator();
        while (itAvailableUsers.hasNext()) {
            if (userName.equals(((CmsUser)itAvailableUsers.next()).getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#readUser(java.lang.String)
     */
    @Override
    protected CmsUser readUser(String name) {

        return null;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        initExportObject();

        super.setColumns(metadata);

        metadata.getColumnDefinition(LIST_COLUMN_GROUPS).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ROLE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_ACTIVATE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_DELETE).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_NAME).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_EMAIL).setVisible(false);
        metadata.getColumnDefinition(LIST_COLUMN_LASTLOGIN).setVisible(false);

        metadata.getColumnDefinition(LIST_COLUMN_DISPLAY).getDefaultAction(LIST_DEFACTION_EDIT).setEnabled(false);
        metadata.getColumnDefinition(LIST_COLUMN_DISPLAY).setWidth("100%");

        // create hidden column for state
        CmsListColumnDefinition hiddenStateCol = new CmsListColumnDefinition(LIST_COLUMN_VALIDATION_HIDDEN);
        hiddenStateCol.setName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_COLS_VAIDATION_0));
        hiddenStateCol.setVisible(false);
        hiddenStateCol.setSorteable(true);
        metadata.addColumn(hiddenStateCol);

        // create column for state
        CmsListColumnDefinition stateCol = new CmsListColumnDefinition(LIST_COLUMN_VALIDATION);
        stateCol.setName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_COLS_VAIDATION_0));
        stateCol.setWidth("20");
        stateCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        stateCol.setSorteable(false);
        // add action for icon displaying
        CmsListDirectAction stateAction = new CmsListDirectAction(LIST_ACTION_VALIDATION) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                String userName = getItem().getId();

                if (((((CmsUserDataImportList)getWp()).getReasons() != null)
                    && ((CmsUserDataImportList)getWp()).getReasons().containsKey(userName))
                    || ((CmsUserDataImportList)getWp()).isAlreadyAvailable(userName)) {
                    return ICON_MULTI_DELETE;
                }
                return ICON_MULTI_ACTIVATE;
            }
        };
        stateAction.setName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_COLS_VAIDATION_0));
        stateAction.setIconPath(ICON_MULTI_ACTIVATE);
        stateAction.setEnabled(false);
        stateCol.addDirectAction(stateAction);
        metadata.addColumn(stateCol, 1);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setDeleteAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setDeleteAction(CmsListColumnDefinition deleteCol) {

        // noop
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setEditAction(org.opencms.workplace.list.CmsListColumnDefinition)
     */
    @Override
    protected void setEditAction(CmsListColumnDefinition editCol) {

        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_USERS_LIST_ACTION_EDIT_NAME_0));
        editAction.setIconPath(PATH_BUTTONS + "user.png");
        editAction.setEnabled(false);
        editCol.addDirectAction(editAction);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add reason details
        CmsListItemDetails reasonDetails = new CmsListItemDetails(LIST_DETAIL_REASON);
        reasonDetails.setAtColumn(LIST_COLUMN_DISPLAY);
        reasonDetails.setVisible(true);
        reasonDetails.setShowActionName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_DETAIL_SHOW_REASON_NAME_0));
        reasonDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_IMPORTLISTCSV_DETAIL_SHOW_REASON_HELP_0));
        reasonDetails.setHideActionName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_DETAIL_HIDE_REASON_NAME_0));
        reasonDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_IMPORTLISTCSV_DETAIL_HIDE_REASON_HELP_0));
        reasonDetails.setName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_DETAIL_REASON_NAME_0));
        reasonDetails.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                StringBuffer html = new StringBuffer(512);
                html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
                html.append("\t<tr>\n");
                html.append("\t\t<td style='white-space:normal;' >\n");
                html.append("\t\t\t");
                html.append(data == null ? "" : data);
                html.append("\n");
                html.append("\t\t</td>\n");
                html.append("\t</tr>\n");
                html.append("</table>\n");
                return html.toString();
            }
        });
        metadata.addItemDetails(reasonDetails);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUsersList#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the select multi action
        CmsListMultiAction selectUser = new CmsListMultiAction(LIST_MACTION_SELECT);
        selectUser.setName(Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_MACTION_SELECT_NAME_0));
        selectUser.setHelpText(Messages.get().container(Messages.GUI_IMPORTLISTCSV_LIST_MACTION_SELECT_HELP_0));
        selectUser.setIconPath(ICON_MULTI_ADD);
        metadata.addMultiAction(selectUser);
    }
}
