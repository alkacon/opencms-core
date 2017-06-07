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

package org.opencms.ui.components.fileselect;

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.util.I_CmsItemSorter;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * The data container for the sitmeap folder selection tree.<p>
 */
public class CmsResourceTreeContainer extends HierarchicalContainer {

    /** Property which is used to store the CmsResource. */
    public static final String PROPERTY_RESOURCE = "RESOURCE";

    /** Property which is used to store the sitemap view caption HTML. */
    public static final String PROPERTY_SITEMAP_CAPTION = "SITEMAP_CAPTION";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTreeContainer.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The resource filter. */
    private CmsResourceFilter m_filter;

    /**
     * Default constructor.<p>
     *
     * @param filter the resource filter to use
     */
    public CmsResourceTreeContainer(CmsResourceFilter filter) {
        m_filter = filter;
        defineProperties();
    }

    /**
     * Adds an item to the folder tree.<p>
     *
     * @param cms the CMS context
     * @param resource the folder resource
     * @param parentId the parent folder id
     */
    public void addTreeItem(CmsObject cms, CmsResource resource, CmsUUID parentId) {

        List<Container.Filter> filters = Lists.newArrayList(getContainerFilters());

        // getItem only finds an existing item if it isn't filtered out by the container
        // filters, so we temporarily remove the filters to access the complete data set
        removeAllContainerFilters();
        try {
            Item resourceItem = getItem(resource.getStructureId());
            if (resourceItem == null) {
                resourceItem = addItem(resource.getStructureId());
            }
            fillProperties(cms, resourceItem, resource, parentId);
            if (resource.isFile()) {
                setChildrenAllowed(resource.getStructureId(), false);
            }
            if (parentId != null) {
                setParent(resource.getStructureId(), parentId);
            }
        } finally {
            for (Container.Filter filter : filters) {
                addContainerFilter(filter);
            }
        }
    }

    /**
     * @see com.vaadin.data.util.IndexedContainer#getSortableContainerPropertyIds()
     */
    @Override
    public Collection<?> getSortableContainerPropertyIds() {

        if (getItemSorter() instanceof I_CmsItemSorter) {
            return ((I_CmsItemSorter)getItemSorter()).getSortableContainerPropertyIds(this);
        } else {
            return super.getSortableContainerPropertyIds();
        }
    }

    /**
     * Initializes the root level of the tree.<p>
     *
     * @param cms the CMS context
     * @param root the root folder
     */
    public void initRoot(CmsObject cms, CmsResource root) {

        addTreeItem(cms, root, null);
        readTreeLevel(cms, root.getStructureId());
    }

