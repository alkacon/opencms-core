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
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.CmsGroupTable.TableProperty;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelectDialog;
import org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
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
    class CmsStateBean {

        /**the filter for tables. */
        private String m_filter;

        /**Group id to be opended (or null). */
        private CmsUUID m_groupID;

        /**ou path. */
        private String m_path = "";

        /**type of element to be openend. */
        private I_CmsOuTreeType m_type;

        /**
         * public constructor.<p>
         *
         * @param path ou path
         * @param type type to be opened
         * @param groupID groupid
         * @param filter filter string
         */
        public CmsStateBean(String path, I_CmsOuTreeType type, CmsUUID groupID, String filter) {

            m_path = path.equals("/") ? "" : path;
            m_type = type;
            m_groupID = groupID;
            m_filter = filter;
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

            String typeString = m_type.getId();
            String groupString = "";
            if (m_groupID != null) {
                groupString = m_groupID.getStringValue();
            }
            return typeString + STATE_SEPERATOR + m_path + STATE_SEPERATOR + groupString + STATE_SEPERATOR + m_filter;

        }

        /**
         * Gets the filter string for the table.<p>
         *
         * @return the table filter
         */
        public String getTableFilter() {

            return m_filter;
        }

        /**
         * Gets type of element to open.<p>
         *
         * @return type of element
         */
        public I_CmsOuTreeType getType() {

            return m_type;
        }
    }

    /**State seperator. */
    public static String STATE_SEPERATOR = A_CmsWorkplaceApp.PARAM_SEPARATOR;

    /** Default tree type provider. */
    private static final CmsDefaultTreeTypeProvider DEFAULT_TREETYPES = new CmsDefaultTreeTypeProvider();

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAccountsApp.class);

    /**Button to add an element. */
    protected Button m_addElementButton;

    /**CmsObject. */
    protected CmsObject m_cms;

    /** Toolbar button for CSV import/export in OUs. */
    protected Button m_importExport;

    /**vaadin component. */
    protected CmsInfoButton m_infoButton;

    /**vaadin component.*/
    protected Button m_newButton;

    /**vaadin component.*/
    protected Button m_toggleButtonGroups;

    /**vaadin component.*/
    protected Button m_toggleButtonRole;

    /**vaadin component.*/
    protected Button m_toggleButtonUser;

    /**Don't handle change event flag.*/
    boolean m_doNotChange;

    /**State bean. */
    CmsStateBean m_stateBean;

    /**Base ou. */
    private String m_baseOU = "";

    /**vaadin component.*/
    private ComboBox m_filter;

    /** The file table filter input. */
    private TextField m_filterTable;

    /**Class to handle visible and managable ous. */
    private CmsOUHandler m_ouHandler;

    /** The folder tree. */
    private CmsOuTree m_ouTree;

    /** Map for the cached password reset states. */
    private Map<CmsUUID, Boolean> m_passwordResetStateCache = new ConcurrentHashMap<>();

    /**vaadin component.*/
    private HorizontalSplitPanel m_splitScreen;

    /**vaadin component.*/
    private I_CmsFilterableTable m_table;

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
     * Creates info panel for OUs.<p>
     *
     *
     * @param ou to get panel for
     * @return CmsResourceInfo
     */
    public static CmsResourceInfo getOUInfo(CmsOrganizationalUnit ou) {

        String style = OpenCmsTheme.ICON_OU;
        if (ou.hasFlagWebuser()) {
            style = OpenCmsTheme.ICON_OU_WEB;
        }
        CmsCssIcon image = new CmsCssIcon(style);
        return new CmsResourceInfo(
            ou.getDisplayName(A_CmsUI.get().getLocale()),
            ou.getDescription(A_CmsUI.get().getLocale()),
            image);
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
     * Adds additional properties for groups to a container.
     *
     * @param container the container to update
     */
    public void addGroupContainerProperties(IndexedContainer container) {

        // do nothing

    }

    /**
     * Adds additional properties for users to a container.
     *
     * @param container the container to update
     */
    public void addUserContainerProperties(IndexedContainer container) {

        // do nothing

    }

    /**
     * Checks if the given user is editable.<p>
     *
     * @param id the id of the user
     * @return true if the user is editable
     */
    public boolean canEditUser(CmsUUID id) {

        return true;
    }

    /**
     * Checks if group members can be removed from the given OU.<p>
     *
     * @param group the group name
     * @return true if group members can be removed from the given OU
     */
    public boolean canRemoveGroupMemebers(String group) {

        return true;
    }

    /**
     * Checks if a set of groups can be added to a user.<p>
     *
     * @param principal the user
     * @param data the set of names of groups to check
     *
     * @return true if the groups can be added to the user
     */
    public boolean checkAddGroup(CmsUser principal, Set<String> data) {

        return true;
    }

    /**
     * Checks if a user can be removed from a set of groups.<p>
     *
     * @param principal the user
     * @param items the names of groups to check
     *
     * @return true if the user can be removed from  the group
     */
    public boolean checkRemoveGroups(CmsUser principal, Set<String> items) {

        return true;
    }

    /**
     * Fills the container item representing a group.<p>
     *
     * @param item the item
     * @param group the group
     * @param indirects the indirect groups
     */
    public void fillGroupItem(Item item, CmsGroup group, List<CmsGroup> indirects) {

        item.getItemProperty(TableProperty.Name).setValue(group.getName());
        item.getItemProperty(TableProperty.Description).setValue(group.getDescription(A_CmsUI.get().getLocale()));
        item.getItemProperty(TableProperty.OU).setValue(group.getOuFqn());
        if (indirects.contains(group)) {
            item.getItemProperty(TableProperty.INDIRECT).setValue(Boolean.TRUE);
        }
    }

    /**
    * Gets the app id.<p>
    *
    * @return the app id
    */
    public String getAppId() {

        return CmsAccountsAppConfiguration.APP_ID;
    }

    /**
     * Gets a data container for the groups available to be added to a user, excluding some groups.<p>
     *
     * @param cms the current CMS context
     * @param ouFqn the OU for which to get the groups
     * @param propCaption the property for the caption
     * @param propIcon the property for the icon
     * @param propOu the property for the OU
     * @param groupsOfUser the groups to exclude
     * @param iconProvider the icon provider
     *
     * @return the container with the group data
     */
    public IndexedContainer getAvailableGroupsContainerWithout(
        CmsObject cms,
        String ouFqn,
        String propCaption,
        String propIcon,
        String propOu,
        List<CmsGroup> groupsOfUser,
        Function<CmsGroup, CmsCssIcon> iconProvider) {

        // TODO Auto-generated method stub
        return CmsVaadinUtils.getAvailableGroupsContainerWithout(
            cms,
            ouFqn,
            propCaption,
            propIcon,
            propOu,
            groupsOfUser,
            iconProvider);
    }

    /**
     * Gets the group edit parameters for a given group.<p>
     *
     * @param group a group
     *
     * @return the group edit parameters for the group
     */
    public CmsGroupEditParameters getGroupEditParameters(CmsGroup group) {

        CmsGroupEditParameters params = new CmsGroupEditParameters();
        return params;
    }

    /**
     * Gets the icon for a group.<p>
     *
     * @param group the group
     * @return the icon for the group
     */
    public CmsCssIcon getGroupIcon(CmsGroup group) {

        return new CmsCssIcon("oc-icon-24-group");
    }

    /**
     * Gets the cache for the password reset states.
     * <p>The cache keys are user ids.
     *
     * @return the cache for the password reset states
     */
    public Map<CmsUUID, Boolean> getPasswordResetStateCache() {

        return m_passwordResetStateCache;
    }

    /**
     * Gets the user edit parameters.<p>
     *
     * @param user the user
     * @return the user edit parameters
     */
    public CmsUserEditParameters getUserEditParameters(CmsUser user) {

        CmsUserEditParameters params = new CmsUserEditParameters();
        params.setEditEnabled(true);
        params.setPasswordChangeEnabled(true);
        return params;
    }

    /**
     * Gets the container for the groups of an user for the purpose of editing them.<p>
     *
     * @param user the user
     * @param propName the property for the name
     * @param propIcon the property for the icon
     * @param propStatus the property for the status
     *
     * @return the container with the user groups
     */
    public IndexedContainer getUserGroupsEditorContainer(
        CmsUser user,
        String propName,
        String propIcon,
        String propStatus) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(propName, String.class, "");
        container.addContainerProperty(CmsUserEditGroupsDialog.ID_OU, String.class, "");
        container.addContainerProperty(propStatus, Boolean.class, Boolean.valueOf(true));
        container.addContainerProperty(propIcon, CmsCssIcon.class, new CmsCssIcon("oc-icon-group-24"));
        try {
            for (CmsGroup group : m_cms.getGroupsOfUser(user.getName(), true)) {
                Item item = container.addItem(group);
                item.getItemProperty(propName).setValue(group.getSimpleName());
                item.getItemProperty(CmsUserEditGroupsDialog.ID_OU).setValue(group.getOuFqn());
                item.getItemProperty(propIcon).setValue(getGroupIcon(group));
            }
        } catch (CmsException e) {
            LOG.error("Unable to read groups from user", e);
        }
        return container;
    }

    /**
     * Gets list of users for organizational unit.<p>
     *
     * @param cms the CMS context
     * @param ou the OU path
     * @param recursive true if users from other OUs should be retrieved
     *
     * @return the list of users, without their additional info
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsUser> getUsersWithoutAdditionalInfo(
        CmsObject cms,
        I_CmsOuTreeType type,
        String ou,
        boolean recursive)
    throws CmsException {

        return CmsPrincipal.filterCoreUsers(
            OpenCms.getOrgUnitManager().getUsersWithoutAdditionalInfo(cms, ou, recursive));
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect#handlePrincipal(org.opencms.security.I_CmsPrincipal)
     */
    public void handlePrincipal(I_CmsPrincipal principal) {

        if (m_stateBean.getType().isGroup()) {
            try {
                CmsGroup group = m_cms.readGroup(m_stateBean.getGroupID());
                m_cms.addUserToGroup(principal.getName(), group.getName());

            } catch (CmsException e) {
                return;
            }
        }
        if (m_stateBean.getType().isRole()) {
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
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changed -> {/* do nothing*/});
        super.initUI(context);
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

        updateInfoButton();
        if (comp != null) {
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            comp.setSizeFull();
            layout.addComponent(m_table.getEmptyLayout());
            layout.addComponent(m_table);
            handleSetTable(m_table);
            m_splitScreen.setSecondComponent(layout);
        } else {
            m_splitScreen.setSecondComponent(new Label("Malformed path, tool not available for path: " + state));
            handleSetTable(null);
        }
        m_splitScreen.setSizeFull();
        updateSubNav(getSubNavEntries(state));
        updateBreadCrumb(getBreadCrumbForState(state));
    }

    /**
     * Parses a given state string to state bean.<p>
     *
     * @param state to be read
     * @param baseOU baseOu
     * @return CmsStateBean
     */
    public CmsStateBean parseState(String state, String baseOU) {

        String path = baseOU;
        String filter = "";
        I_CmsOuTreeType type = CmsOuTreeType.OU;
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
            for (I_CmsOuTreeType ty : getTreeTypeProvider().getTreeTypes()) {
                if (fields.get(0).equals(ty.getId())) {
                    type = ty;
                }
            }
            if (fields.size() > 2) {
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(fields.get(2))) {
                    groupId = new CmsUUID(fields.get(2));
                }
            }
            if (fields.size() > 3) {
                filter = fields.get(3);
            }
        }
        return new CmsStateBean(path, type, groupId, filter);
    }

    /**
     * Reads the list of groups for an organizational unit.<p>
     *
     * @param cms the CMS context
     * @param ou the OU path
     * @param type the tree type
     * @param subOus true if groups for sub-OUs should be read
     * @return the list of groups for the OU
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsGroup> readGroupsForOu(CmsObject cms, String ou, I_CmsOuTreeType type, boolean subOus)
    throws CmsException {

        return CmsPrincipal.filterCoreGroups(OpenCms.getOrgUnitManager().getGroups(m_cms, ou, subOus));
    }

    /**
     * Reloads the app with current state.<p>
     */
    public void reload() {

        update(m_stateBean.getPath(), m_stateBean.getType(), m_stateBean.getGroupID());
    }

    /**
     * Updates the app state.<p>
     *
     * @param ou to be opened
     * @param type to be opened
     * @param groupID to be openend(may be null)
     */
    public void update(String ou, I_CmsOuTreeType type, CmsUUID groupID) {

        update(ou, type, groupID, m_filterTable.getValue());

    }

    /**
     * Updates the app state.<p>
     *
     * @param ou to be opened
     * @param type to be opened
     * @param roleOrGroupID to be openend(may be null)
     * @param filter filter string
     */
    public void update(String ou, I_CmsOuTreeType type, CmsUUID roleOrGroupID, String filter) {

        CmsStateBean stateBean = new CmsStateBean(ou, type, roleOrGroupID, filter);

        try {
            m_ouTree.updateOU(OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou));
        } catch (CmsException e) {
            LOG.error("Unable to read ou: " + ou);
        }
        openSubView(stateBean.getState(), true);

    }

    /**
     * Creates a table for displaying groups.<p>
     *
     * @param path the path
     * @param cmsAccountsApp the app instance
     * @param type the tree type
     * @param toggle the value of the 'sub-OU' toggle
     *
     * @return the table
     */
    protected I_CmsFilterableTable createGroupTable(
        String path,
        CmsAccountsApp cmsAccountsApp,
        I_CmsOuTreeType type,
        boolean toggle) {

        return new CmsGroupTable(path, cmsAccountsApp, type, toggle);
    }

    /**
     * Creates the overview table for the given OU.<p>
     *
     * @param ou the OU path
     * @return the overview table for the given OU
     */
    protected I_CmsFilterableTable createOUTable(String ou) {

        return new CmsOUTable(ou, this);
    }

    /**
     * Creates the role table for  the given OU.<p>
     *
     * @param ou the OU path
     * @return the role table for the given OU
     */
    protected I_CmsFilterableTable createRoleTable(String ou) {

        return new CmsRoleTable(this, ou);
    }

    /**
     * Creates user table for a specific group or role.<p>
     *
     * @param ou the OU path
     * @param groupID the group id
     * @param type the tree type
     * @param showAll true if all users should be shown
     * @param cmsAccountsApp the app instance
     *
     * @return the user table
     */
    protected I_CmsFilterableTable createUserTable(
        String ou,
        CmsUUID groupID,
        I_CmsOuTreeType type,
        boolean showAll,
        CmsAccountsApp cmsAccountsApp) {

        return new CmsUserTable(ou, groupID, type, showAll, cmsAccountsApp);
    }

    /**
     * Creates the user table for an OU.<p>
     *
     * @param ou the OU path
     * @param type the tree type
     * @param cmsAccountsApp the app instance
     * @param buttonPressed true if toggle button for users is active

     * @return the user table
     */
    protected I_CmsFilterableTable createUserTable(
        String ou,
        I_CmsOuTreeType type,
        CmsAccountsApp cmsAccountsApp,
        boolean buttonPressed) {

        return new CmsUserTable(ou, type, cmsAccountsApp, buttonPressed);
    }

    /**
     * Filters table.<p>
     *"
     * @param text for filter
     */
    protected void filterTable(String text) {

        if (m_table != null) {
            m_table.filter(text);
        }
    }

    /**
     * Gets the additional buttons to display.<p>
     *
     * @return the additional buttons to display
     */
    protected List<Button> getAdditionalButtons() {

        return new ArrayList<>();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        try {
            CmsStateBean bean = parseState(state, m_baseOU);
            String[] ouPath = bean.getPath().split("/");
            LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

            if ((bean.getPath().equals(m_baseOU))
                && (CmsOuTreeType.OU.equals(bean.getType()) | (bean.getType() == null))) {
                crumbs.put(
                    "",
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_TOOL_NAME_0)
                        + " ("
                        + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_baseOU).getDisplayName(
                            A_CmsUI.get().getLocale())
                        + ")");
                return crumbs;
            }
            CmsStateBean beanCr = new CmsStateBean(m_baseOU, CmsOuTreeType.OU, null, "");
            crumbs.put(
                getAppId() + "/" + beanCr.getState(),
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_TOOL_NAME_0)
                    + " ("
                    + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, m_baseOU).getDisplayName(
                        A_CmsUI.get().getLocale())
                    + ")");
            String base = "";
            String pathOfLastElement = "";
            for (String oP : ouPath) {
                if (!oP.isEmpty()) {
                    if ((oP + base).length() > m_baseOU.length()) {
                        if (oP.equals(ouPath[ouPath.length - 1])) {
                            CmsStateBean beanCrumb = new CmsStateBean(base + oP, CmsOuTreeType.OU, null, "");
                            pathOfLastElement = getAppId() + "/" + beanCrumb.getState();
                            crumbs.put("", oP);
                        } else {
                            CmsStateBean beanCrumb = new CmsStateBean(base + oP, CmsOuTreeType.OU, null, "");
                            crumbs.put(getAppId() + "/" + beanCrumb.getState(), oP);
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
                        CmsStateBean beanCrumb = new CmsStateBean(bean.getPath(), bean.getType(), null, "");
                        crumbs.put(getAppId() + "/" + beanCrumb.getState(), beanCrumb.getType().getName());
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

        m_stateBean = parseState(state, m_baseOU);

        if (m_filter == null) {
            iniButtons();
        }

        m_doNotChange = true;

        m_filter.setValue(m_stateBean.getPath());
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_stateBean.getTableFilter())) {
            m_filterTable.setValue(m_stateBean.getTableFilter());
        } else {
            m_filterTable.setValue("");
        }

        m_newButton.setVisible((m_stateBean.getGroupID() == null) & isOUManagable(m_stateBean.getPath()));
        m_toggleButtonRole.setVisible(m_stateBean.getType().isRole() && (m_stateBean.getGroupID() != null));
        m_toggleButtonUser.setVisible(m_stateBean.getType().isUser());
        m_importExport.setVisible(m_stateBean.getType().isOrgUnit());

        m_toggleButtonGroups.setVisible(m_stateBean.getType().isGroup() && (m_stateBean.getGroupID() == null));
        m_infoButton.setVisible(
            m_stateBean.getType().isUser()
                || (m_stateBean.getType().isRole() && (m_stateBean.getGroupID() != null))
                || (m_stateBean.getType().isGroup() && (m_stateBean.getGroupID() != null)));
        m_addElementButton.setVisible(
            (m_stateBean.getType().isGroup() || m_stateBean.getType().isRole()) & (m_stateBean.getGroupID() != null));
        m_ouTree.openPath(m_stateBean.getPath(), m_stateBean.getType(), m_stateBean.getGroupID());

        m_doNotChange = false;
        I_CmsFilterableTable table = null;
        if (m_stateBean.getType().equals(CmsOuTreeType.OU)) {
            m_table = createOUTable(m_stateBean.getPath());
            table = m_table;
        }
        if (m_stateBean.getType().isUser()) {
            m_table = createUserTable(
                m_stateBean.getPath(),
                m_stateBean.getType(),
                this,
                CmsVaadinUtils.isButtonPressed(m_toggleButtonUser));
            table = m_table;
        }
        if (m_stateBean.getType().isGroup()) {
            if (m_stateBean.getGroupID() == null) {
                m_table = createGroupTable(
                    m_stateBean.getPath(),
                    this,
                    m_stateBean.getType(),
                    CmsVaadinUtils.isButtonPressed(m_toggleButtonGroups));
                table = m_table;
            } else {
                m_table = createUserTable(
                    m_stateBean.getPath(),
                    m_stateBean.getGroupID(),
                    m_stateBean.getType(),
                    false,
                    this);
                table = m_table;
            }
        }
        if (m_stateBean.getType().isRole()) {
            if (m_stateBean.getGroupID() == null) {
                m_table = createRoleTable(m_stateBean.getPath());
                table = m_table;
            } else {
                m_table = createUserTable(
                    m_stateBean.getPath(),
                    m_stateBean.getGroupID(),
                    m_stateBean.getType(),
                    CmsVaadinUtils.isButtonPressed(m_toggleButtonRole),
                    this);
                table = m_table;
            }
        }
        if ((table != null) && !CmsStringUtil.isEmptyOrWhitespaceOnly(m_filterTable.getValue())) {
            table.filter(m_filterTable.getValue());
        }
        return table;
    }

    /**
     * Gets the group-, role-, or ou name.<p>
     *
     * @param stateBean to be read out
     * @return Name
     */
    protected String getElementName(CmsStateBean stateBean) {

        if (stateBean.getType().equals(CmsOuTreeType.USER)) {
            try {
                return OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, stateBean.getPath()).getDisplayName(
                    A_CmsUI.get().getLocale());
            } catch (CmsException e) {
                LOG.error("Unable to read OU", e);
            }
        }
        if (stateBean.getType().equals(CmsOuTreeType.ROLE)) {
            return CmsRole.valueOfId(stateBean.getGroupID()).getName(A_CmsUI.get().getLocale());
        } else {
            try {
                return m_cms.readGroup(stateBean.getGroupID()).getSimpleName();
            } catch (CmsException e) {
                LOG.error("Unable to read group", e);
            }
        }
        return "";
    }

    /**
     * Gets the full user List including additionInfos.<p>
     *
     * @param users user list
     * @return List of user
     */
    protected List<CmsUser> getFullUser(List<CmsUser> users) {

        List<CmsUser> res = new ArrayList<CmsUser>();
        for (CmsUser user : users) {
            try {
                res.add(m_cms.readUser(user.getId()));
            } catch (CmsException e) {
                LOG.error("Unable to read user", e);
            }
        }
        return res;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Get the current toggle button.<p>
     *
     * @param stateBean to be read out
     * @return Button
     */
    protected Button getToggleButton(CmsStateBean stateBean) {

        if (stateBean.getType().equals(CmsOuTreeType.USER)) {
            return m_toggleButtonUser;
        }
        if (stateBean.getType().equals(CmsOuTreeType.ROLE)) {
            return m_toggleButtonRole;
        } else {
            return m_toggleButtonGroups;
        }
    }

    /**
     * Gets the tree type provider.<p>
     *
     * @return the tree type provider
     */
    protected I_CmsTreeTypeProvider getTreeTypeProvider() {

        return DEFAULT_TREETYPES;
    }

    /**
     * Gets all currently visible user.<p>
     *
     * @return List of CmsUser
     */
    protected List<CmsUser> getVisibleUser() {

        if (m_table instanceof CmsUserTable) {
            return ((CmsUserTable)m_table).getVisibleUser();
        }
        return null;
    }

    /**
     * Called when new table is shown.<p>
     *
     * @param component the table that is displayed
     */
    protected void handleSetTable(Component component) {

        // do nothing
    }


    /**
     * Opens a dialog for a new item (ou, group or user).<p>
     */
    protected void openNewDialog() {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsBasicDialog dialog = new CmsNewElementDialog(m_cms, m_stateBean.getPath(), window, this);
        window.setContent(dialog);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_ELEMENT_0));
        A_CmsUI.get().addWindow(window);
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
            CmsPrincipalSelect.PrincipalType.user);

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
     * Toggles the table.<p>
     *
     * @param toggleButton the toggle button state
     */
    void toggleTable(Button toggleButton) {

        I_CmsToggleTable table = (I_CmsToggleTable)m_table;
        table.toggle(!CmsVaadinUtils.isButtonPressed(toggleButton));
        CmsVaadinUtils.toggleButton(toggleButton);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_filterTable.getValue())) {
            filterTable(m_filterTable.getValue());
        }
        updateInfoButton();
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
        Button csvButton = new Button(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EXPORT_ONLY_USER_0));
        csvButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5472430305539438757L;

            public void buttonClick(ClickEvent event) {

                boolean includeTechnicalFields = false;
                try {
                    OpenCms.getRoleManager().checkRole(m_cms, CmsRole.ADMINISTRATOR);
                    includeTechnicalFields = true;
                } catch (CmsRoleViolationException e) {
                    // ok
                }
                Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                CmsUserCsvExportDialog dialog = new CmsUserCsvExportDialog(
                    getFullUser(getVisibleUser()),
                    m_stateBean.getPath(),
                    m_stateBean.getType(),
                    getElementName(m_stateBean),
                    CmsVaadinUtils.isButtonPressed(getToggleButton(m_stateBean)),
                    window,
                    includeTechnicalFields);
                window.setContent(dialog);
                A_CmsUI.get().addWindow(window);

            }

        });

        m_infoButton.setAdditionalButton(csvButton);

        m_addElementButton = CmsToolBar.createButton(
            FontAwesome.PLUS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_TO_GROUP_0));
        m_addElementButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1859694635385726953L;

            public void buttonClick(ClickEvent event) {

                openAddUserDialog();

            }
        });

        m_importExport = CmsToolBar.createButton(FontOpenCms.DOWNLOAD, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_CONTEXTMENUNAME_0));
        m_importExport.addClickListener(event -> {
            CmsOUTable.openImportExportDialog(A_CmsUI.getCmsObject(), m_stateBean.getPath());
        });


        m_toggleButtonRole = CmsToolBar.createButton(
            FontOpenCms.USERS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ROLES_TOGGLE_0));
        m_toggleButtonRole.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8265075332953321274L;

            public void buttonClick(ClickEvent event) {

                toggleTable(m_toggleButtonRole);

            }

        });

        m_toggleButtonUser = CmsToolBar.createButton(
            FontOpenCms.USERS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_TOGGLE_0));
        m_toggleButtonUser.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8265075332953321274L;

            public void buttonClick(ClickEvent event) {

                toggleTable(m_toggleButtonUser);

            }

        });

        m_toggleButtonGroups = CmsToolBar.createButton(
            FontOpenCms.USERS,
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUPS_TOGGLE_0));
        m_toggleButtonGroups.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 8265075332953321274L;

            public void buttonClick(ClickEvent event) {

                toggleTable(m_toggleButtonGroups);

            }

        });

        m_uiContext.addToolbarButton(m_newButton);

        m_uiContext.addToolbarButton(m_addElementButton);
        m_uiContext.addToolbarButton(m_infoButton);
        m_uiContext.addToolbarButton(m_toggleButtonRole);
        m_uiContext.addToolbarButton(m_toggleButtonUser);
        m_uiContext.addToolbarButton(m_toggleButtonGroups);
        m_uiContext.addToolbarButton(m_importExport);

        for (Button button : getAdditionalButtons()) {
            m_uiContext.addToolbarButton(button);
        }
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
        m_filterTable.setWidth("200px");
        m_infoLayout.addComponent(m_filterTable);
        m_infoLayout.addStyleName("o-many-elements");
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
     * Updates the info button.<p>
     */
    private void updateInfoButton() {

        if (m_stateBean.getType().isUser()) {
            Map<String, String> dataMap = new LinkedHashMap<String, String>();
            dataMap.put(
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                String.valueOf(((Table)m_table).size()));
            try {
                int count = getUsersWithoutAdditionalInfo(
                    m_cms,
                    m_stateBean.getType(),
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
            int size = ((Table)m_table).size();
            if (m_table instanceof CmsUserTable) {
                size = ((CmsUserTable)m_table).getVisibleUser().size();
            }
            m_infoButton.replaceData(
                Collections.singletonMap(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_COUNT_0),
                    String.valueOf(size)));
        }
    }

}
