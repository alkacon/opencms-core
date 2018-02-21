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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.CmsOuTree.CmsOuTreeType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelectDialog;
import org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * App for the OU Management.<p>
 */
public class CmsAccountsApp extends A_CmsWorkplaceApp implements I_CmsPrincipalSelect {

    /**
     * Bean for the state of the app.<p>
     */
    static class CmsStateBean {

        /**Type group. */
        protected static String TYPE_GRUOP = "g";

        /**Type user. */
        protected static String TYPE_USER = "u";

        /**Type OU. */
        protected static String TYPE_OU = "o";

        /**Type Role. */
        protected static String TYPE_ROLE = "r";

        /**State seperator. */
        protected static String STATE_SEPERATOR = "!!";

        /**ou path. */
        private String m_path = "";

        /**type of element to be openend. */
        private CmsOuTreeType m_type;

        /**Group id to be opended (or null). */
        private CmsUUID m_groupID;

        /**
         * public constructor.<p>
         *
         * @param path ou path
         * @param type type to be opened
         * @param groupID groupid
         */
        public CmsStateBean(String path, CmsOuTreeType type, CmsUUID groupID) {

            m_path = path;
            m_type = type;
            m_groupID = groupID;
        }

        /**
         * Parses a given state string to state bean.<p>
         *
         * @param state to be read
         * @param baseOU baseOu
         * @return CmsStateBean
         */
        public static CmsStateBean parseState(String state, String baseOU) {

            String path = baseOU;
            CmsOuTreeType type = CmsOuTreeType.OU;
            CmsUUID groupId = null;
            List<String> fields = CmsStringUtil.splitAsList(state, STATE_SEPERATOR);
            if (!fields.isEmpty()) {
                if (fields.size() > 1) {
                    path = fields.get(1);
                    //Make sure to only show OUs which are under baseOU
                    if (path.equals("") | !path.startsWith(baseOU)) {
                        path = baseOU;
                    }
                }
                for (CmsOuTreeType ty : CmsOuTreeType.values()) {
                    if (fields.get(0).equals(ty.getID())) {
                        type = ty;
                    }
                }
                if (fields.size() > 2) {
                    groupId = new CmsUUID(fields.get(2));
                }
            }
            return new CmsStateBean(path, type, groupId);
        }

        /**
         * Gets group id.<p>
         *
         * @return group id
         */
        public CmsUUID getGroupID() {

            return m_groupID;
        }

        /**
         * Gets the ou path.<p>
         *
         * @return ou path
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Gets the state string of the current bean.<p>
         *
         * @return state string
         */
        public String getState() {

            String typeString = m_type.getID();
            if (m_groupID != null) {
                return typeString + STATE_SEPERATOR + m_path + STATE_SEPERATOR + m_groupID.getStringValue();
            }
            return typeString + STATE_SEPERATOR + m_path;
        }

        /**
         * Gets type of element to open.<p>
         *
         * @return type of element
         */
        public CmsOuTreeType getType() {

            return m_type;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAccountsApp.class);

    /** The folder tree. */
    private CmsOuTree m_ouTree;

    /**vaadin component.*/
    private HorizontalSplitPanel m_splitScreen;

    /**CmsObject. */
    private CmsObject m_cms;

    /** The file table filter input. */
    private TextField m_filterTable;

    /**vaadin component.*/
    private ComboBox m_filter;

    /**vaadin component.*/
    private I_CmsFilterableTable m_table;

    /**Base ou. */
    private String m_baseOU = "";

    /**Don't handle change event flag.*/
    boolean m_doNotChange;

    /**vaadin component.*/
    private Button m_newButton;

    /**vaadin component. */
    private CmsInfoButton m_infoButton;

    /**Button to add an element. */
    private Button m_addElementButton;

    /**vaadin component.*/
    private Button m_toggleButton;

    /**State bean. */
    CmsStateBean m_stateBean;

    /**Class to handle visible and managable ous. */
    private CmsOUHandler m_ouHandler;

    /**
     * constructor.<p>
     */
    public CmsAccountsApp() {

        super();
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_cms.getRequestContext().setSiteRoot("");
            m_ouHandler = new CmsOUHandler(m_cms);
            m_baseOU = m_ouHandler.getBaseOU();

        } catch (

        CmsException e) {
            //}
        }
        m_rootLayout.setMainHeightFull(true);
        m_splitScreen = new HorizontalSplitPanel();
        m_splitScreen.setSizeFull();
        m_splitScreen.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        m_rootLayout.setMainContent(m_splitScreen);
        m_ouTree = new CmsOuTree(m_cms, this, m_baseOU);
        m_splitScreen.setFirstComponent(m_ouTree);

    }

