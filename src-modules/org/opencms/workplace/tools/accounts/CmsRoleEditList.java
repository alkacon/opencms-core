/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsRoleEditList.java,v $
 * Date   : $Date: 2007/01/31 15:57:03 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDefaultComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * User roles overview view.<p>
 * 
 * @author Raphael Schnuck  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.6 
 */
public class CmsRoleEditList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ACTIVATE = "aa";

    /** list action id constant. */
    public static final String LIST_ACTION_DEACTIVATE = "ac";

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_ACTIVATE = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_DEPENDENCY = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_HIDE_NAME = "chn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_DESCRIPTION = "dd";

    /** list id constant. */
    public static final String LIST_ID = "lsre";

    /** list action id constant. */
    public static final String LIST_MACTION_ACTIVATE = "ma";

    /** list action id constant. */
    public static final String LIST_MACTION_DEACTIVATE = "mc";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsRoleEditList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID);
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     */
    public CmsRoleEditList(CmsJspActionElement jsp, String listId) {

        this(jsp, listId, Messages.get().container(Messages.GUI_ROLEEDIT_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRoleEditList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the name of the list
     */
    protected CmsRoleEditList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        super(jsp, listId, listName, LIST_COLUMN_HIDE_NAME, CmsListOrderEnum.ORDER_ASCENDING, null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_ACTIVATE)) {
            // execute the activate multiaction
            try {
                CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    CmsGroup group = getCms().readGroup((String)listItem.get(LIST_COLUMN_HIDE_NAME));
                    CmsRole role = CmsRole.valueOf(group);
                    if (!OpenCms.getRoleManager().hasRole(getCms(), user.getName(), role)) {
                        OpenCms.getRoleManager().addUserToRole(getCms(), role, user.getName());
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_ACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else if (getParamListAction().equals(LIST_MACTION_DEACTIVATE)) {
            // execute the activate multiaction
            try {
                CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    CmsGroup group = getCms().readGroup((String)listItem.get(LIST_COLUMN_HIDE_NAME));
                    CmsRole role = CmsRole.valueOf(group);
                    if (OpenCms.getRoleManager().hasRole(getCms(), user.getName(), role)) {
                        OpenCms.getRoleManager().removeUserFromRole(getCms(), role, user.getName());
                        getCms().writeUser(user);
                    }
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DEACTIVATE_SELECTED_USERS_0), e);
            }
            // refreshing no needed becaus the activate action does not add/remove rows to the list
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        String roleName = getSelectedItem().getId();
        try {
            CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
            CmsUser user = getCms().readUser(new CmsUUID(getParamUserid()));

            if (getParamListAction().equals(LIST_ACTION_ACTIVATE)) {
                // execute the activate action
                try {
                    OpenCms.getRoleManager().addUserToRole(getCms(), role, user.getName());
                    getCms().writeUser(user);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(Messages.get().container(
                        Messages.ERR_ACTIVATE_ROLE_2,
                        roleName,
                        user.getName()), e);
                }
            } else if (getParamListAction().equals(LIST_ACTION_DEACTIVATE)) {
                // execute the activate action
                try {
                    OpenCms.getRoleManager().removeUserFromRole(getCms(), role, user.getName());
                    getCms().writeUser(user);
                } catch (CmsException e) {
                    throw new CmsRuntimeException(Messages.get().container(
                        Messages.ERR_DEACTIVATE_ROLE_2,
                        roleName,
                        user.getName()), e);
                }
            } else {
                throwListUnsupportedActionException();
            }
            listSave();
        } catch (CmsException e) {
            // should never happen
        }
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
     * Returns the User id parameter value.<p>
     *
     * @return the User id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
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
     * Sets the User id parameter value.<p>
     * 
     * @param userid the userid to set
     */
    public void setParamUserid(String userid) {

        m_paramUserid = userid;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        return getList().listJs() + dialogContentStart(getParamTitle());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List roles = getList().getAllContent();
        Iterator itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsListItem item = (CmsListItem)itRoles.next();
            String roleName = item.get(LIST_COLUMN_HIDE_NAME).toString();
            StringBuffer html = new StringBuffer(512);
            try {
                if (detailId.equals(LIST_DETAIL_DESCRIPTION)) {
                    CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                    html.append(role.getDescription(getCms().getRequestContext().getLocale()));
                } else {
                    continue;
                }
            } catch (Exception e) {
                // noop
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        List roles = OpenCms.getRoleManager().getRolesOfUser(
            getCms(),
            getCms().getRequestContext().currentUser().getName(),
            getParamOufqn(),
            false,
            false,
            true);

        Iterator itRoles = roles.iterator();
        while (itRoles.hasNext()) {
            CmsGroup group = (CmsGroup)itRoles.next();
            CmsRole role = CmsRole.valueOf(group);
            CmsListItem item = getList().newItem(group.getName());
            Locale locale = getCms().getRequestContext().getLocale();
            item.set(LIST_COLUMN_NAME, role.getName(locale));
            item.set(LIST_COLUMN_HIDE_NAME, group.getName());
            String dependency = "";
            while (role.getParentRole() != null) {
                dependency = dependency + role.getParentRole().getName(locale);
                role = role.getParentRole();
                if (role.getParentRole() != null) {
                    dependency = dependency + ", ";
                }
            }
            item.set(LIST_COLUMN_DEPENDENCY, dependency);
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        // adds a role icon
        CmsListDirectAction dirAction = new CmsListDefaultAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#getIconPath()
             */
            public String getIconPath() {

                try {
                    CmsRole role = CmsRole.valueOf(getCms().readGroup((String)getItem().get(LIST_COLUMN_HIDE_NAME)));
                    if (!OpenCms.getRoleManager().hasRole(
                        getCms(),
                        getCms().readUser(new CmsUUID(getParamUserid())).getName(),
                        role)) {
                        return PATH_BUTTONS + "role_inactive.png";
                    }
                    return super.getIconPath();
                } catch (CmsException e) {
                    return super.getIconPath();
                }
            }
        };
        dirAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_NAME_0));
        dirAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ICON_HELP_0));
        dirAction.setIconPath(PATH_BUTTONS + "role.png");
        dirAction.setEnabled(false);
        iconCol.addDirectAction(dirAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for activation/deactivation
        CmsListColumnDefinition actCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVATE);
        actCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ACTIVATE_0));
        actCol.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_ACTIVATE_HELP_0));
        actCol.setWidth("20");
        actCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        actCol.setListItemComparator(new CmsListItemActionIconComparator());

        // activate action
        CmsListDirectAction actAction = new CmsListDirectAction(LIST_ACTION_ACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String roleName = getItem().getId();
                    try {
                        CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                        if (OpenCms.getRoleManager().hasRole(
                            getCms(),
                            getCms().readUser(new CmsUUID(getParamUserid())).getName(),
                            role)) {
                            return false;
                        }
                        return true;
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        actAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_ACTIVATE_NAME_0));
        actAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_ACTIVATE_HELP_0));
        actAction.setIconPath(ICON_INACTIVE);
        actCol.addDirectAction(actAction);

        // deactivate action
        CmsListDirectAction deactAction = new CmsListDirectAction(LIST_ACTION_DEACTIVATE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                if (getItem() != null) {
                    String roleName = getItem().getId();
                    try {
                        CmsRole role = CmsRole.valueOf(getCms().readGroup(roleName));
                        if (OpenCms.getRoleManager().hasRole(
                            getCms(),
                            getCms().readUser(new CmsUUID(getParamUserid())).getName(),
                            role)) {
                            return true;
                        }
                        return false;
                    } catch (CmsException e) {
                        return false;
                    }
                }
                return super.isVisible();
            }
        };
        deactAction.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_DEACTIVATE_NAME_0));
        deactAction.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_ACTION_DEACTIVATE_HELP_0));
        deactAction.setIconPath(ICON_ACTIVE);
        actCol.addDirectAction(deactAction);

        // add it to the list definition
        metadata.addColumn(actCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_NAME_0));
        nameCol.setWidth("40%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // create column for hidden name
        CmsListColumnDefinition hideNameCol = new CmsListColumnDefinition(LIST_COLUMN_HIDE_NAME);
        hideNameCol.setVisible(false);
        hideNameCol.setListItemComparator(new CmsListItemDefaultComparator() {

            /**
             * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
             */
            public Comparator getComparator(final String columnId, final Locale locale) {

                return new Comparator() {

                    /**
                     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                     */
                    public int compare(Object o1, Object o2) {

                        if ((o1 == o2) || !(o1 instanceof CmsListItem) || !(o2 instanceof CmsListItem)) {
                            return 0;
                        }
                        Comparable c1 = (Comparable)((CmsListItem)o1).get(columnId);
                        Comparable c2 = (Comparable)((CmsListItem)o2).get(columnId);
                        if ((c1 instanceof String) && (c2 instanceof String)) {
                            try {
                                CmsRole role1 = CmsRole.valueOf(getCms().readGroup((String)c1));
                                CmsRole role2 = CmsRole.valueOf(getCms().readGroup((String)c2));

                                if (role1.getParentRole().equals(role2)) {
                                    return 1;
                                } else if (role2.getParentRole().equals(role1)) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            } catch (CmsException e) {
                                return 0;
                            }
                        } else if (c1 != null) {
                            if (c2 == null) {
                                return 1;
                            }
                            return c1.compareTo(c2);
                        } else if (c2 != null) {
                            return -1;
                        }
                        return 0;
                    }
                };
            }
        });
        hideNameCol.setSorteable(true);
        // add it to the list definition
        metadata.addColumn(hideNameCol);

        // create column for path
        CmsListColumnDefinition depCol = new CmsListColumnDefinition(LIST_COLUMN_DEPENDENCY);
        depCol.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_COLS_DEPENDENCY_0));
        depCol.setWidth("60%");
        depCol.setTextWrapping(true);
        // add it to the list definition
        metadata.addColumn(depCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add description details
        CmsListItemDetails descriptionDetails = new CmsListItemDetails(LIST_DETAIL_DESCRIPTION);
        descriptionDetails.setAtColumn(LIST_COLUMN_NAME);
        descriptionDetails.setVisible(true);
        descriptionDetails.setShowActionName(Messages.get().container(
            Messages.GUI_ROLEEDIT_DETAIL_SHOW_DESCRIPTION_NAME_0));
        descriptionDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_ROLEEDIT_DETAIL_SHOW_DESCRIPTION_HELP_0));
        descriptionDetails.setHideActionName(Messages.get().container(
            Messages.GUI_ROLEEDIT_DETAIL_HIDE_DESCRIPTION_NAME_0));
        descriptionDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_ROLEEDIT_DETAIL_HIDE_DESCRIPTION_HELP_0));
        descriptionDetails.setName(Messages.get().container(Messages.GUI_ROLEEDIT_DETAIL_DESCRIPTION_NAME_0));
        descriptionDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_ROLEEDIT_DETAIL_DESCRIPTION_NAME_0)));
        metadata.addItemDetails(descriptionDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the activate role multi action
        CmsListMultiAction activateRole = new CmsListMultiAction(LIST_MACTION_ACTIVATE);
        activateRole.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_ACTIVATE_NAME_0));
        activateRole.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_ACTIVATE_HELP_0));
        activateRole.setIconPath(ICON_MULTI_ACTIVATE);
        metadata.addMultiAction(activateRole);

        // add the deactivate role multi action
        CmsListMultiAction deactivateRole = new CmsListMultiAction(LIST_MACTION_DEACTIVATE);
        deactivateRole.setName(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_DEACTIVATE_NAME_0));
        deactivateRole.setHelpText(Messages.get().container(Messages.GUI_ROLEEDIT_LIST_MACTION_DEACTIVATE_HELP_0));
        deactivateRole.setIconPath(ICON_MULTI_DEACTIVATE);
        metadata.addMultiAction(deactivateRole);
    }

}
