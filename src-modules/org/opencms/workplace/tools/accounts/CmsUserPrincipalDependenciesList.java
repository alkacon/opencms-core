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
 * User dependencies list view.<p>
 *
 * @since 8.0.0
 */
public class CmsUserPrincipalDependenciesList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATED = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LASTMODIFIED = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PERMISSIONS = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "ct";

    /** List id constant. */
    public static final String LIST_ID = "lud";

    /** Stores the value of the request parameter for the user id, could be a list of ids. */
    private String m_paramUserid;

    /** Flag to show resources with file attributes */
    protected boolean m_showAttributes;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUserPrincipalDependenciesList(CmsJspActionElement jsp) {

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
    public CmsUserPrincipalDependenciesList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
        m_showAttributes = false;
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsUserPrincipalDependenciesList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_NAME_0),
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
     * Returns the user id parameter value.<p>
     *
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Sets the user id parameter value.<p>
     *
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
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
        Iterator<String> itUsers = CmsStringUtil.splitAsList(
            getParamUserid(),
            CmsHtmlList.ITEM_SEPARATOR,
            true).iterator();
        String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
        try {
            getCms().getRequestContext().setSiteRoot("/");
            while (itUsers.hasNext()) {
                CmsUser user = getCms().readUser(new CmsUUID(itUsers.next().toString()));
                // get content
                Set<CmsResource> resources = getCms().getResourcesForPrincipal(user.getId(), null, m_showAttributes);
                Iterator<CmsResource> itRes = resources.iterator();
                while (itRes.hasNext()) {
                    CmsResource resource = itRes.next();
                    CmsListItem item = ret.getObject(resource.getResourceId().toString());
                    if (item == null) {
                        String userCreated = resource.getUserCreated().toString();
                        try {
                            userCreated = getCms().readUser(resource.getUserCreated()).getFullName();
                        } catch (CmsException exc) {
                            // noop - user id could not be resolved, so display it
                        }
                        String userLastmodified = resource.getUserLastModified().toString();
                        try {
                            userLastmodified = getCms().readUser(resource.getUserLastModified()).getFullName();
                        } catch (CmsException exc) {
                            // noop - user id could not be resolved, so display it
                        }
                        item = getList().newItem(resource.getResourceId().toString());
                        item.set(LIST_COLUMN_NAME, resource.getRootPath());
                        item.set(LIST_COLUMN_TYPE, Integer.valueOf(resource.getTypeId()));
                        item.set(LIST_COLUMN_CREATED, userCreated);
                        item.set(LIST_COLUMN_LASTMODIFIED, userLastmodified);
                        Iterator<CmsAccessControlEntry> itAces = getCms().getAccessControlEntries(
                            resource.getRootPath(),
                            false).iterator();
                        while (itAces.hasNext()) {
                            CmsAccessControlEntry ace = itAces.next();
                            if (ace.getPrincipal().equals(user.getId())) {
                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                                    ace.getPermissions().getPermissionString())) {
                                    item.set(
                                        LIST_COLUMN_PERMISSIONS,
                                        user.getName() + ": " + ace.getPermissions().getPermissionString());
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
                            if (ace.getPrincipal().equals(user.getId())) {
                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                                    ace.getPermissions().getPermissionString())) {
                                    String data = user.getName() + ": " + ace.getPermissions().getPermissionString();
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

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add icon action
        CmsListDirectAction iconAction = new CmsDependencyIconAction(
            LIST_ACTION_ICON,
            CmsDependencyIconActionType.RESOURCE,
            getCms());
        iconAction.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_ACTION_ICON_HELP_0));
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);

        // add it to the list definition
        metadata.addColumn(iconCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_NAME_0));
        nameCol.setWidth("50%");
        metadata.addColumn(nameCol);

        // add column for created by
        CmsListColumnDefinition createdCol = new CmsListColumnDefinition(LIST_COLUMN_CREATED);
        createdCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_CREATED_0));
        createdCol.setWidth("20%");
        metadata.addColumn(createdCol);

        // add column for last modified by
        CmsListColumnDefinition lastModifiedCol = new CmsListColumnDefinition(LIST_COLUMN_LASTMODIFIED);
        lastModifiedCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_LASTMODIFIED_0));
        lastModifiedCol.setWidth("20%");
        metadata.addColumn(lastModifiedCol);

        // add column for permissions
        CmsListColumnDefinition permissionsCol = new CmsListColumnDefinition(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(Messages.get().container(Messages.GUI_USER_DEPENDENCIES_LIST_COLS_PERMISSIONS_0));
        permissionsCol.setWidth("20%");
        metadata.addColumn(permissionsCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(new CmsMessageContainer(null, "type"));
        typeCol.setVisible(false);
        metadata.addColumn(typeCol);
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