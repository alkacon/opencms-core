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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Tree;

/**
 * Class for the OU Tree.<p>
 */
public class CmsOuTree extends Tree {

    /**Type of element.*/
    protected enum CmsOuTreeType {

        /**Group. */
        GROUP(Messages.GUI_USERMANAGEMENT_GROUPS_0, "g", true, new CmsCssIcon(OpenCmsTheme.ICON_GROUP), Messages.GUI_USERMANAGEMENT_NO_GROUPS_0),
        /**OU. */
        OU(Messages.GUI_USERMANAGEMENT_USER_OU_0, "o", true, new CmsCssIcon(OpenCmsTheme.ICON_OU), ""),
        /**Role. */
        ROLE(Messages.GUI_USERMANAGEMENT_ROLES_0, "r", true, new CmsCssIcon(OpenCmsTheme.ICON_ROLE), Messages.GUI_USERMANAGEMENT_NO_USER_0),
        /**User.*/
        USER(Messages.GUI_USERMANAGEMENT_USER_0, "u", false, new CmsCssIcon(OpenCmsTheme.ICON_USER), Messages.GUI_USERMANAGEMENT_NO_USER_0);

        /**Name of entry. */
        private String m_name;

        /**ID for entry. */
        private String m_id;

        /**Is expandable?*/
        private boolean m_isExpandable;

        /**Icon for type. */
        private CmsCssIcon m_icon;

        /**Bundle key for empty message.*/
        private String m_emptyMessageKey;

        /**
         * constructor.<p>
         *
         * @param name name
         * @param id id
         * @param isExpandable boolean
         * @param icon icon
         * @param empty empty string
         */
        CmsOuTreeType(String name, String id, boolean isExpandable, CmsCssIcon icon, String empty) {
            m_name = name;
            m_id = id;
            m_isExpandable = isExpandable;
            m_icon = icon;
            m_emptyMessageKey = empty;
        }

        /**
         * Returns tree type from id.<p>
         *
         * @param id of type
         * @return CmsOuTreeType
         */
        public static CmsOuTreeType fromID(String id) {

            for (CmsOuTreeType ty : values()) {
                if (ty.getID().equals(id)) {
                    return ty;
                }
            }
            return null;
        }

        /**
         * Returns tree type from name.<p>
         *
         * @param name of type
         * @return CmsOuTreeType
         */
        public static CmsOuTreeType fromName(String name) {

            for (CmsOuTreeType ty : values()) {
                if (ty.getName().equals(name)) {
                    return ty;
                }
            }
            return null;
        }

        /**
         * Returns the key for the empty-message.<p>
         *
         * @return key as string
         */
        public String getEmptyMessageKey() {

            return m_emptyMessageKey;
        }

        /**
         * Get the icon.<p>
         *
         * @return CmsCssIcon
         */
        public CmsCssIcon getIcon() {

            return m_icon;
        }

        /**
         * Gets the id of the type.<p>
         *
         * @return id string
         */
        public String getID() {

            return m_id;
        }

        /**
         * Gets the name of the element.<p>
         *
         * @return name
         */
        public String getName() {

            return CmsVaadinUtils.getMessageText(m_name);
        }

