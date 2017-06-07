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

package org.opencms.workplace.commons;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.workplace.I_CmsGroupNameTranslation;
import org.opencms.workplace.list.A_CmsListDefaultJsAction;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;
import org.opencms.workplace.list.I_CmsListItemComparator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Principal selection dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsPrincipalSelectionList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_SELECT = "js";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "cou";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_OTHEROU = "doo";

    /** list action id constant. */
    public static final String LIST_IACTION_GROUPS = "iag";

    /** list action id constant. */
    public static final String LIST_IACTION_USERS = "iau";

    /** list id constant. */
    public static final String LIST_ID = "lus";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/accounts/buttons/";

    /** Item comparator to ensure that special principals go first. */
    private static final I_CmsListItemComparator LIST_ITEM_COMPARATOR = new I_CmsListItemComparator() {

        /**
         * @see org.opencms.workplace.list.I_CmsListItemComparator#getComparator(java.lang.String, java.util.Locale)
         */
        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public Comparator getComparator(final String columnId, final Locale locale) {

            final Collator collator = Collator.getInstance(locale);
            final String overwriteAll = Messages.get().getBundle(locale).key(Messages.GUI_LABEL_OVERWRITEALL_0);
            final String allOthers = Messages.get().getBundle(locale).key(Messages.GUI_LABEL_ALLOTHERS_0);

            return new Comparator() {

                /**
                 * @see org.opencms.security.CmsAccessControlEntry#COMPARATOR_PRINCIPALS
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Object o1, Object o2) {

                    if ((o1 == o2) || !(o1 instanceof CmsListItem) || !(o2 instanceof CmsListItem)) {
                        return 0;
                    }
                    CmsListItem li1 = (CmsListItem)o1;
                    CmsListItem li2 = (CmsListItem)o2;

                    String id1 = (String)li1.get(LIST_COLUMN_DISPLAY);
                    String id2 = (String)li2.get(LIST_COLUMN_DISPLAY);
                    if (id1.equals(id2)) {
                        return 0;
                    } else if (id1.equals(overwriteAll)) {
                        return -1;
                    } else if (id1.equals(allOthers)) {
                        if (id2.equals(overwriteAll)) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else if (id2.equals(allOthers)) {
                        if (id1.equals(overwriteAll)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else if (id2.equals(overwriteAll)) {
                        return 1;
                    }

                    Comparable c1 = (Comparable)li1.get(columnId);
                    Comparable c2 = (Comparable)li2.get(columnId);
                    if ((c1 instanceof String) && (c2 instanceof String)) {
                        return collator.compare(c1, c2);
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

    };

    /** Cached value. */
    private Boolean m_hasPrincipalsInOtherOus;

    /** Stores the value of the request parameter for the flags. */
    private String m_paramFlags;

    /** The use parent flag, indicates the value should be set in the parent frame, not the window opener. */
    private String m_paramUseparent;

    /** The 'realonly' parameter value.*/
    private String m_realOnly;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPrincipalSelectionList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID + getListInstanceHash(jsp.getRequest()),
            Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_NAME_0),
            LIST_COLUMN_DISPLAY,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPrincipalSelectionList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Gets the hash which is appended to the list name to form the list id.<p>
     *
     * @param request the current request
     * @return the list instance hash
     */
    private static String getListInstanceHash(HttpServletRequest request) {

        String result;
        if ((request != null) && (request.getSession(false) != null)) {
            result = "_" + String.valueOf(request.getSession(false).getId().hashCode());

        } else {
            result = "";
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#dialogTitle()
     */
    @Override
    public String dialogTitle() {

        // build title
        StringBuffer html = new StringBuffer(512);
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        if (getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).isVisible()) {
            html.append(key(Messages.GUI_GROUPSELECTION_INTRO_TITLE_0));
            getList().setName(Messages.get().container(Messages.GUI_GROUPSELECTION_LIST_NAME_0));
            getList().getMetadata().getIndependentAction(LIST_DETAIL_OTHEROU);
        } else {
            html.append(key(Messages.GUI_USERSELECTION_INTRO_TITLE_1, new Object[] {""}));
            getList().setName(Messages.get().container(Messages.GUI_USERSELECTION_LIST_NAME_0));
        }
        html.append("\n\t\t\t</td>");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    @Override
    public void executeListIndepActions() {

        if (LIST_IACTION_USERS.equals(getParamListAction())) {
            getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).setVisible(false);
            getList().getMetadata().getIndependentAction(LIST_IACTION_GROUPS).setVisible(true);
        } else if (LIST_IACTION_GROUPS.equals(getParamListAction())) {
            getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).setVisible(true);
            getList().getMetadata().getIndependentAction(LIST_IACTION_GROUPS).setVisible(false);
        }
        super.executeListIndepActions();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * Returns the right icon path for the given list item.<p>
     *
     * @param item the list item to get the icon path for
     *
     * @return the icon path for the given role
     */
    public String getIconPath(CmsListItem item) {

        boolean showingUsers = isShowingUsers();
        try {
            CmsPrincipal principal;
            if (showingUsers) {
                principal = getCms().readUser((String)item.get(LIST_COLUMN_NAME));
            } else {
                principal = getCms().readGroup((String)item.get(LIST_COLUMN_NAME));
            }
            if (principal.getOuFqn().equals(getCms().getRequestContext().getCurrentUser().getOuFqn())) {
                if (showingUsers) {
                    return PATH_BUTTONS + "user.png";
                } else {
                    return PATH_BUTTONS + "group.png";
                }
            } else {
                if (showingUsers) {
                    return PATH_BUTTONS + "user_other_ou.png";
                } else {
                    return PATH_BUTTONS + "group_other_ou.png";
                }
            }
        } catch (CmsException e) {
            if (item.get(LIST_COLUMN_DISPLAY).equals(key(Messages.GUI_LABEL_OVERWRITEALL_0))) {
                return "commons/" + CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME.toLowerCase() + ".png";
            } else if (item.get(LIST_COLUMN_DISPLAY).equals(key(Messages.GUI_LABEL_ALLOTHERS_0))) {
                return "commons/" + CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME.toLowerCase() + ".png";
            } else if (showingUsers) {
                return PATH_BUTTONS + "user.png";
            } else {
                return PATH_BUTTONS + "group.png";
            }
        }
    }

    /**
     * Returns the flags parameter value.<p>
     *
     * @return the flags parameter value
     */
    public String getParamFlags() {

        return m_paramFlags;
    }

    /**
     * Gets the 'realonly parameter'.<p>
     *
     * This controls whether pseudo-principals like 'ALL OTHERS' should be shown or not.<p>
     *
     * @return the parameter value
     */
    public String getParamRealonly() {

        return m_realOnly;
    }

    /**
     * Returns the use parent frame flag.<p>
     *
     * @return the use parent frame flag
     */
    public String getParamUseparent() {

        return m_paramUseparent;
    }

    /**
     * Returns if the list of principals has principals of other organizational units.<p>
     *
     * @return if the list of principals has principals of other organizational units
     */
    public boolean hasPrincipalsInOtherOus() {

        if (m_hasPrincipalsInOtherOus == null) {
            // lazzy initialization
            m_hasPrincipalsInOtherOus = Boolean.FALSE;
            try {
                Iterator<CmsPrincipal> itPrincipals = getPrincipals(true).iterator();
                while (itPrincipals.hasNext()) {
                    CmsPrincipal principal = itPrincipals.next();
                    if (!principal.getOuFqn().equals(getCms().getRequestContext().getCurrentUser().getOuFqn())) {
                        m_hasPrincipalsInOtherOus = Boolean.TRUE;
                        break;
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return m_hasPrincipalsInOtherOus.booleanValue();
    }

    /**
     * Checks if we are currently displaying users or groups.<p>
     *
     * @return <code>true</code> if we are currently displaying users
     */
    public boolean isShowingUsers() {

        return getList().getMetadata().getIndependentAction(LIST_IACTION_GROUPS).isVisible();
    }

    /**
     * Sets the flags parameter value.<p>
     *
     * @param flags the flags parameter value to set
     */
    public void setParamFlags(String flags) {

        m_paramFlags = flags;
    }

    /**
     * Sets the 'realonly' parameter.<p>
     *
     * This controls whether 'pseudo-principals' like 'ALL OTHERS' should be shown or not.
     *
     * @param realonly the parameter value
     */
    public void setParamRealonly(String realonly) {

        m_realOnly = realonly;
    }

    /**
     * Sets the use parent frame flag.<p>
     *
     * @param useParent the use parent frame flag
     */
    public void setParamUseparent(String useParent) {

        m_paramUseparent = useParent;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        boolean withOtherOus = hasPrincipalsInOtherOus()
            && getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_OTHEROU).isVisible();

        // get content
        Iterator<CmsPrincipal> itPrincipals = getPrincipals(withOtherOus).iterator();
        while (itPrincipals.hasNext()) {
            I_CmsPrincipal principal = itPrincipals.next();
            CmsListItem item = getList().newItem(principal.getId().toString());
            item.set(LIST_COLUMN_NAME, principal.getName());

            I_CmsGroupNameTranslation translation = OpenCms.getWorkplaceManager().getGroupNameTranslation();
            if (principal.isGroup()) {
                item.set(LIST_COLUMN_DISPLAY, translation.translateGroupName(principal.getName(), false));
            } else {
                item.set(LIST_COLUMN_DISPLAY, principal.getSimpleName());
            }
            if (principal.isUser()) {
                if (principal.getId().equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)
                    || principal.getId().equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                    item.set(LIST_COLUMN_DESCRIPTION, ((CmsUser)principal).getDescription(getLocale()));
                } else {
                    item.set(LIST_COLUMN_DESCRIPTION, ((CmsUser)principal).getFullName());
                }
            } else {
                item.set(LIST_COLUMN_DESCRIPTION, ((CmsGroup)principal).getDescription(getLocale()));
            }
            item.set(LIST_COLUMN_ORGUNIT, CmsOrganizationalUnit.SEPARATOR + principal.getOuFqn());
            ret.add(item);
        }
        return ret;
    }

    /**
     * Returns the list of principals for selection.<p>
     *
     * @param includeOtherOus if to include other ou's in the selection
     *
     * @return a list of principals
     *
     * @throws CmsException if womething goes wrong
     */
    protected List<CmsPrincipal> getPrincipals(boolean includeOtherOus) throws CmsException {

        String ou = getCms().getRequestContext().getCurrentUser().getOuFqn();
        Set<CmsPrincipal> principals = new HashSet<CmsPrincipal>();
        boolean realOnly = Boolean.valueOf(getParamRealonly()).booleanValue();
        if (isShowingUsers()) {
            // include special principals
            if (!realOnly) {
                if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER)) {
                    CmsUser user = new CmsUser(
                        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                        key(Messages.GUI_LABEL_OVERWRITEALL_0),
                        "",
                        "",
                        "",
                        "",
                        0,
                        0,
                        0,
                        null);
                    user.setDescription(key(Messages.GUI_DESCRIPTION_OVERWRITEALL_0));
                    principals.add(user);
                }
                CmsUser user = new CmsUser(
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    key(Messages.GUI_LABEL_ALLOTHERS_0),
                    "",
                    "",
                    "",
                    "",
                    0,
                    0,
                    0,
                    null);
                user.setDescription(key(Messages.GUI_DESCRIPTION_ALLOTHERS_0));
                principals.add(user);
            }
            if (includeOtherOus) {
                // add all manageable users
                principals.addAll(OpenCms.getRoleManager().getManageableUsers(getCms(), "", true));
                // add own ou users
                principals.addAll(OpenCms.getOrgUnitManager().getUsers(getCms(), ou, true));
            } else {
                // add own ou users
                principals.addAll(OpenCms.getOrgUnitManager().getUsers(getCms(), ou, false));
            }
        } else {
            // include special principals
            if (!realOnly) {
                if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER)) {
                    principals.add(
                        new CmsGroup(
                            CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                            null,
                            key(Messages.GUI_LABEL_OVERWRITEALL_0),
                            key(Messages.GUI_DESCRIPTION_OVERWRITEALL_0),
                            0));
                }
                principals.add(
                    new CmsGroup(
                        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                        null,
                        key(Messages.GUI_LABEL_ALLOTHERS_0),
                        key(Messages.GUI_DESCRIPTION_ALLOTHERS_0),
                        0));
            }
            if (includeOtherOus) {
                // add all manageable users
                principals.addAll(OpenCms.getRoleManager().getManageableGroups(getCms(), "", true));
                // add own ou users
                principals.addAll(OpenCms.getOrgUnitManager().getGroups(getCms(), ou, true));
            } else {
                // add own ou users
                principals.addAll(OpenCms.getOrgUnitManager().getGroups(getCms(), ou, false));
            }
        }
        List<CmsPrincipal> ret = new ArrayList<CmsPrincipal>(principals);
        if (getParamFlags() != null) {
            int flags = Integer.parseInt(getParamFlags());
            return new ArrayList<CmsPrincipal>(CmsPrincipal.filterFlag(ret, flags));
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
            boolean visible = hasPrincipalsInOtherOus()
                && getList().getMetadata().getItemDetailDefinition(LIST_DETAIL_OTHEROU).isVisible();
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setVisible(visible);
            getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setPrintable(visible);
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon display
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // set icon action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return ((CmsPrincipalSelectionList)getWp()).getIconPath(getItem());
            }
        };
        iconAction.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_ICON_HELP_0));
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        CmsListColumnDefinition loginCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        loginCol.setVisible(false);
        metadata.addColumn(loginCol);
        loginCol.setPrintable(false);

        // create column for display name
        CmsListColumnDefinition displayNameCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayNameCol.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_COLS_NAME_0));
        displayNameCol.setWidth("40%");
        displayNameCol.setListItemComparator(LIST_ITEM_COMPARATOR);
        CmsListDefaultAction selectAction = new A_CmsListDefaultJsAction(LIST_ACTION_SELECT) {

            /**
             * @see org.opencms.workplace.list.A_CmsListDirectJsAction#jsCode()
             */
            @Override
            public String jsCode() {

                if (Boolean.parseBoolean(getParamUseparent())) {
                    return "parent.setPrincipalFormValue("
                        + (((CmsPrincipalSelectionList)getWp()).isShowingUsers() ? 1 : 0)
                        + ",'"
                        + getItem().get(LIST_COLUMN_NAME)
                        + "');";
                } else {
                    return "window.opener.setPrincipalFormValue("
                        + (((CmsPrincipalSelectionList)getWp()).isShowingUsers() ? 1 : 0)
                        + ",'"
                        + getItem().get(LIST_COLUMN_NAME)
                        + "'); window.opener.focus(); window.close();";
                }
            }
        };
        selectAction.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_NAME_0));
        selectAction.setHelpText(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_HELP_0));
        displayNameCol.addDefaultAction(selectAction);
        // add it to the list definition
        metadata.addColumn(displayNameCol);

        // create column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_COLS_DESCRIPTION_0));
        descriptionCol.setWidth("60%");
        descriptionCol.setTextWrapping(true);
        descriptionCol.setListItemComparator(LIST_ITEM_COMPARATOR);
        // add it to the list definition
        metadata.addColumn(descriptionCol);

        // create column for org unit
        CmsListColumnDefinition ouCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        ouCol.setName(Messages.get().container(Messages.GUI_PRINCIPALSELECTION_LIST_COLS_ORGUNIT_0));
        ouCol.setWidth("40%");
        ouCol.setTextWrapping(true);
        ouCol.setListItemComparator(LIST_ITEM_COMPARATOR);
        // add it to the list definition
        metadata.addColumn(ouCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add other ou button
        CmsListItemDetails otherOuDetails = new CmsListItemDetails(LIST_DETAIL_OTHEROU);
        otherOuDetails.setVisible(false);
        otherOuDetails.setHideAction(new CmsListIndependentAction(LIST_DETAIL_OTHEROU) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (getWp().getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).isVisible()) {
                    return Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_OTHEROU_HELP_0);
                } else {
                    return Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_OTHEROU_HELP_0);
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_HIDE;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            @Override
            public CmsMessageContainer getName() {

                if (getWp().getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).isVisible()) {
                    return Messages.get().container(Messages.GUI_GROUPS_DETAIL_HIDE_OTHEROU_NAME_0);
                } else {
                    return Messages.get().container(Messages.GUI_USERS_DETAIL_HIDE_OTHEROU_NAME_0);
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((CmsPrincipalSelectionList)getWp()).hasPrincipalsInOtherOus();
            }
        });
        otherOuDetails.setShowAction(new CmsListIndependentAction(LIST_DETAIL_OTHEROU) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getHelpText()
             */
            @Override
            public CmsMessageContainer getHelpText() {

                if (getWp().getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).isVisible()) {
                    return Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_OTHEROU_HELP_0);
                } else {
                    return Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_OTHEROU_HELP_0);
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getIconPath()
             */
            @Override
            public String getIconPath() {

                return A_CmsListDialog.ICON_DETAILS_SHOW;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#getName()
             */
            @Override
            public CmsMessageContainer getName() {

                if (getWp().getList().getMetadata().getIndependentAction(LIST_IACTION_USERS).isVisible()) {
                    return Messages.get().container(Messages.GUI_GROUPS_DETAIL_SHOW_OTHEROU_NAME_0);
                } else {
                    return Messages.get().container(Messages.GUI_USERS_DETAIL_SHOW_OTHEROU_NAME_0);
                }
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                return ((CmsPrincipalSelectionList)getWp()).hasPrincipalsInOtherOus();
            }
        });
        otherOuDetails.setName(Messages.get().container(Messages.GUI_PRINCIPALS_DETAIL_OTHEROU_NAME_0));
        otherOuDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_PRINCIPALS_DETAIL_OTHEROU_NAME_0)));
        metadata.addItemDetails(otherOuDetails);

        CmsListIndependentAction usersAction = new CmsListIndependentAction(LIST_IACTION_USERS);
        usersAction.setName(Messages.get().container(Messages.GUI_PRINCIPALS_IA_USERS_NAME_0));
        usersAction.setHelpText(Messages.get().container(Messages.GUI_PRINCIPALS_IA_USERS_HELP_0));
        usersAction.setIconPath(PATH_BUTTONS + "user.png");
        usersAction.setVisible(true);
        metadata.addIndependentAction(usersAction);

        CmsListIndependentAction groupsAction = new CmsListIndependentAction(LIST_IACTION_GROUPS);
        groupsAction.setName(Messages.get().container(Messages.GUI_PRINCIPALS_IA_GROUPS_NAME_0));
        groupsAction.setHelpText(Messages.get().container(Messages.GUI_PRINCIPALS_IA_GROUPS_HELP_0));
        groupsAction.setIconPath(PATH_BUTTONS + "group.png");
        groupsAction.setVisible(false);
        metadata.addIndependentAction(groupsAction);

        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_DISPLAY));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        searchAction.setCaseInSensitive(true);
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        try {
            Integer.valueOf(getParamFlags());
        } catch (Throwable e) {
            setParamFlags(null);
        }
    }
}