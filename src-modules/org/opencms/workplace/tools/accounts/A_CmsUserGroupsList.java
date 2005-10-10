/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsUserGroupsList.java,v $
 * Date   : $Date: 2006/03/27 14:52:49 $
 * Version: $Revision: 1.18 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generalized user groups view.<p>
 * 
 * @author Michael Moossen 
 *  
 * @version $Revision: 1.18 $ 
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
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATE = "cs";

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

        super(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, searchable ? LIST_COLUMN_NAME
        : null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    public CmsHtmlList getList() {

        // assure we have the right username
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition col = list.getMetadata().getColumnDefinition(LIST_COLUMN_ICON);
            if (col != null) {
                Iterator it = col.getDirectActions().iterator();
                while (it.hasNext()) {
                    I_CmsListDirectAction action = (I_CmsListDirectAction)it.next();
                    if (action instanceof CmsGroupStateAction) {
                        ((CmsGroupStateAction)action).setUserName(m_paramUsername);
                    }
                }
            }
        }
        return list;
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
     * Returns the User name parameter.<p>
     *
     * @return the User name paramter
     */
    public String getParamUsername() {

        return m_paramUsername;
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
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * Returns a list of groups to display.<p>
     * 
     * @return a list of <code><{@link CmsGroup}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract List getGroups() throws CmsException;

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();

        // get content        
        List groups = getGroups();
        Iterator itGroups = groups.iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            CmsListItem item = getList().newItem(group.getId().toString());
            item.set(LIST_COLUMN_NAME, group.getName());
            item.set(LIST_COLUMN_DESCRIPTION, group.getDescription());
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
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

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_GROUPS_LIST_COLS_NAME_0));
        nameCol.setWidth("35%");
        // add default actions
        setDefaultAction(nameCol);

        // add it to the list definition
        metadata.addColumn(nameCol);

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
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
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
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        m_paramUsername = getCms().readUser(new CmsUUID(getParamUserid())).getName();
    }
}
