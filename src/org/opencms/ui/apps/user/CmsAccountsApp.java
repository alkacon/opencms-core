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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
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
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * App for the OU Management.<p>
 */
public class CmsAccountsApp extends A_CmsWorkplaceApp {

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
                type = CmsOuTreeType.OU;
                if (fields.get(0).equals(TYPE_GRUOP)) {
                    type = CmsOuTreeType.GROUP;
                }
                if (fields.get(0).equals(TYPE_USER)) {
                    type = CmsOuTreeType.USER;
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

            String typeString = TYPE_OU;
            if (m_type.equals(CmsOuTreeType.GROUP)) {
                typeString = TYPE_GRUOP;
            }
            if (m_type.equals(CmsOuTreeType.USER)) {
                typeString = TYPE_USER;
            }
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

    /**Type of element. */
    CmsOuTreeType m_type;

    /**vaadin component. */
    Button m_settingsButton;

    /**ou path. */
    private String m_ou = "";

    /**Base ou. */
    private String m_baseOU = "";

    /**vaadin component.*/
    private Button m_newButton;

    /**vaadin component. */
    private CmsInfoButton m_infoButton;

    /**
     * constructor.<p>
     */
    public CmsAccountsApp() {
        super();
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_baseOU = m_cms.getRequestContext().getCurrentUser().getOuFqn();
            m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            //
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
        return new CmsResourceInfo(
            principal.getName(),
            principal.getDescription(A_CmsUI.get().getLocale()),
            new CmsCssIcon(OpenCmsTheme.ICON_GROUP));
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
        m_infoButton.replaceData(
            Collections.singletonMap(
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                String.valueOf(((Table)m_table).size())));
        if (comp != null) {
            comp.setSizeFull();
            m_splitScreen.setSecondComponent(comp);

        } else {
            m_splitScreen.setSecondComponent(new Label("Malformed path, tool not availabel for path: " + state));
        }
        m_splitScreen.setSizeFull();
        updateSubNav(getSubNavEntries(state));
        updateBreadCrumb(getBreadCrumbForState(state));
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
                        crumbs.put("", m_cms.readGroup(bean.getGroupID()).getSimpleName());
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

        if (m_filter == null) {
            m_newButton = CmsToolBar.createButton(
                FontOpenCms.WAND,
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0));
            m_newButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    openNewDialog();
                }
            });
            m_newButton.setImmediate(true);
            m_infoButton = new CmsInfoButton();
            m_settingsButton = CmsToolBar.createButton(
                FontOpenCms.SETTINGS,
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0));
            m_settingsButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    openEditDialog();
                }
            });

            m_uiContext.addToolbarButton(m_newButton);
            m_uiContext.addToolbarButton(m_settingsButton);
            m_uiContext.addToolbarButton(m_infoButton);
            m_filter = getOUComboBox();
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

                    if (m_type != null) {
                        update((String)event.getProperty().getValue(), CmsOuTreeType.OU, null);
                    } else {
                        System.out.println("Null");
                    }
                }
            });

        }
        CmsStateBean stateBean = CmsStateBean.parseState(state, m_baseOU);
        m_type = stateBean.getType();
        m_ou = stateBean.getPath();
        m_newButton.setVisible(stateBean.getGroupID() == null);
        m_infoButton.setVisible(!stateBean.getType().equals(CmsOuTreeType.OU));
        m_settingsButton.setVisible(stateBean.getType().equals(CmsOuTreeType.OU) && !m_ou.isEmpty());
        m_ouTree.openPath(stateBean.getPath(), stateBean.getType(), stateBean.getGroupID());

        if (stateBean.getType().equals(CmsOuTreeType.OU)) {
            m_table = new CmsOUTable(stateBean.getPath(), this);
            return m_table;
        }
        if (stateBean.getType().equals(CmsOuTreeType.USER)) {
            m_table = new CmsUserTable(stateBean.getPath());
            return m_table;
        }
        if (stateBean.getType().equals(CmsOuTreeType.GROUP)) {
            if (stateBean.getGroupID() == null) {
                m_table = new CmsGroupTable(stateBean.getPath(), this);
                return m_table;
            }
            m_table = new CmsUserTable(stateBean.getPath(), stateBean.getGroupID());
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

        final Window window = CmsBasicDialog.prepareWindow();
        CmsBasicDialog dialog = new CmsOUEditDialog(m_cms, window, m_ou);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_OU_0));
        if (CmsOuTreeType.GROUP.equals(m_type)) {
            dialog = new CmsGroupEditDialog(m_cms, window, m_ou);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_0));
        }
        if (CmsOuTreeType.USER.equals(m_type)) {
            dialog = new CmsUserEditDialog(m_cms, window, m_ou);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_0));
        }
        window.setContent(dialog);
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
     * Opens a dialog to edit current OU.<p>
     */
    void openEditDialog() {

        Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_EDIT_WINDOW_CAPTION_0));
        window.setContent(new CmsOUEditDialog(m_cms, m_ou, window));

        A_CmsUI.get().addWindow(window);

    }

    /**
     * Creates the ComboBox for OU selection.<p>
     *
     * @return ComboBox
     */
    private ComboBox getOUComboBox() {

        ComboBox combo = null;
        try {
            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty("desc", String.class, "");
            CmsOrganizationalUnit root = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_baseOU);
            Item itemRoot = container.addItem(root.getName());
            itemRoot.getItemProperty("desc").setValue(root.getDisplayName(A_CmsUI.get().getLocale()));
            for (CmsOrganizationalUnit ou : OpenCms.getOrgUnitManager().getOrganizationalUnits(m_cms, m_baseOU, true)) {
                Item item = container.addItem(ou.getName());
                item.getItemProperty("desc").setValue(ou.getDisplayName(A_CmsUI.get().getLocale()));
            }
            combo = new ComboBox(null, container);
            combo.setTextInputAllowed(true);
            combo.setNullSelectionAllowed(false);
            combo.setWidth("200px");
            combo.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_CLICK_TO_EDIT_0));
            combo.setItemCaptionPropertyId("desc");

            combo.setFilteringMode(FilteringMode.CONTAINS);

            combo.select(m_baseOU);

        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
        return combo;
    }

}
