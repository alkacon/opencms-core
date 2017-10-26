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
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;

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
        GROUP(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUPS_0)),
        /**OU. */
        OU(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_OU_0)),
        /**User.*/
        USER(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_0));

        /**Name of entry. */
        private String m_name;

        /**
         * constructor.<p>
         *
         * @param name name
         */
        CmsOuTreeType(String name) {
            m_name = name;
        }

        /**
         * Gets the name of the element.<p>
         *
         * @return name
         */
        public String getName() {

            return m_name;
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
            String[] pathP = path.split("/");
            String complPath = "";
            for (String subP : pathP) {
                complPath += subP + "/";
                CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, complPath);
                addChildForOU(ou);
                expandItem(ou);
            }

            if (type.equals(CmsOuTreeType.GROUP)) {
                String itemId = "G" + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName();
                expandItem(itemId);
                if (groupID == null) {
                    setValue(itemId);
                    return;
                }
                setValue(m_cms.readGroup(groupID));
                return;
            }
            if (type.equals(CmsOuTreeType.USER)) {
                setValue("U" + OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, path).getName());
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
        m_app.update(getOuFromItem(itemId, type), type, itemId instanceof CmsGroup ? ((CmsGroup)itemId).getId() : null);
        if (isExpanded(itemId) | (itemId instanceof CmsPrincipal)) {
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
                Item groupItem = m_treeContainer.addItem(group);
                if (groupItem == null) {
                    groupItem = getItem(group);
                }
                groupItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(group, CmsOuTreeType.GROUP));
                groupItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.GROUP);
                setChildrenAllowed(group, false);
                m_treeContainer.setParent(group, ouItem);
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
            String groupItemId = "G" + item.getName();
            Item groupItem = m_treeContainer.addItem(groupItemId);
            if (groupItem != null) {
                groupItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(groupItemId, CmsOuTreeType.GROUP));
                groupItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.GROUP);
                m_treeContainer.setParent(groupItemId, item);
            }

            String userItemId = "U" + item.getName();
            Item userItem = m_treeContainer.addItem(userItemId);
            if (userItem != null) {
                userItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(userItemId, CmsOuTreeType.USER));
                userItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.USER);
                setChildrenAllowed(userItemId, false);
                m_treeContainer.setParent(userItemId, item);
            }

            List<CmsOrganizationalUnit> ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(
                m_cms,
                item.getName(),
                false);

            for (CmsOrganizationalUnit ou : ous) {
                Item containerItem;
                containerItem = m_treeContainer.addItem(ou);
                if (containerItem == null) {
                    containerItem = getItem(ou);
                }
                containerItem.getItemProperty(PROP_NAME).setValue(getIconCaptionHTML(ou, CmsOuTreeType.OU));
                containerItem.getItemProperty(PROP_TYPE).setValue(CmsOuTreeType.OU);
                m_treeContainer.setParent(ou, item);
            }

        } catch (CmsException e) {
            LOG.error("Can't read ou", e);
        }
    }

    /**
     * Get HTML for icon and item caption.<p>
     *
     * @param item item to get icon and caption for
     * @param type type
     * @return html
     */
    private String getIconCaptionHTML(Object item, CmsOuTreeType type) {

        if (type.equals(CmsOuTreeType.OU)) {
            CmsOrganizationalUnit ou = (CmsOrganizationalUnit)item;
            CmsCssIcon icon;
            if (ou.hasFlagWebuser()) {
                icon = new CmsCssIcon(OpenCmsTheme.ICON_OU_WEB);
            } else {
                icon = new CmsCssIcon(OpenCmsTheme.ICON_OU);
            }
            return "<span class=\"o-resource-icon\">"
                + icon.getHtml()
                + "</span>"
                + "<span class=\"o-tree-caption\">"
                + (ou.equals(m_rootSystemOU) ? ou.getDisplayName(A_CmsUI.get().getLocale()) : ou.getName())
                + "</span>";

        }

        if (type.equals(CmsOuTreeType.GROUP)) {
            CmsCssIcon icon = new CmsCssIcon(OpenCmsTheme.ICON_GROUP);
            if (item instanceof CmsGroup) {
                //Real group shown under groups
                return icon.getHtml() + "<span class=\"o-tree-caption\">" + ((CmsGroup)item).getName() + "</span>";
            }
            //parent item for the groups
            return "<span class=\"o-resource-icon\">"
                + icon.getHtml()
                + "</span>"
                + "<span class=\"o-tree-caption\">"
                + CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUPS_0)
                + "</span>";
        }

        if (type.equals(CmsOuTreeType.USER)) {
            CmsCssIcon icon = new CmsCssIcon(OpenCmsTheme.ICON_USER);
            return "<span class=\"o-resource-icon\">"
                + icon.getHtml()
                + "</span>"
                + "<span class=\"o-tree-caption\">"
                + CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_0)
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
        expandItem(itemId);
    }

}