    /**
     * Creates info panel for principals.<p>
     *
     * @param principal to get info panel for
     * @return CmsResourceInfo
     */
    public static CmsResourceInfo getPrincipalInfo(I_CmsPrincipal principal) {

        if (principal == null) {
            return null;
        }
        if (principal instanceof CmsUser) {
            CmsUser user = (CmsUser)principal;
            CmsUserIconHelper helper = OpenCms.getWorkplaceAppManager().getUserIconHelper();
            return new CmsResourceInfo(
                user.getName(),
                user.getEmail(),
                new ExternalResource(helper.getTinyIconPath(A_CmsUI.getCmsObject(), user)));
        }
        if (principal.getId().equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)) {
            return new CmsResourceInfo(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_ALLOTHERS_0),
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_ALLOTHERS_0),
                new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_ALL));
        }
        if (principal.getId().equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
            return new CmsResourceInfo(
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_OVERWRITEALL_0),
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_OVERWRITEALL_0),
                new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_OVERWRITE));
        }
        CmsRole role = CmsRole.valueOfId(principal.getId());
        if (role != null) {
            return new CmsResourceInfo(
                role.getName(A_CmsUI.get().getLocale()),
                role.getDescription(A_CmsUI.get().getLocale()),
                new CmsCssIcon(OpenCmsTheme.ICON_ROLE));
        }
        return new CmsResourceInfo(
            principal.getName(),
            principal.getDescription(A_CmsUI.get().getLocale()),
            new CmsCssIcon(OpenCmsTheme.ICON_GROUP));
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect#handlePrincipal(org.opencms.security.I_CmsPrincipal)
     */
    public void handlePrincipal(I_CmsPrincipal principal) {

        if (m_stateBean.getType().equals(CmsOuTreeType.GROUP)) {
            try {
                CmsGroup group = m_cms.readGroup(m_stateBean.getGroupID());
                m_cms.addUserToGroup(principal.getName(), group.getName());

            } catch (CmsException e) {
                return;
            }
        }
        if (m_stateBean.getType().equals(CmsOuTreeType.ROLE)) {
            try {
                OpenCms.getRoleManager().addUserToRole(
                    m_cms,
                    CmsRole.valueOfId(m_stateBean.getGroupID()).forOrgUnit(m_stateBean.getPath()),
                    principal.getName());
            } catch (CmsException e) {
                return;
            }
        }
        A_CmsUI.get().reload();
    }

    /**
     * Checks if the given OU is manageable.<p>
     *
     * @param ou to check
     * @return true if user is allowed to manage ou
     */
    public boolean isOUManagable(String ou) {

        return m_ouHandler.isOUManagable(ou);
    }

    /**
     * Checks if given ou is parent of a managable ou.<p>
     *
     * @param name to check
     * @return boolean
     */
    public boolean isParentOfManagableOU(String name) {

        return m_ouHandler.isParentOfManagableOU(name);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#openSubView(java.lang.String, boolean)
     */
    @Override
    public void openSubView(String state, boolean updateState) {

        if (updateState) {
            CmsAppWorkplaceUi.get().changeCurrentAppState(state);
        }

        Component comp = getComponentForState(state);
        if (m_stateBean.getType().equals(CmsOuTreeType.USER)) {
            Map<String, String> dataMap = new LinkedHashMap<String, String>();
            dataMap.put(
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                String.valueOf(((Table)m_table).size()));
            try {
                int count = OpenCms.getOrgUnitManager().getUsersWithoutAdditionalInfo(
                    m_cms,
                    m_stateBean.getPath(),
                    true).size();
                if (count > ((Table)m_table).size()) {
                    dataMap.put(
                        CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_TOT_COUNT_0),
                        String.valueOf(count));
                }
            } catch (CmsException e) {
                //;
            }
            m_infoButton.replaceData(dataMap);
        } else {
            m_infoButton.replaceData(
                Collections.singletonMap(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                    String.valueOf(((Table)m_table).size())));
        }
        if (comp != null) {
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            comp.setSizeFull();
            layout.addComponent(m_table.getEmptyLayout());
            layout.addComponent(m_table);
            m_splitScreen.setSecondComponent(layout);
        } else {
            m_splitScreen.setSecondComponent(new Label("Malformed path, tool not availabel for path: " + state));
        }
        m_splitScreen.setSizeFull();
        updateSubNav(getSubNavEntries(state));
        updateBreadCrumb(getBreadCrumbForState(state));
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect#setType(java.lang.String)
     */
    public void setType(String type) {

        // is never called

    }

    /**
     * Updates the app state.<p>
     *
     * @param ou to be opened
     * @param type to be opened
     * @param groupID to be openend(may be null)
     */
    public void update(String ou, CmsOuTreeType type, CmsUUID groupID) {

        CmsStateBean stateBean = new CmsStateBean(ou, type, groupID);
        openSubView(stateBean.getState(), true);

    }

    /**
     * Filters table.<p>
     *
     * @param text for filter
     */
    protected void filterTable(String text) {

        if (m_table != null) {
            m_table.filter(text);
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        try {
            CmsStateBean bean = CmsStateBean.parseState(state, m_baseOU);
            String[] ouPath = bean.getPath().split("/");
            LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
            if ((bean.getPath().equals(m_baseOU))
                && (CmsOuTreeType.OU.equals(bean.getType()) | (bean.getType() == null))) {
                crumbs.put(
                    "",
                    OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_baseOU).getDisplayName(
                        A_CmsUI.get().getLocale()));
                return crumbs;
            }
            CmsStateBean beanCr = new CmsStateBean(m_baseOU, CmsOuTreeType.OU, null);
            crumbs.put(
                CmsAccountsAppConfiguration.APP_ID + "/" + beanCr.getState(),
                OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_baseOU).getDisplayName(
                    A_CmsUI.get().getLocale()));
            String base = "";
            String pathOfLastElement = "";
            for (String oP : ouPath) {
                if (!oP.isEmpty()) {
                    if ((oP + base).length() > m_baseOU.length()) {
                        if (oP.equals(ouPath[ouPath.length - 1])) {
                            CmsStateBean beanCrumb = new CmsStateBean(base + oP, CmsOuTreeType.OU, null);
                            pathOfLastElement = CmsAccountsAppConfiguration.APP_ID + "/" + beanCrumb.getState();
                            crumbs.put("", oP);
                        } else {
                            CmsStateBean beanCrumb = new CmsStateBean(base + oP, CmsOuTreeType.OU, null);
                            crumbs.put(CmsAccountsAppConfiguration.APP_ID + "/" + beanCrumb.getState(), oP);
                        }
                    }
                    base += oP + "/";
                }
            }
            if (bean.getType() != null) {
                if (!bean.getType().equals(CmsOuTreeType.OU)) {
                    if (!pathOfLastElement.isEmpty()) {
                        crumbs.put(pathOfLastElement, crumbs.get(""));
                        crumbs.remove("");
                    }
                    if (bean.getGroupID() == null) {
                        crumbs.put("", bean.getType().getName());
                    } else {
                        CmsStateBean beanCrumb = new CmsStateBean(bean.getPath(), bean.getType(), null);
                        crumbs.put(
                            CmsAccountsAppConfiguration.APP_ID + "/" + beanCrumb.getState(),
                            beanCrumb.getType().getName());
                        if (bean.getType().equals(CmsOuTreeType.ROLE)) {
                            crumbs.put("", CmsRole.valueOfId(bean.getGroupID()).getName(A_CmsUI.get().getLocale()));
                        } else {
                            crumbs.put("", m_cms.readGroup(bean.getGroupID()).getSimpleName());
                        }
                    }
                }
            }
            return crumbs;
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        m_stateBean = CmsStateBean.parseState(state, m_baseOU);

        if (m_filter == null) {
            iniButtons();
        }

        m_doNotChange = true;

        m_filter.setValue(m_stateBean.getPath());

        m_newButton.setVisible((m_stateBean.getGroupID() == null) & isOUManagable(m_stateBean.getPath()));
        m_toggleButton.setVisible(
            m_stateBean.getType().equals(CmsOuTreeType.ROLE) & (m_stateBean.getGroupID() != null));
        m_infoButton.setVisible(!m_stateBean.getType().equals(CmsOuTreeType.OU));
        m_addElementButton.setVisible(
            (m_stateBean.getType().equals(CmsOuTreeType.GROUP) | m_stateBean.getType().equals(CmsOuTreeType.ROLE))
                & (m_stateBean.getGroupID() != null));
        m_ouTree.openPath(m_stateBean.getPath(), m_stateBean.getType(), m_stateBean.getGroupID());

        m_doNotChange = false;
        if (m_stateBean.getType().equals(CmsOuTreeType.OU)) {
            m_table = new CmsOUTable(m_stateBean.getPath(), this);
            return m_table;
        }
        if (m_stateBean.getType().equals(CmsOuTreeType.USER)) {
            m_table = new CmsUserTable(m_stateBean.getPath(), this);
            return m_table;
        }
        if (m_stateBean.getType().equals(CmsOuTreeType.GROUP)) {
            if (m_stateBean.getGroupID() == null) {
                m_table = new CmsGroupTable(m_stateBean.getPath(), this);
                return m_table;
            }
            m_table = new CmsUserTable(
                m_stateBean.getPath(),
                m_stateBean.getGroupID(),
                m_stateBean.getType(),
                isPressed(m_toggleButton),
                this);
            return m_table;
        }
        if (m_stateBean.getType().equals(CmsOuTreeType.ROLE)) {
            if (m_stateBean.getGroupID() == null) {
                m_table = new CmsRoleTable(this, m_stateBean.getPath());
                return m_table;
            }
            m_table = new CmsUserTable(
                m_stateBean.getPath(),
                m_stateBean.getGroupID(),
                m_stateBean.getType(),
                isPressed(m_toggleButton),
                this);
            return m_table;
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Opens a dialog for a new item (ou, group or user).<p>
     */
    protected void openNewDialog() {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsBasicDialog dialog = new CmsNewElementDialog(m_cms, m_stateBean.getPath(), window);
        window.setContent(dialog);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_ELEMENT_0));
        A_CmsUI.get().addWindow(window);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#updateBreadCrumb(java.util.Map)
     */
    @Override
    protected void updateBreadCrumb(Map<String, String> breadCrumbEntries) {

        LinkedHashMap<String, String> entries = new LinkedHashMap<String, String>();
        if ((breadCrumbEntries != null) && !breadCrumbEntries.isEmpty()) {
            entries.putAll(breadCrumbEntries);
        } else {
            entries.put(
                "",
                OpenCms.getWorkplaceAppManager().getAppConfiguration(m_uiContext.getAppId()).getName(
                    UI.getCurrent().getLocale()));
        }
        setBreadCrumbEntries(entries);
    }

    /**
     * opens a principle select dialog.<p>
     */
    void openAddUserDialog() {

        CmsPrincipalSelectDialog dialog;

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

        dialog = new CmsPrincipalSelectDialog(
            this,
            m_stateBean.getPath(),
            window,
            WidgetType.userwidget,
            true,
            WidgetType.userwidget);

        try {
            dialog.setOuComboBoxEnabled(
                !OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_stateBean.getPath()).hasFlagWebuser()
                    | m_stateBean.getType().equals(CmsOuTreeType.ROLE));
        } catch (CmsException e) {
            LOG.error("Can not read OU.", e);
        }
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_TO_GROUP_0));
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);

    }

    /**
     * toggles the table.<p>
     */
    void toggleTable() {

        CmsUserTable table = (CmsUserTable)m_table;
        table.toggle(!isPressed(m_toggleButton));
        if (isPressed(m_toggleButton)) {
            m_toggleButton.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_toggleButton.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        }
        m_infoButton.replaceData(
            Collections.singletonMap(
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                String.valueOf(table.size())));
    }

    /**
     * Initializes the toolbar buttons.<p>
     */
    private void iniButtons() {

        m_newButton = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_ELEMENT_0));
        m_newButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openNewDialog();
            }
        });
        m_infoButton = new CmsInfoButton();

        m_addElementButton = CmsToolBar.createButton(
            FontAwesome.PLUS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_TO_GROUP_0));
        m_addElementButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1859694635385726953L;

            public void buttonClick(ClickEvent event) {

                openAddUserDialog();

            }
        });

        m_toggleButton = CmsToolBar.createButton(
            FontOpenCms.USERS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ROLES_TOGGLE_0));
        m_toggleButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8265075332953321274L;

            public void buttonClick(ClickEvent event) {

                toggleTable();

            }

        });

        m_uiContext.addToolbarButton(m_newButton);

        m_uiContext.addToolbarButton(m_addElementButton);
        m_uiContext.addToolbarButton(m_infoButton);
        m_uiContext.addToolbarButton(m_toggleButton);
        m_filter = CmsVaadinUtils.getOUComboBox(m_cms, m_baseOU, LOG);
        m_filter.setWidth("379px");
        m_infoLayout.addComponent(m_filter, 0);

        m_filterTable = new TextField();
        m_filterTable.setIcon(FontOpenCms.FILTER);
        m_filterTable.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_filterTable.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        //            m_filterTable.setWidth("200px");
        m_filterTable.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_filterTable);
        m_filter.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                if ((m_stateBean.getType() != null) & !m_doNotChange) {
                    update((String)event.getProperty().getValue(), CmsOuTreeType.OU, null);
                } else {
                    //
                }
            }
        });
        if (!m_ouHandler.isOUManagable(m_stateBean.getPath())) {
            boolean change = m_doNotChange;
            m_doNotChange = false;
            m_filter.select(m_filter.getItemIds().iterator().next());
            m_doNotChange = change;
        }
    }

    /**
     * Checks if a given button is pressed.<p>
     *
     * Check works via style OpenCms.BUTTON_PRESSED.<p>
     *
     * @param button to be checked
     * @return true if button is checked
     */
    private boolean isPressed(Button button) {

        if (button == null) {
            return false;
        }
        List<String> styles = Arrays.asList(button.getStyleName().split(" "));

        return styles.contains(OpenCmsTheme.BUTTON_PRESSED);
    }

}
