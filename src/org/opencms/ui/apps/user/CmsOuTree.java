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
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Tree;

/**
 * Class for the OU Tree.<p>
 */
@SuppressWarnings("deprecation")
public class CmsOuTree extends Tree {

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

    private static final String PROP_SID = "sid";

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
        m_treeContainer.addContainerProperty(PROP_TYPE, I_CmsOuTreeType.class, null);
        m_treeContainer.addContainerProperty(PROP_SID, CmsUUID.class, null);
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
        addChildrenForOUNode(m_rootOu);
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
    public void openPath(String path, I_CmsOuTreeType type, CmsUUID groupID) {

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
                addChildrenForOUNode(ou);
                expandItem(ou);
            }

            if (type.isGroup() || type.isRole()) {
                String itemId = type.getId()
                    + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName();
                expandItem(itemId);
                if (groupID == null) {
                    setValue(itemId);
                    return;
                }
                setValue(groupID);
                return;
            }
            if (type.isUser()) {
                setValue(type.getId() + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName());
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

        I_CmsOuTreeType type = (I_CmsOuTreeType)getItem(itemId).getItemProperty(PROP_TYPE).getValue();
        loadAndExpand(itemId, type);
    }

    /**
     * Handle item click.<p>
     *
     * @param itemId item which was clicked
     */

    protected void handleItemClick(Object itemId) {

        Item item = getItem(itemId);
        I_CmsOuTreeType type = (I_CmsOuTreeType)getItem(itemId).getItemProperty(PROP_TYPE).getValue();
        CmsUUID roleOrGroupID = null;
        boolean idInItem = false;
        if (itemId instanceof CmsUUID) {
            roleOrGroupID = (CmsUUID)itemId;
            idInItem = true;
        } else if (item.getItemProperty(PROP_SID).getValue() != null) {
            roleOrGroupID = (CmsUUID)(item.getItemProperty(PROP_SID).getValue());
            idInItem = true;
        }
        if (type.equals(CmsOuTreeType.ROLE)) {
            String ou = getOuFromItem(itemId, CmsOuTreeType.ROLE);
            boolean isRoot = ou.isEmpty();
            if (isRoot) {
                ou = "/";
            }
            if (!((String)itemId).endsWith(ou)) {
                if (isRoot) {
                    if (((String)itemId).length() > (ou.length() + 1)) {
                        roleOrGroupID = new CmsUUID(((String)itemId).substring(ou.length() + 1));
                    }
                } else {
                    roleOrGroupID = new CmsUUID(((String)itemId).substring(ou.length() + 2));
                }
            }
        }

        m_app.update(getOuFromItem(itemId, type), type, roleOrGroupID, "");
        if (isExpanded(itemId) || idInItem) {
            return;
        }
        loadAndExpand(itemId, type);
        setValue(itemId);

    }

    /**
     * Updates items of current ou for item.<p>
     *
     * @param item to update ou for
     */
    void updateOU(CmsOrganizationalUnit item) {

        //Check if ou has children ... vaadin returns null if not
        if (m_treeContainer.getChildren(item) == null) {
            return;
        }
        for (Object it : m_treeContainer.getChildren(item)) {
            if (isExpanded(it)) {
                I_CmsOuTreeType type = (I_CmsOuTreeType)getItem(it).getItemProperty(PROP_TYPE).getValue();
                if (type.isGroup()) {
                    addChildrenForGroupsNode(type, type.getId() + item.getName());
                }
            }
        }
    }

