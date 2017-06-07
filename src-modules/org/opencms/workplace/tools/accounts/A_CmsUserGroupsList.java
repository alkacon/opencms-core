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

import org.opencms.file.CmsGroup;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generalized user groups view.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsUserGroupsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON_DIRECT = "aid";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON_INDIRECT = "aii";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_DIRECT = "asd";

    /** list action id constant. */
    public static final String LIST_ACTION_STATE_INDIRECT = "asi";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "co";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_OTHEROU = "doo";

    /** Cached value. */
    private Boolean m_hasGroupsInOtherOus;

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramUsername;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     * @param searchable searchable flag
     */
    protected A_CmsUserGroupsList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        boolean searchable) {

        super(
            jsp,
            listId,
            listName,
            LIST_COLUMN_DISPLAY,
            CmsListOrderEnum.ORDER_ASCENDING,
            searchable ? LIST_COLUMN_DISPLAY : null);
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
     * Returns the user id parameter value.<p>
     *
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Returns the User name parameter value.<p>
     *
     * @return the User name parameter value
     */
    public String getParamUsername() {

        return m_paramUsername;
    }

    /**
     * Returns if the list of groups has groups of other organizational units.<p>
     *
     * @return if the list of groups has groups of other organizational units
     */
    public boolean hasGroupsInOtherOus() {

        if (m_hasGroupsInOtherOus == null) {
            // lazzy initialization
            m_hasGroupsInOtherOus = Boolean.FALSE;
            try {
                List<CmsGroup> groups = getGroups(true);
                Iterator<CmsGroup> itGroups = groups.iterator();
                while (itGroups.hasNext()) {
                    CmsGroup group = itGroups.next();
                    if (!group.getOuFqn().equals(getParamOufqn())) {
                        m_hasGroupsInOtherOus = Boolean.TRUE;
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return m_hasGroupsInOtherOus.booleanValue();
    }

    /**
     * Sets the user organizational unit value.<p>
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
     * Sets the user id parameter value.<p>
     *
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the User name parameter value.<p>
     *
     * @param username the username to set
     */
    public void setParamUsername(String username) {

        m_paramUsername = username;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * Returns a list of groups to display.<p>
     *
     * @param withOtherOus if not set only groups of the current ou should be returned
     *
     * @return a list of <code><{@link CmsGroup}</code>s
     *
     * @throws CmsException if something goes wrong
     */
    protected abstract List<CmsGroup> getGroups(boolean withOtherOus) throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        CmsListItemDetails details = getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_OTHEROU);
        boolean withOtherOus = hasGroupsInOtherOus() && (details != null) && details.isVisible();
        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        // get content
        List<CmsGroup> groups = getGroups(withOtherOus);
        Iterator<CmsGroup> itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DISPLAY, OpenCms.getWorkplaceManager().translateGroupName(group.getName(), false));
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription(getLocale()));
            item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + group.getOuFqn());
            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#initializeDetail(java.lang.String)
     */
    @Override
    protected void initializeDetail(String detailId) {

        super.initializeDetail(detailId);
        if (detailId.equals(LIST_DETAIL_OTHEROU)) {
            boolean visible = hasGroupsInOtherOus()
                && getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_OTHEROU).isVisible();
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setVisible(visible);
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setPrintable(visible);
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // add icon actions
        setIconAction(iconCol);
        // add it to the list definition
        metadata.addColumn(iconCol);
        // add state column and actions
        setStateActionCol(metadata);

        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setVisible(false);
        metadata.addColumn(nameCol);

        // create column for display name
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        displayCol.setWidth("35%");
        // add default actions
        setDefaultAction(displayCol);
        // add it to the list definition
        metadata.addColumn(displayCol);

        // create column for orgunit
        CmsListColumnDefinition orgunitCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        orgunitCol.setName(Messages.get().container(Messages.GUI_USERS_LIST_COLS_ORGUNIT_0));
        orgunitCol.setVisible(false);
        // add it to the list definition
        metadata.addColumn(orgunitCol);

        // create column for description
        CmsListColumnDefinition descCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_DESCRIPTION_0));
        descCol.setWidth("65%");
        descCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(descCol);
    }

    /**
     * Sets the optional login default action.<p>
     *
     * @param nameCol the group name column
     */
    protected abstract void setDefaultAction(CmsListColumnDefinition nameCol);

    /**
     * Sets the needed icon action(s).<p>
     *
     * @param iconCol the list column for edition.
     */
    protected abstract void setIconAction(CmsListColumnDefinition iconCol);

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add user address details
        CmsListItemDetails otherOuDetails = new CmsListItemDetails(LIST_DETAIL_OTHEROU);
        otherOuDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_OTHEROU) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((A_CmsUserGroupsList)getWp()).hasGroupsInOtherOus();
            }
        });
        otherOuDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_OTHEROU) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((A_CmsUserGroupsList)getWp()).hasGroupsInOtherOus();
            }
        });
        otherOuDetails.setShowActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_OTHEROU_NAME_0));
        otherOuDetails.setShowActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_OTHEROU_HELP_0));
        otherOuDetails.setHideActionName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_OTHEROU_NAME_0));
        otherOuDetails.setHideActionHelpText(Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_OTHEROU_HELP_0));
        otherOuDetails.setName(Messages.get().container(Messages.GUI_GROUPS_DETAIL_OTHEROU_NAME_0));
        otherOuDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_GROUPS_DETAIL_OTHEROU_NAME_0)));
        otherOuDetails.setVisible(true);
        metadata.addItemDetails(otherOuDetails);
    }

    /**
     * Sets the optional state change action column.<p>
     *
     * @param metadata the list metadata object
     */
    protected abstract void setStateActionCol(CmsListMetadata metadata);

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        m_paramUsername = getCms().readUser(new CmsUUID(getParamUserid())).getName();
    }
}
