/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsEditGroupDialog.java,v $
 * Date   : $Date: 2011/03/23 14:51:08 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Skeleton dialog to create a new group or edit an existing group in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsEditGroupDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "group";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the group id. */
    public static final String PARAM_GROUPID = "groupid";

    /** Request parameter name for the group name. */
    public static final String PARAM_GROUPNAME = "groupname";

    /** The user object that is edited on this dialog. */
    protected CmsGroup m_group;

    /** Stores the value of the request parameter for the group id. */
    private String m_paramGroupid;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** Auxiliary Property for better representation of the bean parentId property. */
    private String m_parentGroup;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsEditGroupDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Commits the edited group to the db.<p>
     */
    @Override
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // if new create it first
            if (m_group.getId() == null) {
                CmsGroup newGroup = getCms().createGroup(
                    m_paramOufqn + m_group.getSimpleName(),
                    m_group.getDescription(),
                    m_group.isEnabled() ? I_CmsPrincipal.FLAG_ENABLED : I_CmsPrincipal.FLAG_DISABLED,
                    getParentGroup());
                newGroup.setProjectManager(m_group.isProjectManager());
                newGroup.setProjectCoWorker(m_group.isProjectCoWorker());
                m_group = newGroup;
            } else {
                if (getParentGroup() != null) {
                    m_group.setParentId(getCms().readGroup(getParentGroup()).getId());
                } else {
                    m_group.setParentId(CmsUUID.getNullUUID());
                }
            }
            // write the edited group
            getCms().writeGroup(m_group);
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(getListClass());
                objects.remove(A_CmsUsersList.class.getName());
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        if (errors.isEmpty() && isNewGroup()) {
            if ((getParamCloseLink() != null) && (getParamCloseLink().indexOf("path=" + getListRootPath()) > -1)) {
                // set closelink
                Map argMap = new HashMap();
                argMap.put("groupid", m_group.getId());
                argMap.put("groupname", m_group.getName());
                argMap.put("oufqn", m_paramOufqn);
                setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), getListRootPath() + "/edit", argMap));
            }
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the description of the parent ou.<p>
     * 
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            return OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).getDescription(
                getLocale())
                + " ("
                + CmsOrganizationalUnit.SEPARATOR
                + getParamOufqn()
                + ")";
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Returns the localized description of a group.<p>
     * 
     * @return the localized description of a group
     */
    public String getDescription() {

        return m_group.getDescription(getLocale());
    }

    /**
     * Returns the simple name of the group object.<p>
     * 
     * @return the simple name of the group object
     */
    public String getName() {

        return m_group.getSimpleName();
    }

    /**
     * Returns the user id parameter value.<p>
     * 
     * @return the user id parameter value
     */
    public String getParamGroupid() {

        return m_paramGroupid;
    }

    /**
     * Returns the organizational unit parameter value.<p>
     * 
     * @return the organizational unit parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the parent Group name.<p>
     *
     * @return the parent Group name
     */
    public String getParentGroup() {

        return m_parentGroup;
    }

    /**
     * The method is just needed for displaying reasons.<p>
     * 
     * @param assignedOu nothing to do with this parameter
     */
    public void setAssignedOu(String assignedOu) {

        // nothing will be done here, just to avoid warnings
        assignedOu.length();
    }

    /**
     * Sets the description for a group.<p>
     * 
     * @param description the description for a group
     */
    public void setDescription(String description) {

        m_group.setDescription(description);
    }

    /**
     * Sets the name of the group object.<p>
     * 
     * @param name the name of the group object
     */
    public void setName(String name) {

        m_group.setName(getParamOufqn() + name);
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userId the user id parameter value
     */
    public void setParamGroupid(String userId) {

        m_paramGroupid = userId;
    }

    /**
     * Sets the organizational unit parameter value.<p>
     * 
     * @param ouFqn the organizational unit parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the parent Group name.<p>
     *
     * @param parentGroup the parent Group name to set
     */
    public void setParentGroup(String parentGroup) {

        if (CmsStringUtil.isEmpty(parentGroup) || parentGroup.equals("null") || parentGroup.equals("none")) {
            parentGroup = null;
        }
        if (parentGroup != null) {
            try {
                getCms().readGroup(parentGroup);
            } catch (CmsException e) {
                throw new CmsIllegalArgumentException(e.getMessageContainer());
            }
        }
        m_parentGroup = parentGroup;
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
            result.append(dialogBlockStart(key(Messages.GUI_GROUP_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            boolean webuserOu = false;
            try {
                webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser();
            } catch (CmsException e) {
                // ignore
            }
            if (!webuserOu) {
                result.append(dialogBlockStart(key(Messages.GUI_GROUP_EDITOR_LABEL_FLAGS_BLOCK_0)));
                result.append(createWidgetTableStart());
                result.append(createDialogRowsHtml(5, 6));
                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
            }
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initGroupObject();
        boolean webuserOu = false;
        try {
            webuserOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), getParamOufqn()).hasFlagWebuser();
        } catch (CmsException e) {
            // ignore
        }
        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if ((m_group.getId() == null) && isEditable(m_group)) {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
        }
        if (isEditable(m_group)) {
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsTextareaWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "parentGroup", PAGES[0], new CmsGroupWidget(
                null,
                null,
                getParamOufqn())));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "enabled", PAGES[0], new CmsCheckboxWidget()));
            if (!webuserOu) {
                addWidget(new CmsWidgetDialogParameter(m_group, "projectManager", PAGES[0], new CmsCheckboxWidget()));
                addWidget(new CmsWidgetDialogParameter(m_group, "projectCoWorker", PAGES[0], new CmsCheckboxWidget()));
            }
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "parentGroup", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "enabled", PAGES[0], new CmsDisplayWidget()));
            if (!webuserOu) {
                addWidget(new CmsWidgetDialogParameter(m_group, "projectManager", PAGES[0], new CmsDisplayWidget()));
                addWidget(new CmsWidgetDialogParameter(m_group, "projectCoWorker", PAGES[0], new CmsDisplayWidget()));
            }
        }
    }

    /**
     * Returns the dialog class name of the list to refresh.<p> 
     * 
     * @return the list dialog class name
     */
    protected abstract String getListClass();

    /**
     * Returns the root path for the list tool.<p>
     * 
     * @return the root path
     */
    protected abstract String getListRootPath();

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the group object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the group object on first dialog call are possible:
     * <ul>
     * <li>edit an existing group</li>
     * <li>create a new group</li>
     * <li>view an existing group overview</li>
     * <li>view an existing group short info</li>
     * </ul>
     */
    protected void initGroupObject() {

        Object o = null;

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing user, get the user object from db
                m_group = getCms().readGroup(new CmsUUID(getParamGroupid()));
            } else {
                // this is not the initial call, get the user object from session            
                o = getDialogObject();
                m_group = (CmsGroup)o;
                // test
                m_group.getId();
            }
            if ((m_group.getParentId() != null) && !m_group.getParentId().isNullUUID()) {
                setParentGroup(getCms().getParent(m_group.getName()).getName());
            }
        } catch (Exception e) {
            // create a new user object
            m_group = new CmsGroup();
            setParentGroup(null);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the group (may be changed because of the widget values)
        setDialogObject(m_group);
    }

    /**
     * Tests if the given group is editable or not.<p>
     * 
     * Not editable means that no property can be changed.<p>
     * 
     * @param group the group to test 
     * 
     * @return the editable flag
     */
    protected abstract boolean isEditable(CmsGroup group);

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        if (!isNewGroup()) {
            // test the needed parameters
            getCms().readGroup(new CmsUUID(getParamGroupid())).getName();
        }
    }

    /**
     * Checks if the new Group dialog has to be displayed.<p>
     * 
     * @return <code>true</code> if the new Group dialog has to be displayed
     */
    private boolean isNewGroup() {

        return getCurrentToolPath().equals(getListRootPath() + "/new");
    }
}