        /**
         * Checks if type is expandable.<p>
         *
         * @return true if expandable
         */
        public boolean isExpandable() {

            return m_isExpandable;
        }
    }

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsOuTree.class);

    /**Root OU.*/
    private static CmsOrganizationalUnit m_rootSystemOU;

    /**name property. */
    private static final String PROP_NAME = "name";

    /**type property. */
    private static final String PROP_TYPE = "type";

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3532367333216144806L;

    /**Calling app. */
    private CmsAccountsApp m_app;

    /**CmsObject. */
    private CmsObject m_cms;

    /**Root ou. */
    private CmsOrganizationalUnit m_rootOu;

    /**Container. */
    private HierarchicalContainer m_treeContainer;

    /**
     * constructor.<p>
     *
     * @param cms CmsObject
     * @param app app instance
     * @param baseOU baseOu
     */
    public CmsOuTree(CmsObject cms, CmsAccountsApp app, String baseOU) {
        m_cms = cms;
        try {
            m_rootSystemOU = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, "");
        } catch (CmsException e1) {
            //
        }
        m_app = app;
        addStyleName(OpenCmsTheme.FULL_WIDTH_PADDING);
        addStyleName(OpenCmsTheme.SIMPLE_DRAG);
        setWidth("100%");
        m_treeContainer = new HierarchicalContainer();
        m_treeContainer.addContainerProperty(PROP_NAME, String.class, "");
        m_treeContainer.addContainerProperty(PROP_TYPE, CmsOuTreeType.class, null);
        setContainerDataSource(m_treeContainer);

        m_rootOu = null;
        try {
            m_rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, baseOU);
            Item item = m_treeContainer.addItem(m_rootOu);
            item.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(m_rootOu, CmsOuTreeType.OU));
            item.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.OU);
        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
        setItemCaptionPropertyId(PROP_NAME);
        setHtmlContentAllowed(true);
        setNullSelectionAllowed(false);
        addChildForOU(m_rootOu);
        expandItem(m_rootOu);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -6475529853027436127L;

            public void itemClick(ItemClickEvent event) {

                handleItemClick(event.getItemId());

            }
        });
        addExpandListener(new ExpandListener() {

            private static final long serialVersionUID = 589297480547091120L;

            public void nodeExpand(ExpandEvent event) {

                handleExpand(event.getItemId());

            }
        });
    }

    /**
     * Opens given path.<p>
     *
     * @param path ou path (=ou-name)
     * @param type type (ou,group or user)
     * @param groupID id of group (optional)
     */
    public void openPath(String path, CmsOuTreeType type, CmsUUID groupID) {

        if (type == null) {
            return;
        }
        try {
            expandItem(m_rootOu);
            String[] pathP = path.split("/");
            String complPath = "";
            for (String subP : pathP) {
                complPath += subP + "/";
                CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, complPath);
                addChildForOU(ou);
                expandItem(ou);
            }

            if (type.equals(CmsOuTreeType.GROUP) | type.equals(CmsOuTreeType.ROLE)) {
                String itemId = type.getID()
                    + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName();
                expandItem(itemId);
                if (groupID == null) {
                    setValue(itemId);
                    return;
                }
                setValue(groupID);
                return;
            }
            if (type.equals(CmsOuTreeType.USER)) {
                setValue(type.getID() + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName());
                return;
            }

            setValue(OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path));

        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
    }

    /**
     * Handle expand action.<p>
     *
     * @param itemId which was expended
     */
    protected void handleExpand(Object itemId) {

        CmsOuTreeType type = (CmsOuTreeType)getItem(itemId).getItemProperty(PROP_TYPE).getValue();
        loadAndExpand(itemId, type);
    }

    /**
     * Handle item click.<p>
     *
     * @param itemId item which was clicked
     */
    protected void handleItemClick(Object itemId) {

        CmsOuTreeType type = (CmsOuTreeType)getItem(itemId).getItemProperty(PROP_TYPE).getValue();
        CmsUUID roleOrGroupID = null;
        if (itemId instanceof CmsUUID) {

            roleOrGroupID = (CmsUUID)itemId;

        }

        m_app.update(getOuFromItem(itemId, type), type, roleOrGroupID);
        if (isExpanded(itemId) | (itemId instanceof CmsUUID)) {
            return;
        }
        loadAndExpand(itemId, type);
        setValue(itemId);

    }

    /**
     * Add groups for given group parent item.
     *
     * @param ouItem group parent item
     */
    private void addChildForGroup(String ouItem) {

        try {
            List<CmsGroup> groups = OpenCms.getOrgUnitManager().getGroups(m_cms, ouItem.substring(1), false);
            for (CmsGroup group : groups) {
                Item groupItem = m_treeContainer.addItem(group.getId());
                if (groupItem == null) {
                    groupItem = getItem(group.getId());
                }
                groupItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(group, CmsOuTreeType.GROUP));
                groupItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.GROUP);
                setChildrenAllowed(group.getId(), false);
                m_treeContainer.setParent(group.getId(), ouItem);
            }
        } catch (CmsException e) {
            LOG.error("Can not read group", e);
        }
    }

    /**
     * Add children for ou.<p>
     *
     * @param item ou item
     */
    private void addChildForOU(CmsOrganizationalUnit item) {

        try {
            if (m_app.isOUManagable(item.getName())) {
                List<CmsOuTreeType> types = Arrays.asList(CmsOuTreeType.GROUP, CmsOuTreeType.ROLE, CmsOuTreeType.USER);
                for (CmsOuTreeType type : types) {
                    String itemId = type.getID() + item.getName();
                    Item newItem = m_treeContainer.addItem(itemId);
                    if (newItem != null) {
                        newItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(itemId, type));
                        newItem.getItemProperty(PROP_TYPE).setValue(type);
                        m_treeContainer.setParent(itemId, item);
                        setChildrenAllowed(itemId, type.isExpandable());
                    }
                }
            }
            List<CmsOrganizationalUnit> ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(
                m_cms,
                item.getName(),
                false);
            List<CmsOrganizationalUnit> webOus = new ArrayList<CmsOrganizationalUnit>();
            for (CmsOrganizationalUnit ou : ous) {
                if (m_app.isParentOfManagableOU(ou.getName())) {
                    if (ou.hasFlagWebuser()) {
                        webOus.add(ou);
                    } else {
                        addOuToTree(ou, item);
                    }
                }
            }
            for (CmsOrganizationalUnit ou : webOus) {
                if (m_app.isParentOfManagableOU(ou.getName())) {
                    addOuToTree(ou, item);
                }
            }
        } catch (CmsException e) {
            LOG.error("Can't read ou", e);
        }
    }

    /**
     * Add roles for given role parent item.
     *
     * @param ouItem group parent item
     */
    private void addChildForRole(String ouItem) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(m_cms, ouItem.substring(1), false);
            CmsRole.applySystemRoleOrder(roles);
            for (CmsRole role : roles) {
                Item roleItem = m_treeContainer.addItem(role.getId());
                if (roleItem == null) {
                    roleItem = getItem(role.getId());
                }
                roleItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(role, CmsOuTreeType.ROLE));
                roleItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.ROLE);
                setChildrenAllowed(role.getId(), false);
                m_treeContainer.setParent(role.getId(), ouItem);
            }
        } catch (CmsException e) {
            LOG.error("Can not read group", e);
        }
    }

    /**
     * Adds an ou to the tree.<p>
     *
     * @param ou to be added
     * @param parent_ou parent ou
     */
    private void addOuToTree(CmsOrganizationalUnit ou, CmsOrganizationalUnit parent_ou) {

        Item containerItem;
        containerItem = m_treeContainer.addItem(ou);
        if (containerItem == null) {
            containerItem = getItem(ou);
        }
        containerItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(ou, CmsOuTreeType.OU));
        containerItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.OU);
        m_treeContainer.setParent(ou, parent_ou);
    }

    /**
     * Get HTML for icon and item caption.<p>
     *
     * @param item item to get icon and caption for
     * @param type type
     * @return html
     */
    private String getIconCaptionHTML(Object item, CmsOuTreeType type) {

        CmsCssIcon icon = type.getIcon();
        String caption = type.getName();
        if (type.equals(CmsOuTreeType.OU)) {
            CmsOrganizationalUnit ou = (CmsOrganizationalUnit)item;
            if (ou.hasFlagWebuser()) {
                icon = new CmsCssIcon(OpenCmsTheme.ICON_OU_WEB);
            }
            caption = (ou.equals(m_rootSystemOU) ? ou.getDisplayName(A_CmsUI.get().getLocale()) : ou.getName());
        }

        if (item instanceof CmsGroup) {
            //Real group shown under groups
            caption = ((CmsGroup)item).getName();
        }

        if (item instanceof CmsRole) {
            //Real group shown under groups
            caption = ((CmsRole)item).getName(A_CmsUI.get().getLocale());
        }

        if (icon != null) {
            return "<span class=\"o-resource-icon\">"
                + icon.getHtml()
                + "</span>"
                + "<span class=\"o-tree-caption\">"
                + caption
                + "</span>";
        }
        return "";
    }

    /**
     * Gets ou from given item.<p>
     *
     * @param itemId to get ou for
     * @param type of given item
     * @return name of ou
     */
    private String getOuFromItem(Object itemId, CmsOuTreeType type) {

        if (type.equals(CmsOuTreeType.OU)) {
            return ((CmsOrganizationalUnit)itemId).getName();
        }
        Object o = m_treeContainer.getParent(itemId);
        while (!(o instanceof CmsOrganizationalUnit)) {
            o = m_treeContainer.getParent(o);
        }
        return ((CmsOrganizationalUnit)o).getName();
    }

    /**
     * Load and expand given item.<p>
     *
     * @param itemId to be expanded
     * @param type of item
     */
    private void loadAndExpand(Object itemId, CmsOuTreeType type) {

        if (type.equals(CmsOuTreeType.OU)) {
            addChildForOU((CmsOrganizationalUnit)itemId);
        }
        if (type.equals(CmsOuTreeType.GROUP)) {
            addChildForGroup((String)itemId);
        }
        if (type.equals(CmsOuTreeType.ROLE)) {
            addChildForRole((String)itemId);
        }
        expandItem(itemId);
    }

}