    /**
     * Add groups for given group parent item.
     *
     * @param type the tree type
     * @param ouItem group parent item
     */
    private void addChildrenForGroupsNode(I_CmsOuTreeType type, String ouItem) {

        try {
            // Cut of type-specific prefix from ouItem with substring()
            List<CmsGroup> groups = m_app.readGroupsForOu(m_cms, ouItem.substring(1), type, false);

            List<Object> itemsToRemove = new ArrayList<Object>();

            Collection<?> childCol = m_treeContainer.getChildren(ouItem);
            if (childCol != null) {
                itemsToRemove.addAll(childCol);
            }
            for (CmsGroup group : groups) {
                Pair<String, CmsUUID> key = Pair.of(type.getId(), group.getId());
                Item groupItem = m_treeContainer.addItem(key);
                if (groupItem == null) {
                    groupItem = getItem(key);
                    itemsToRemove.remove(key);
                }
                groupItem.getItemProperty(PROP_SID).setValue(group.getId());
                groupItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(group, CmsOuTreeType.GROUP));
                groupItem.getItemProperty(PROP_TYPE).setValue(type);
                setChildrenAllowed(key, false);
                m_treeContainer.setParent(key, ouItem);
            }

            for (Object item : itemsToRemove) {
                m_treeContainer.removeItem(item);
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
    private void addChildrenForOUNode(CmsOrganizationalUnit item) {

        List<Object> itemsToRemove = new ArrayList<Object>();

        Collection<?> childCol = m_treeContainer.getChildren(item);
        if (childCol != null) {
            itemsToRemove.addAll(childCol);
        }

        try {
            if (m_app.isOUManagable(item.getName())) {

                List<I_CmsOuTreeType> types = m_app.getTreeTypeProvider().getTreeTypes();
                for (I_CmsOuTreeType type : types) {
                    if (!type.isValidForOu(m_cms, item.getName())) {
                        continue;
                    }
                    if (type.isOrgUnit()) {
                        continue;
                    }
                    String itemId = type.getId() + item.getName();
                    Item newItem = m_treeContainer.addItem(itemId);
                    itemsToRemove.remove(itemId);
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
                    itemsToRemove.remove(ou);
                    if (ou.hasFlagWebuser()) {
                        webOus.add(ou);
                    } else {
                        addOuToTree(ou, item);
                    }
                }
            }
            for (CmsOrganizationalUnit ou : webOus) {
                if (m_app.isParentOfManagableOU(ou.getName())) {
                    itemsToRemove.remove(ou);
                    addOuToTree(ou, item);
                }
            }
        } catch (CmsException e) {
            LOG.error("Can't read ou", e);
        }
        for (Object it : itemsToRemove) {
            m_treeContainer.removeItemRecursively(it);
        }
    }

    /**
     * Add roles for given role parent item.
     *
     * @param ouItem group parent item
     */
    private void addChildrenForRolesNode(String ouItem) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(m_cms, ouItem.substring(1), false);
            CmsRole.applySystemRoleOrder(roles);
            for (CmsRole role : roles) {
                String roleId = ouItem + "/" + role.getId();
                Item roleItem = m_treeContainer.addItem(roleId);
                if (roleItem == null) {
                    roleItem = getItem(roleId);
                }
                roleItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(role, CmsOuTreeType.ROLE));
                roleItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.ROLE);
                setChildrenAllowed(roleId, false);
                m_treeContainer.setParent(roleId, ouItem);
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
    private String getIconCaptionHTML(Object item, I_CmsOuTreeType type) {

        CmsCssIcon icon = type.getIcon();
        String caption = type.getName();
        if (item instanceof CmsOrganizationalUnit) {
            CmsOrganizationalUnit ou = (CmsOrganizationalUnit)item;
            if (ou.hasFlagWebuser()) {
                icon = new CmsCssIcon(OpenCmsTheme.ICON_OU_WEB);
            }
            caption = (ou.equals(m_rootSystemOU) ? ou.getDisplayName(A_CmsUI.get().getLocale()) : ou.getName());
        }

        if (item instanceof CmsGroup) {
            //Real group shown under groups
            caption = ((CmsGroup)item).getName();
            icon = m_app.getGroupIcon((CmsGroup)item);
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
    private String getOuFromItem(Object itemId, I_CmsOuTreeType type) {

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
    private void loadAndExpand(Object itemId, I_CmsOuTreeType type) {

        if (type.isOrgUnit()) {
            addChildrenForOUNode((CmsOrganizationalUnit)itemId);
        }
        if (type.isGroup()) {
            addChildrenForGroupsNode(type, (String)itemId);
        }
        if (type.isRole()) {
            addChildrenForRolesNode((String)itemId);
        }
        expandItem(itemId);
    }

}
