/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Tree;

/**
 * Tree subclass used to display VFS resource trees.<p>
 */
public class CmsResourceTree extends Tree {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTree.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The CMS context. */
    private CmsObject m_cms;

    /** The resource filter. */
    private CmsResourceFilter m_filter;

    /** The list of selection handlers. */
    private List<I_CmsSelectionHandler<CmsResource>> m_resourceSelectionHandlers = Lists.newArrayList();

    /** The root resource. */
    private CmsResource m_root;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @param filter the resource filter
     */
    public CmsResourceTree(CmsObject cms, CmsResource root, CmsResourceFilter filter) {

        this(
            cms,
            root,
            filter,
            new CmsResourceTreeContainer(),
            CmsResourceTableProperty.PROPERTY_TYPE_ICON,
            CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
    }

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @param filter the resource filter
     * @param container the data container for the tree
     * @param iconId the property id for the icon
     * @param captionId the property id for the caption
     */
    public CmsResourceTree(
        CmsObject cms,
        CmsResource root,
        CmsResourceFilter filter,
        CmsResourceTreeContainer container,
        Object iconId,
        Object captionId) {
        m_cms = cms;
        m_root = root;
        m_filter = filter;
        setContainerDataSource(container);
        setItemIconPropertyId(iconId);
        setItemCaptionPropertyId(captionId);
        addExpandListener(new ExpandListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void nodeExpand(ExpandEvent event) {

                getTreeContainer().readTreeLevel(m_cms, (CmsUUID)event.getItemId(), m_filter);
                markAsDirtyRecursive(); // required so open / close arrows on folders without contents are rendered correctly
            }
        });

        addCollapseListener(new CollapseListener() {

            private static final long serialVersionUID = 1L;

            public void nodeCollapse(CollapseEvent event) {

                getTreeContainer().removeChildren((CmsUUID)event.getItemId());
            }
        });

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                CmsResource resource = CmsResourceTreeContainer.getResource(event.getItem());
                handleSelection(resource);
            }
        });

        setItemStyleGenerator(new ItemStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Tree source, Object itemId) {

                return CmsFileTable.getStateStyle(source.getContainerDataSource().getItem(itemId));
            }
        });

        getTreeContainer().addTreeItem(cms, m_root, null);
        try {
            expandItem(m_root.getStructureId());
            markAsDirtyRecursive();

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Adds a resoure selection handler.<p>
     *
     * @param handler the resource selection handler
     */
    public void addResourceSelectionHandler(I_CmsSelectionHandler<CmsResource> handler) {

        m_resourceSelectionHandlers.add(handler);
    }

    /**
     * Gets the tree container.<p>
     *
     * @return the tree container
     */
    public CmsResourceTreeContainer getTreeContainer() {

        return (CmsResourceTreeContainer)getContainerDataSource();
    }

    /**
     * Removes the given resource selection handler.<p>
     *
     * @param handler the resource selection handler
     */
    public void removeResourceSelectionHandler(I_CmsSelectionHandler<CmsResource> handler) {

        m_resourceSelectionHandlers.remove(handler);
    }

    /**
     * Handles resource selection.<p>
     *
     * @param resource the selected resource
     */
    void handleSelection(CmsResource resource) {

        for (I_CmsSelectionHandler<CmsResource> handler : m_resourceSelectionHandlers) {
            handler.onSelection(resource);
        }
    }
}
