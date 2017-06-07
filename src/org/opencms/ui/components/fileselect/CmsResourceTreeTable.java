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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsFileTable;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.util.I_CmsItemSorter;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Tree subclass used to display VFS resource trees.<p>
 */
public class CmsResourceTreeTable extends TreeTable {

    /**
     * Extends the default sorting to differentiate between files and folder when sorting by name.<p>
     * Also allows sorting by navPos property for the Resource icon column.<p>
     */
    public static class FileSorter extends DefaultItemSorter implements I_CmsItemSorter {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see org.opencms.ui.util.I_CmsItemSorter#getSortableContainerPropertyIds(com.vaadin.data.Container)
         */
        public Collection<?> getSortableContainerPropertyIds(Container container) {

            Set<Object> result = new HashSet<Object>();
            result.add(CAPTION_FOLDERS);
            result.add(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT);
            return result;
        }

        /**
         * @see com.vaadin.data.util.DefaultItemSorter#compareProperty(java.lang.Object, boolean, com.vaadin.data.Item, com.vaadin.data.Item)
         */
        @Override
        protected int compareProperty(Object propertyId, boolean sortDirection, Item item1, Item item2) {

            if (CAPTION_FOLDERS.equals(propertyId)) {
                Boolean isFolder1 = (Boolean)item1.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                Boolean isFolder2 = (Boolean)item2.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_IS_FOLDER).getValue();
                String name1 = (String)(item1.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).getValue());
                name1 = CmsFileUtil.removeTrailingSeparator(name1);
                String name2 = (String)(item2.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_RESOURCE_NAME).getValue());
                name2 = CmsFileUtil.removeTrailingSeparator(name2);
                return (sortDirection ? 1 : -1)
                    * ComparisonChain.start().compareTrueFirst(
                        isFolder1.booleanValue(),
                        isFolder2.booleanValue()).compare(name1, name2).result();
            } else if (CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.equals(propertyId)
                && (item1.getItemProperty(CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION) != null)) {
                int result;
                Float pos1 = (Float)item1.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION).getValue();
                Float pos2 = (Float)item2.getItemProperty(
                    CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION).getValue();
                if (pos1 == null) {
                    result = pos2 == null
                    ? compareProperty(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME, true, item1, item2)
                    : 1;
                } else {
                    result = pos2 == null ? -1 : Float.compare(pos1.floatValue(), pos2.floatValue());
                }
                if (!sortDirection) {
                    result = result * (-1);
                }
                return result;
            }
            return super.compareProperty(propertyId, sortDirection, item1, item2);
        }
    }

    /**
     * The style generator.<p>
     */
    public class StyleGenerator implements CellStyleGenerator {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.ui.Table.CellStyleGenerator#getStyle(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public String getStyle(Table source, Object itemId, Object propertyId) {

            Item item = source.getContainerDataSource().getItem(itemId);
            String style = CmsFileTable.getStateStyle(item);
            if (!isSelectable(item)) {
                style += " " + OpenCmsTheme.DISABLED;
            }
            if (CAPTION_FOLDERS.equals(propertyId)) {
                style += " o-name-column";
            } else if (CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT == propertyId) {
                if ((item.getItemProperty(CmsResourceTableProperty.PROPERTY_IN_NAVIGATION) != null)
                    && ((Boolean)item.getItemProperty(
                        CmsResourceTableProperty.PROPERTY_IN_NAVIGATION).getValue()).booleanValue()) {
                    style += " " + OpenCmsTheme.IN_NAVIGATION;
                }
            }
            return style;
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTreeTable.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The folder caption column id. */
    public static final String CAPTION_FOLDERS = "CAPTION_FOLDERS";

    /** The CMS context. */
    CmsObject m_cms;

    /** The list of selection handlers. */
    private List<I_CmsSelectionHandler<CmsResource>> m_resourceSelectionHandlers = Lists.newArrayList();

    /** The root resource. */
    private CmsResource m_root;

    /**
     * Predicate which can be used to prevent selection of a node (this means the select handler will not be called
     * if this returns false, but the Vaadin selection mechanism will still mark it as selected.
     */
    private Predicate<Item> m_selectionFilter = Predicates.alwaysTrue();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @param filter the resource filter
     */
    public CmsResourceTreeTable(CmsObject cms, CmsResource root, CmsResourceFilter filter) {

        this(cms, root, new CmsResourceTreeContainer(filter));
    }

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @param container the data container for the tree
     */
    public CmsResourceTreeTable(CmsObject cms, CmsResource root, CmsResourceTreeContainer container) {
        m_cms = cms;
        m_root = root;
        setContainerDataSource(container);
        container.setItemSorter(new FileSorter());
        ColumnGenerator captionGenerator = new ColumnGenerator() {

            private static final long serialVersionUID = 1L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                if (CAPTION_FOLDERS.equals(columnId)) {
                    String html = (String)source.getContainerDataSource().getItem(itemId).getItemProperty(
                        CmsResourceTableProperty.PROPERTY_TREE_CAPTION).getValue();
                    Label label = new Label(html, ContentMode.HTML);
                    label.setStyleName("o-tree-table-caption");
                    return label;
                } else {
                    return null;
                }
            }
        };
        addGeneratedColumn(CAPTION_FOLDERS, captionGenerator);
        setVisibleColumns(CAPTION_FOLDERS, CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT);
        setItemCaptionPropertyId(CAPTION_FOLDERS);
        setColumnHeader(
            CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT,
            CmsVaadinUtils.getMessageText(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT.getHeaderKey()));
        setColumnHeader(
            CAPTION_FOLDERS,
            CmsVaadinUtils.getMessageText(CmsResourceTableProperty.PROPERTY_RESOURCE_NAME.getHeaderKey()));

        // hide vertical and horizontal lines and disable alternating row background
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);

        addExpandListener(new ExpandListener() {

            private static final long serialVersionUID = 1L;

            public void nodeExpand(ExpandEvent event) {

                getTreeContainer().readTreeLevel(m_cms, (CmsUUID)event.getItemId());
                getTreeContainer().updateSort();
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

                if (isSelectable(event.getItem())) {
                    CmsResource resource = (CmsResource)(event.getItem().getItemProperty(
                        CmsResourceTreeContainer.PROPERTY_RESOURCE).getValue());
                    handleSelection(resource);
                }
            }
        });
        setCellStyleGenerator(new StyleGenerator());

        getTreeContainer().addTreeItem(cms, m_root, null);
        try {
            setCollapsed(m_root.getStructureId(), false);
            markAsDirtyRecursive();

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Adds a resource selection handler.<p>
     *
     * @param handler the resource selection handler
     */
    public void addResourceSelectionHandler(I_CmsSelectionHandler<CmsResource> handler) {

        m_resourceSelectionHandlers.add(handler);
    }

    /**
     * Expands the item with the given id.<p>
     *
     * @param itemId the item id
     */
    public void expandItem(CmsUUID itemId) {

        setCollapsed(itemId, false);
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
     * Sets the selection filter, a predicate that, by returning false for an item, can veto a tree entry selection.<p>
     *
     * @param selectionFilter the selection filter
     */
    public void setSelectionFilter(Predicate<Item> selectionFilter) {

        m_selectionFilter = selectionFilter;
    }

    /**
     * Shows the sitemap view.<p>
     *
     * @param showSitemap <code>true</code> to show the sitemap view
     */
    public void showSitemapView(boolean showSitemap) {

        if (showSitemap) {
            setSortContainerPropertyId(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT);
            setSortAscending(true);
        } else {
            setSortContainerPropertyId(CAPTION_FOLDERS);
            setSortAscending(true);
        }
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

    /**
     * Returns whether the given item is selectable.<p>
     *
     * @param item the item to check
     *
     * @return <code>true</code> if the given item is selectable
     */
    boolean isSelectable(Item item) {

        return m_selectionFilter.apply(item);
    }
}
