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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.CmsIdentifiableObjectContainer;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Group dependencies list view.<p>
 *
 * @since 8.0.0
 */
public class CmsGroupPrincipalDependenciesList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PERMISSIONS = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "ct";

    /** List id constant. */
    public static final String LIST_ID = "lgd";

    /** Stores the value of the request parameter for the group id, could be a list of ids. */
    private String m_paramGroupid;

    /** Flag to show resources with file attributes. */
    protected boolean m_showAttributes;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupPrincipalDependenciesList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
        m_showAttributes = false;
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupPrincipalDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_showAttributes = false;
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsGroupPrincipalDependenciesList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_NAME_0),
            LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_NAME);
        m_showAttributes = false;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * Returns the group id parameter value.<p>
     *
     * @return the group id parameter value
     */
    public String getParamGroupid() {

        return m_paramGroupid;
    }

    /**
     * Sets the group id parameter value.<p>
     *
     * @param groupId the group id parameter value
     */
    public void setParamGroupid(String groupId) {

        m_paramGroupid = groupId;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        CmsIdentifiableObjectContainer<CmsListItem> ret = new CmsIdentifiableObjectContainer<CmsListItem>(true, false);
        Iterator<String> itGroups = CmsStringUtil.splitAsList(
            getParamGroupid(),
            CmsHtmlList.ITEM_SEPARATOR,
            true).iterator();
        String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
        try {
            getCms().getRequestContext().setSiteRoot("/");
            while (itGroups.hasNext()) {
                CmsGroup group = getCms().readGroup(new CmsUUID(itGroups.next()));
                // get content
                Set<CmsResource> resources = getCms().getResourcesForPrincipal(group.getId(), null, m_showAttributes);
                Iterator<CmsResource> itRes = resources.iterator();
                while (itRes.hasNext()) {
                    CmsResource resource = itRes.next();
                    CmsListItem item = ret.getObject(resource.getResourceId().toString());
                    if (item == null) {
                        item = getList().newItem(resource.getResourceId().toString());
                        item.set(LIST_COLUMN_NAME, resource.getRootPath());
                        item.set(LIST_COLUMN_TYPE, Integer.valueOf(resource.getTypeId()));
                        Iterator<CmsAccessControlEntry> itAces = getCms().getAccessControlEntries(
                            resource.getRootPath(),
                            false).iterator();
                        while (itAces.hasNext()) {
                            CmsAccessControlEntry ace = itAces.next();
                            if (ace.getPrincipal().equals(group.getId())) {
                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                                    ace.getPermissions().getPermissionString())) {
                                    item.set(
                                        LIST_COLUMN_PERMISSIONS,
                                        group.getName() + ": " + ace.getPermissions().getPermissionString());
                                }
                                break;
                            }
                        }
                        ret.addIdentifiableObject(item.getId(), item);
                    } else {
                        String oldData = (String)item.get(LIST_COLUMN_PERMISSIONS);
                        Iterator<CmsAccessControlEntry> itAces = getCms().getAccessControlEntries(
                            resource.getRootPath(),
                            false).iterator();
                        while (itAces.hasNext()) {
                            CmsAccessControlEntry ace = itAces.next();
                            if (ace.getPrincipal().equals(group.getId())) {
                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                                    ace.getPermissions().getPermissionString())) {
                                    String data = group.getName() + ": " + ace.getPermissions().getPermissionString();
                                    if (oldData != null) {
                                        data = oldData + ", " + data;
                                    }
                                    item.set(LIST_COLUMN_PERMISSIONS, data);
                                }
                                break;
                            }
                        }
                    }
                }
                // add users
                Iterator<CmsUser> itUsers = getCms().getUsersOfGroup(group.getName()).iterator();
                while (itUsers.hasNext()) {
                    CmsUser user = itUsers.next();
                    CmsListItem item = ret.getObject(user.getId().toString());
                    if (item == null) {
                        item = getList().newItem(user.getId().toString());
                        item.set(LIST_COLUMN_NAME, user.getName());
                        item.set(LIST_COLUMN_PERMISSIONS, "--");
                        ret.addIdentifiableObject(item.getId(), item);
                    }
                }
                // add child groups
                Iterator<CmsGroup> itChildren = getCms().getChildren(group.getName(), false).iterator();
                while (itChildren.hasNext()) {
                    CmsGroup child = itChildren.next();
                    CmsListItem item = ret.getObject(child.getId().toString());
                    if (item == null) {
                        item = getList().newItem(child.getId().toString());
                        item.set(LIST_COLUMN_NAME, child.getName());
                        item.set(LIST_COLUMN_PERMISSIONS, "--");
                        ret.addIdentifiableObject(item.getId(), item);
                    }
                }
            }
        } finally {
            getCms().getRequestContext().setSiteRoot(storedSiteRoot);
        }
        return ret.elementList();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add cms dialog resource bundle
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add icon actions
        setIconActions(iconCol);

        // add it to the list definition
        metadata.addColumn(iconCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_COLS_NAME_0));
        nameCol.setWidth("80%");
        metadata.addColumn(nameCol);

        // add column for permissions
        CmsListColumnDefinition permissionsCol = new CmsListColumnDefinition(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_COLS_PERMISSIONS_0));
        permissionsCol.setWidth("20%");
        metadata.addColumn(permissionsCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(new CmsMessageContainer(null, "type"));
        typeCol.setVisible(false);
        metadata.addColumn(typeCol);
    }

    /**
     * Sets the right icon actions for the dialog.<p>
     *
     * @param iconCol the column to set the actions
     */
    protected void setIconActions(CmsListColumnDefinition iconCol) {

        // add resource icon action
        CmsListDirectAction resourceIconAction = new CmsDependencyIconAction(
            LIST_ACTION_ICON,
            CmsDependencyIconActionType.RESOURCE,
            getCms());
        resourceIconAction.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_RES_NAME_0));
        resourceIconAction.setHelpText(
            Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_RES_HELP_0));
        resourceIconAction.setEnabled(false);
        iconCol.addDirectAction(resourceIconAction);

        // add group icon action
        CmsListDirectAction groupIconAction = new CmsDependencyIconAction(
            LIST_ACTION_ICON,
            CmsDependencyIconActionType.GROUP,
            getCms());
        groupIconAction.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_GRP_NAME_0));
        groupIconAction.setHelpText(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_GRP_HELP_0));
        groupIconAction.setEnabled(false);
        iconCol.addDirectAction(groupIconAction);

        // add group icon action
        CmsListDirectAction userIconAction = new CmsDependencyIconAction(
            LIST_ACTION_ICON,
            CmsDependencyIconActionType.USER,
            getCms());
        userIconAction.setName(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_USR_NAME_0));
        userIconAction.setHelpText(Messages.get().container(Messages.GUI_GROUP_DEPENDENCIES_LIST_ACTION_USR_HELP_0));
        userIconAction.setEnabled(false);
        iconCol.addDirectAction(userIconAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }
}