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

package org.opencms.ui.apps.user;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.sessions.CmsKillSessionDialog;
import org.opencms.ui.apps.user.CmsOuTree.CmsOuTreeType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsUserInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Table for user.<p>
 *
 */
public class CmsUserTable extends Table implements I_CmsFilterableTable {

    /**
     *Entry to addition info dialog.<p>
     */
    class EntryAddInfos implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
            CmsAdditionalInfosDialog dialog = new CmsAdditionalInfosDialog(
                m_cms,
                new CmsUUID(context.iterator().next()),
                window);
            window.setContent(dialog);
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_ADDITIONAL_INFOS_MENU_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry to delete user.<p>
     */
    class EntryDelete implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            CmsBasicDialog dialog = null;

            dialog = new CmsDeleteMultiplePrincipalDialog(m_cms, context, window);

            window.setContent(dialog);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DELETE_0));
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return onlyVisibleForOU(context);
        }

    }

    /**
     * Menu entry to edit user.<p>
     */
    class EntryEdit implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USER_0));
            window.setContent(new CmsUserEditDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return onlyVisibleForOU(new CmsUUID(context.iterator().next()));
        }

    }

    /**
     * Menu entry to edit groups.<p>
     */
    class EntryEditGroup implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            window.setContent(new CmsUserEditGroupDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USERGROUP_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return onlyVisibleForOU(new CmsUUID(context.iterator().next()));
        }

    }

    /**
     * Menu entry for editing roles of user.<p>
     */
    class EntryEditRole implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            window.setContent(new CmsUserEditRoleDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USERROLES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return onlyVisibleForOU(new CmsUUID(context.iterator().next()));
        }

    }

    /**
     *Entry to info dialog.<p>
     */
    class EntryInfo implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            openInfoDialog(new CmsUUID(context.iterator().next()));
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles#getStyles()
         */
        public String getStyles() {

            return ValoTheme.LABEL_BOLD;
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     *Entry to kill Session.<p>
     */
    class EntryKillSession implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            final Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_DESTROY_SESSION_0));
            Set<String> sessionIds = new HashSet<String>();
            for (CmsSessionInfo info : OpenCms.getSessionManager().getSessionInfos(
                new CmsUUID(context.iterator().next()))) {
                sessionIds.add(info.getSessionId().getStringValue());
            }
            CmsKillSessionDialog dialog = new CmsKillSessionDialog(sessionIds, new Runnable() {

                public void run() {

                    window.close();
                }
            });
            window.setContent(dialog);
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_DESTROY_SESSION_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            if (!OpenCms.getSessionManager().getSessionInfos(new CmsUUID(context.iterator().next())).isEmpty()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
        }

    }

    /**
     * Menu entry to remove user from group.<p>
     */
    class EntryRemoveFromGroup implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> context) {

            try {
                final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                final String user = m_cms.readUser(new CmsUUID(context.iterator().next())).getName();
                String confirmText = "";
                String caption = "";
                Runnable okRunnable = null;
                if (m_type.equals(CmsOuTreeType.GROUP)) {
                    caption = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_GROUP_0);
                    confirmText = CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_GROUP_CONFIRM_2,
                        user,
                        m_group);
                    okRunnable = new Runnable() {

                        public void run() {

                            try {
                                m_cms.removeUserFromGroup(user, m_group);
                            } catch (CmsException e) {
                                //
                            }
                            window.close();
                            A_CmsUI.get().reload();
                        }
                    };
                }
                if (m_type.equals(CmsOuTreeType.ROLE)) {
                    caption = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_ROLE_0);
                    confirmText = CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_ROLE_CONFIRM_2,
                        user,
                        m_group);
                    okRunnable = new Runnable() {

                        public void run() {

                            try {
                                OpenCms.getRoleManager().removeUserFromRole(
                                    m_cms,
                                    CmsRole.valueOfRoleName(m_group),
                                    user);

                            } catch (CmsException e) {
                                //
                            }
                            window.close();
                            A_CmsUI.get().reload();
                        }
                    };
                }

                window.setCaption(caption);
                window.setContent(new CmsConfirmationDialog(confirmText, okRunnable, new Runnable() {

                    public void run() {

                        window.close();
                    }
                }));

                A_CmsUI.get().addWindow(window);
            } catch (CmsException e) {
                //
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            if (m_type.equals(CmsOuTreeType.GROUP)) {
                return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_GROUP_0);
            }
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_REMOVE_USER_FROM_ROLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            if (m_group == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            try {
                CmsUser user = m_cms.readUser(context.iterator().next());
                return ((Boolean)(getItem(user).getItemProperty(TableProperty.INDIRECT).getValue())).booleanValue()
                ? CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE
                : CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } catch (CmsException e) {
                //
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry for show resources of user.<p>
     */
    class EntryShowResources implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_SHOW_RESOURCES_0));
            window.setContent(new CmsShowResourcesDialog(context.iterator().next(), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_SHOW_RESOURCES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     *Entry to switch user.<p>
     */
    class EntrySwitchUser implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            final Window window = CmsBasicDialog.prepareWindow();
            final CmsUUID userID = new CmsUUID(context.iterator().next());
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_SWITCH_USER_0));
            CmsConfirmationDialog dialog = new CmsConfirmationDialog(
                CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_SWITCH_USER_CONFIRM_0),
                new Runnable() {

                    public void run() {

                        try {
                            String path = OpenCms.getSessionManager().switchUser(
                                A_CmsUI.getCmsObject(),
                                CmsVaadinUtils.getRequest(),
                                m_cms.readUser(userID));
                            if (path == null) {
                                path = CmsVaadinUtils.getWorkplaceLink() + "?_lrid=" + (new Date()).getTime();
                            }
                            A_CmsUI.get().getPage().setLocation(path);
                            window.close();
                        } catch (CmsException e) {
                            //
                        }
                    }
                },
                new Runnable() {

                    public void run() {

                        window.close();
                    }
                });
            window.setContent(dialog);
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_SWITCH_USER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**Table properties. */
    enum TableProperty {
        /**Icon. */
        Icon(null, Resource.class, new CmsCssIcon("oc-icon-24-user")),
        /**Full system Name. */
        SystemName("", String.class, ""),
        /**Name. */
        Name(Messages.GUI_USERMANAGEMENT_USER_USER_0, String.class, ""),
        /**Description. */
        FullName(Messages.GUI_USERMANAGEMENT_USER_NAME_0, String.class, ""),
        /**OU. */
        OU(Messages.GUI_USERMANAGEMENT_USER_OU_0, String.class, ""),
        /**Last login. */
        LastLogin(Messages.GUI_USERMANAGEMENT_USER_LAST_LOGIN_0, Long.class, new Long(0L)),
        /**IsIndirect?. */
        INDIRECT("", Boolean.class, new Boolean(false)),
        /**From Other ou?. */
        FROMOTHEROU("", Boolean.class, new Boolean(false)),
        /**Is the user disabled? */
        DISABLED("", Boolean.class, new Boolean(false)),
        /**Is the user new? */
        NEWUSER("", Boolean.class, new Boolean(false)),
        /**Status. */
        STATUS("", Integer.class, new Integer(0));

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.<p>
         *
         * @param name Name
         * @param type type
         * @param defaultValue value
         */
        TableProperty(String name, Class<?> type, Object defaultValue) {

            m_headerMessage = name;
            m_type = type;
            m_defaultValue = defaultValue;
        }

        /**
         * The default value.<p>
         *
         * @return the default value object
         */
        Object getDefault() {

            return m_defaultValue;
        }

        /**
         * Gets the name of the property.<p>
         *
         * @return a name
         */
        String getName() {

            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_headerMessage)) {
                return CmsVaadinUtils.getMessageText(m_headerMessage);
            }
            return "";
        }

        /**
         * Gets the type of property.<p>
         *
         * @return the type
         */
        Class<?> getType() {

            return m_type;
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = 7863356514060544048L;

    /** Log instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsUserTable.class);

    /**Map for password status of user. */
    protected static final Map<CmsUUID, Boolean> USER_PASSWORD_STATUS = new HashMap<CmsUUID, Boolean>();

    /**Indexed container. */
    private IndexedContainer m_container;

    /**CmsObject.*/
    CmsObject m_cms;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Name of group to show user for, or null. */
    protected String m_group;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**The app.*/
    private CmsAccountsApp m_app;

    /**Type to be shown. */
    protected CmsOuTreeType m_type;

    /**List of user. */
    private List<CmsUser> m_user;

    /**flag indicates if all indirect items (for roles) are loaded. */
    private boolean m_fullLoaded;

    /**List of indirect user. */
    List<CmsUser> m_indirects;

    /**vaadin component.*/
    private VerticalLayout m_emptyLayout;

    /**OU. */
    private String m_ou;

    /**Black list of user from higher OU than current user.*/
    private HashSet<CmsUser> m_blackList = new HashSet<CmsUser>();

    /**Set of user with Flag to reset password. */
    private Set<CmsUUID> m_checkedUserPasswordReset;

    /**
     * public constructor.<p>
     *
     * @param ou name
     * @param app the app
     */
    public CmsUserTable(String ou, CmsAccountsApp app) {

        m_ou = ou;
        m_app = app;
        try {
            m_cms = getCmsObject();
            m_type = CmsOuTreeType.USER;
            m_user = OpenCms.getOrgUnitManager().getUsersWithoutAdditionalInfo(m_cms, ou, false);
            m_indirects = Collections.emptyList();
            init(false);
            m_fullLoaded = true;
        } catch (CmsException e) {
            LOG.error("Unable to read user.", e);
        }
    }

    /**
     * puiblic constructor.<p>
     *
     * @param ou ou name
     * @param groupID id of group
     * @param cmsOuTreeType type to be shown
     * @param showAll show all user including inherited?
     * @param app the app
     */
    public CmsUserTable(String ou, CmsUUID groupID, CmsOuTreeType cmsOuTreeType, boolean showAll, CmsAccountsApp app) {

        m_ou = ou;
        m_app = app;

        try {
            m_cms = getCmsObject();
            m_type = cmsOuTreeType;

            m_indirects = new ArrayList<CmsUser>();
            if (m_type.equals(CmsOuTreeType.GROUP)) {
                m_group = m_cms.readGroup(groupID).getName();
                m_user = m_cms.getUsersOfGroup(m_group, true);
                m_fullLoaded = true;
            }
            if (m_type.equals(CmsOuTreeType.ROLE)) {
                m_group = CmsRole.valueOfId(groupID).forOrgUnit(ou).getFqn();
                List<CmsUser> directs = OpenCms.getRoleManager().getUsersOfRole(
                    m_cms,
                    CmsRole.valueOfId(groupID).forOrgUnit(ou),
                    true,
                    true);
                if (showAll) {
                    setAllUser(directs);
                } else {
                    m_user = directs;
                }
            }

            init(showAll);
        } catch (CmsException e) {
            LOG.error("Unable to read user", e);
        }
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#filter(java.lang.String)
     */
    public void filter(String data) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(data)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableProperty.SystemName, data, true, false),
                    new SimpleStringFilter(TableProperty.FullName, data, true, false)));
        }

    }

    /**
     * Layout which gets displayed if table is empty.
     *
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#getEmptyLayout()
     */
    public VerticalLayout getEmptyLayout() {

        m_emptyLayout = CmsVaadinUtils.getInfoLayout(CmsOuTreeType.USER.getEmptyMessageKey());
        setVisible(size() > 0);
        m_emptyLayout.setVisible(size() == 0);
        return m_emptyLayout;
    }

    /**
     * Toggles the table.<p>
     *
     * @param pressed boolean
     */
    public void toggle(boolean pressed) {

        try {
            if (pressed & !m_fullLoaded) {
                setAllUser(m_user);
            }
        } catch (CmsException e) {
            m_fullLoaded = false;
            LOG.error("Error loading user", e);
        }
        fillContainer(pressed);
    }

    /**
     * Fills the container.<p>
     *
     * @param showIndirect true-> show all user, false -> only direct user
     */
    protected void fillContainer(boolean showIndirect) {

        m_container.removeAllItems();
        m_checkedUserPasswordReset = new HashSet<CmsUUID>();
        for (CmsUser user : m_user) {
            if (!m_indirects.contains(user)) {
                addUserToContainer(m_container, user);
            }
        }
        if (showIndirect) {
            for (CmsUser user : m_indirects) {
                addUserToContainer(m_container, user);
            }
        }
        setVisibilities();
    }

    /**
     * Visibility which is only active if user is in right ou.<p>
     *
     * @param userId to be checked
     * @return CmsMenuItemVisibilityMode
     */
    protected CmsMenuItemVisibilityMode onlyVisibleForOU(CmsUUID userId) {

        try {
            if (m_app.isOUManagable(m_cms.readUser(userId).getOuFqn())) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
        } catch (CmsException e) {
            //
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
    }

    /**
     * Checks if all selected items are editable.<p>
     *
     * @param context items
     * @return CmsMenuItemVisibilityMode
     */
    protected CmsMenuItemVisibilityMode onlyVisibleForOU(Set<String> context) {

        for (String id : context) {
            if (onlyVisibleForOU(new CmsUUID(id)).equals(CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
        }
        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * Opens the user info dialog.<p>
     *
     * @param id of user
     */
    protected void openInfoDialog(CmsUUID id) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.content);
        CmsBasicDialog dialog = new CmsBasicDialog();
        dialog.setContent(new CmsUserInfo(id));
        Button cancelButton = new Button(CmsVaadinUtils.messageClose());
        cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_TITLE_0));
        dialog.addButton(cancelButton);
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Checks value of table and sets it new if needed:<p>
     * if multiselect: new itemId is in current Value? -> no change of value<p>
     * no multiselect and multiselect, but new item not selected before: set value to new item<p>
     *
     * @param itemId if of clicked item
     */
    void changeValueIfNotMultiSelect(Object itemId) {

        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>)getValue();
        if (value == null) {
            select(itemId);
        } else if (!value.contains(itemId)) {
            setValue(null);
            select(itemId);
        }
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EntryInfo());
            m_menuEntries.add(new EntryEdit());
            m_menuEntries.add(new EntryEditRole());
            m_menuEntries.add(new EntryEditGroup());
            m_menuEntries.add(new EntryAddInfos());
            m_menuEntries.add(new EntryShowResources());
            m_menuEntries.add(new EntrySwitchUser());
            m_menuEntries.add(new EntryRemoveFromGroup());
            m_menuEntries.add(new EntryDelete());
            m_menuEntries.add(new EntryKillSession());
        }
        return m_menuEntries;
    }

    /**
     * Returns status message.
     *
     * @param user CmsUser
     * @param disabled boolean
     * @param newUser boolean
     * @return String
     */
    String getStatus(CmsUser user, boolean disabled, boolean newUser) {

        if (disabled) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DISABLED_0);
        }
        if (newUser) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_INACTIVE_0);
        }
        if (isUserPasswordReset(user)) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_PASSWORT_RESET_0);
        }
        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_ACTIVE_0);
    }

    /**
     * Returns status help message.
     *
     * @param user CmsUser
     * @param disabled boolean
     * @param newUser boolean
     * @return String
     */
    String getStatusHelp(CmsUser user, boolean disabled, boolean newUser) {

        if (disabled) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DISABLED_HELP_0);
        }
        if (newUser) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_INACTIVE_HELP_0);
        }
        if (isUserPasswordReset(user)) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_PASSWORT_RESET_HELP_0);
        }
        long lastLogin = user.getLastlogin();
        return CmsVaadinUtils.getMessageText(
            Messages.GUI_USERMANAGEMENT_USER_ACTIVE_HELP_1,
            CmsDateUtil.getDateTime(new Date(lastLogin), DateFormat.SHORT, A_CmsUI.get().getLocale()));
    }

    /**
     * Integer value for status (allows to sort column).
     *
     * @param disabled boolean
     * @param newUser boolean
     * @return Integer
     */
    Integer getStatusInt(boolean disabled, boolean newUser) {

        if (disabled) {
            return new Integer(2);
        }
        if (newUser) {
            return new Integer(1);
        }
        return new Integer(0);
    }

    /**
     * Is the user password reset?
     *
     * @param user User to check
     * @return boolean
     */
    boolean isUserPasswordReset(CmsUser user) {

        if (USER_PASSWORD_STATUS.containsKey(user.getId())) { //Check if user was checked before
            if (!USER_PASSWORD_STATUS.get(user.getId()).booleanValue()) { // was false before, false->true is never done without changing map
                return false;
            }
            if (m_checkedUserPasswordReset.contains(user.getId())) { //was true before, true->false happens when user resets password. Only check one time per table load.
                return true; //Set gets flushed on reloading table
            }
        }
        CmsUser currentUser = user;
        if (user.getAdditionalInfo().size() < 3) {

            try {
                currentUser = m_cms.readUser(user.getId());
            } catch (CmsException e) {
                LOG.error("Can not read user", e);
            }
        }
        if (currentUser.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET) != null) {
            USER_PASSWORD_STATUS.put(currentUser.getId(), new Boolean(true));
            m_checkedUserPasswordReset.add(currentUser.getId());
            return true;
        }
        USER_PASSWORD_STATUS.put(currentUser.getId(), new Boolean(false));
        return false;
    }

    /**
     * Adds given user to given IndexedContainer.<p>
     *
     * @param container to add the user to
     * @param user to add
     */
    private void addUserToContainer(IndexedContainer container, CmsUser user) {

        if (m_blackList.contains(user)) {
            return;
        }
        Item item = container.addItem(user);
        item.getItemProperty(TableProperty.Name).setValue(user.getSimpleName());
        item.getItemProperty(TableProperty.FullName).setValue(user.getFullName());
        item.getItemProperty(TableProperty.SystemName).setValue(user.getName());
        boolean disabled = !user.isEnabled();
        item.getItemProperty(TableProperty.DISABLED).setValue(new Boolean(disabled));
        boolean newUser = user.getLastlogin() == 0L;
        item.getItemProperty(TableProperty.NEWUSER).setValue(new Boolean(newUser));
        try {
            item.getItemProperty(TableProperty.OU).setValue(
                OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, user.getOuFqn()).getDisplayName(
                    A_CmsUI.get().getLocale()));
        } catch (CmsException e) {
            LOG.error("Can't read OU", e);
        }
        item.getItemProperty(TableProperty.LastLogin).setValue(new Long(user.getLastlogin()));
        item.getItemProperty(TableProperty.INDIRECT).setValue(new Boolean(m_indirects.contains(user)));
        item.getItemProperty(TableProperty.FROMOTHEROU).setValue(new Boolean(!user.getOuFqn().equals(m_ou)));
        item.getItemProperty(TableProperty.STATUS).setValue(getStatusInt(disabled, newUser));
    }

    /**
     * Gets list of indirect users to show.<p>
     *
     * @param allUser all users
     * @param directUser direct user
     * @return indirect user
     */
    private List<CmsUser> getAllowedIndirects(List<CmsUser> allUser, List<CmsUser> directUser) {

        List<CmsUser> res = new ArrayList<CmsUser>();
        for (CmsUser u : allUser) {
            if (!directUser.contains(u)) {
                res.add(u);
            }
        }

        return res;
    }

    /**
     * Gets CmsObject.<p>
     *
     * @return cmsobject
     */
    private CmsObject getCmsObject() {

        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            //m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            cms = A_CmsUI.getCmsObject();
        }
        return cms;
    }

    /**
     * initializes table.
     * @param showAll boolean
     */
    private void init(boolean showAll) {

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new IndexedContainer();

        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefault());
            setColumnHeader(prop, prop.getName());
        }
        setContainerDataSource(m_container);
        setItemIconPropertyId(TableProperty.Icon);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);
        setColumnWidth(TableProperty.STATUS, 100);
        setSelectable(true);
        setMultiSelect(true);

        setVisibleColumns(TableProperty.Name, TableProperty.OU);

        fillContainer(showAll);

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            @SuppressWarnings("unchecked")
            public void itemClick(ItemClickEvent event) {

                changeValueIfNotMultiSelect(event.getItemId());

                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    Set<String> userIds = new HashSet<String>();
                    for (CmsUser user : (Set<CmsUser>)getValue()) {
                        userIds.add(user.getId().getStringValue());
                    }

                    m_menu.setEntries(getMenuEntries(), userIds);
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsUserTable.this);
                } else if (event.getButton().equals(MouseButton.LEFT)
                    && TableProperty.Name.equals(event.getPropertyId())) {
                    CmsUser user = ((Set<CmsUser>)getValue()).iterator().next();
                    openInfoDialog(user.getId());
                }

            }

        });
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 4685652851810828147L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableProperty.STATUS.equals(propertyId)) {
                    return getStatusStyleForItem(source.getItem(itemId), (CmsUser)itemId);

                }
                String css = " ";

                if (((Boolean)(source.getItem(itemId).getItemProperty(
                    TableProperty.FROMOTHEROU).getValue())).booleanValue()) {
                    css += OpenCmsTheme.EXPIRED;
                }

                if (TableProperty.Name.equals(propertyId)) {
                    css += " " + OpenCmsTheme.HOVER_COLUMN;
                }

                if (((Boolean)source.getItem(itemId).getItemProperty(
                    TableProperty.INDIRECT).getValue()).booleanValue()) {
                    return css + " " + OpenCmsTheme.TABLE_CELL_DISABLED;
                }
                return css.length() == 1 ? null : css;
            }

            private String getStatusStyleForItem(Item item, CmsUser user) {

                if (((Boolean)item.getItemProperty(TableProperty.DISABLED).getValue()).booleanValue()) {
                    return OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
                }

                if (((Boolean)item.getItemProperty(TableProperty.NEWUSER).getValue()).booleanValue()) {
                    return OpenCmsTheme.TABLE_COLUMN_BOX_BLUE;
                }

                if (isUserPasswordReset(user)) {
                    return OpenCmsTheme.TABLE_COLUMN_BOX_ORANGE;
                }

                return OpenCmsTheme.TABLE_COLUMN_BOX_GREEN;
            }
        });
        addGeneratedColumn(TableProperty.STATUS, new ColumnGenerator() {

            private static final long serialVersionUID = -2144476865774782965L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                return getStatus(
                    (CmsUser)itemId,
                    ((Boolean)source.getItem(itemId).getItemProperty(TableProperty.DISABLED).getValue()).booleanValue(),
                    ((Boolean)source.getItem(itemId).getItemProperty(TableProperty.NEWUSER).getValue()).booleanValue());

            }

        });
        addGeneratedColumn(TableProperty.LastLogin, new ColumnGenerator() {

            private static final long serialVersionUID = -6781906011584975559L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                long lastLogin = ((Long)source.getItem(itemId).getItemProperty(
                    TableProperty.LastLogin).getValue()).longValue();
                return lastLogin == 0L
                ? CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_NEVER_LOGGED_IN_0)
                : CmsDateUtil.getDateTime(new Date(lastLogin), DateFormat.SHORT, A_CmsUI.get().getLocale());

            }

        });

        setItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 7367011213487089661L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                if (TableProperty.STATUS.equals(propertyId)) {

                    return getStatusHelp(
                        (CmsUser)itemId,
                        ((Boolean)((Table)source).getItem(itemId).getItemProperty(
                            TableProperty.DISABLED).getValue()).booleanValue(),
                        ((Boolean)((Table)source).getItem(itemId).getItemProperty(
                            TableProperty.NEWUSER).getValue()).booleanValue());
                }
                return null;
            }
        });
        setVisibleColumns(
            TableProperty.STATUS,
            TableProperty.Name,
            TableProperty.FullName,
            TableProperty.OU,
            TableProperty.LastLogin);
    }

    /**
     * Checks is it is allown for the current user to see given user.<p>
     *
     * @param user to be checked
     * @return boolean
     * @throws CmsException exception
     */
    private boolean isAllowedUser(CmsUser user) throws CmsException {

        if (user.getOuFqn().startsWith(m_cms.getRequestContext().getOuFqn())) {
            return true;
        }
        return OpenCms.getRoleManager().getRolesOfUser(m_cms, user.getName(), m_ou, true, true, false).size() > 0;
    }

    /**
     * Sets all user, including indirect user for roles.<p>
     *
     * @param directs direct user
     * @throws CmsException exception
     */
    private void setAllUser(List<CmsUser> directs) throws CmsException {

        m_fullLoaded = true;
        m_user = OpenCms.getRoleManager().getUsersOfRole(
            m_cms,
            CmsRole.valueOfRoleName(m_group).forOrgUnit(m_ou),
            true,
            false);
        Iterator<CmsUser> it = m_user.iterator();
        while (it.hasNext()) {
            CmsUser u = it.next();
            if (!isAllowedUser(u)) {
                m_blackList.add(u);
            }
        }
        m_indirects.addAll(getAllowedIndirects(m_user, directs));
    }

    /**
     * Sets the visibility of table and empty info panel.<p>     *
     */
    private void setVisibilities() {

        setVisible(size() > 0);
        if (m_emptyLayout != null) {
            m_emptyLayout.setVisible(size() == 0);
        }
    }
}