    /**
     * Reads the given tree level.<p>
     * @param cms the CMS context
     * @param parentId the parent id
     */
    public void readTreeLevel(CmsObject cms, CmsUUID parentId) {

        try {
            CmsResource parent = cms.readResource(parentId, m_filter);
            List<CmsResource> children = cms.readResources(parent, m_filter, false);

            // sets the parent to leaf mode, in case no child folders are present
            setChildrenAllowed(parentId, !children.isEmpty());

            for (CmsResource resource : children) {
                addTreeItem(cms, resource, parentId);
            }
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog(
                CmsVaadinUtils.getMessageText(Messages.ERR_EXPLORER_CAN_NOT_READ_RESOURCE_1, parentId),
                e);
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Clears the given tree level.<p>
     *
     * @param parentId the parent id
     */
    public void removeChildren(CmsUUID parentId) {

        // create a new list to avoid concurrent modifications
        Collection<?> children = getChildren(parentId);
        // may be null when monkey clicking
        if (children != null) {
            List<Object> childIds = new ArrayList<Object>(children);
            for (Object childId : childIds) {
                removeItemRecursively(childId);
            }
        }
    }

    /**
     * Updates the item for the given structure id.<p>
     *
     * @param cms the CMS context
     * @param id the structure id
     * @param filter the resource filter used for reading the resource
     *
     * @throws CmsException if something goes wrong
     */
    public void update(CmsObject cms, CmsUUID id, CmsResourceFilter filter) throws CmsException {

        try {
            CmsResource resource = cms.readResource(id, filter);

            CmsResource parent = cms.readParentFolder(id);
            CmsUUID parentId = parent.getStructureId();
            Item resourceItem = getItem(id);
            if (resourceItem != null) {
                fillProperties(cms, resourceItem, resource, parentId);
                if (parentId != null) {
                    setParent(resource.getStructureId(), parentId);
                }
            } else {
                addTreeItem(cms, resource, parentId);
            }
        } catch (CmsVfsResourceNotFoundException e) {
            removeItemRecursively(id);
            LOG.debug(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the item order according to the latest sort setting.<p>
     */
    public void updateSort() {

        doSort();

        // Post sort updates
        if (isFiltered()) {
            filterAll();
        } else {
            fireItemSetChange();
        }
    }

    /**
     * Defines the container properties.<p>
     */
    protected void defineProperties() {

        addContainerProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME, String.class, null);
        addContainerProperty(CmsResourceTableProperty.PROPERTY_STATE, CmsResourceState.class, null);
        addContainerProperty(CmsResourceTableProperty.PROPERTY_TREE_CAPTION, String.class, null);
        addContainerProperty(CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT, Boolean.class, Boolean.TRUE);
        addContainerProperty(CmsResourceTableProperty.PROPERTY_IS_FOLDER, Boolean.class, Boolean.TRUE);
        addContainerProperty(PROPERTY_RESOURCE, CmsResource.class, null);

        addContainerProperty(CmsResourceTableProperty.PROPERTY_IN_NAVIGATION, Boolean.class, Boolean.FALSE);
        addContainerProperty(
            CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION,
            Float.class,
            Float.valueOf(Float.MAX_VALUE));
        addContainerProperty(PROPERTY_SITEMAP_CAPTION, String.class, "");
        addContainerProperty(
            CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT,
            CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.getColumnType(),
            "");
    }

    /**
     * Fills the properties of a tree item.<p>
     *
     * @param cms the CMS context
     * @param resourceItem the empty item
     * @param resource the resource for which the tree item is being created
     * @param parentId the parent id
     */
    protected void fillProperties(CmsObject cms, Item resourceItem, CmsResource resource, CmsUUID parentId) {

        resourceItem.getItemProperty(PROPERTY_RESOURCE).setValue(resource);
        // use the root path as name in case of the root item
        String name = getName(cms, resource, parentId);
        if (resource.isFolder() && !name.endsWith("/")) {
            name += "/";
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).setValue(name);
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_STATE).setValue(resource.getState());

        resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).setValue(Boolean.valueOf(resUtil.isInsideProject()));
        resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_IS_FOLDER).setValue(
            Boolean.valueOf(resource.isFolder()));
        try {
            CmsObject rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            CmsJspNavBuilder builder = new CmsJspNavBuilder(rootCms);
            CmsJspNavElement nav = builder.getNavigationForResource(
                resource.getRootPath(),
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            boolean inNavigation = nav.isInNavigation();
            resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_IN_NAVIGATION).setValue(
                Boolean.valueOf(inNavigation));

            if (inNavigation) {
                resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION).setValue(
                    Float.valueOf(nav.getNavPosition()));
            }
            String navText = null;
            if (nav.getProperties().containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT)) {
                navText = nav.getProperties().get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            } else if (nav.getProperties().containsKey(CmsPropertyDefinition.PROPERTY_TITLE)) {
                navText = nav.getProperties().get(CmsPropertyDefinition.PROPERTY_TITLE);
            }
            resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT).setValue(navText);
            String folderCaption;
            folderCaption = CmsResourceIcon.getTreeCaptionHTML(name, resUtil, null, false);
            resourceItem.getItemProperty(CmsResourceTableProperty.PROPERTY_TREE_CAPTION).setValue(folderCaption);
            if (inNavigation) {
                if (navText == null) {
                    navText = name;
                }
                String sitemapCaption = CmsResourceIcon.getTreeCaptionHTML(navText, resUtil, null, false);
                resourceItem.getItemProperty(PROPERTY_SITEMAP_CAPTION).setValue(sitemapCaption);
            }

        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Gets the name to display for the given resource.<p>
     *
     * @param cms the CMS context
     * @param resource a resource
     * @param parentId the id of the parent of the resource
     *
     * @return the name for the given resoure
     */
    protected String getName(CmsObject cms, CmsResource resource, CmsUUID parentId) {

        return parentId == null ? resource.getRootPath() : resource.getName();
    }
}
