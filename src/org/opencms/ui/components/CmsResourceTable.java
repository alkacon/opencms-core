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

package org.opencms.ui.components;

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_CACHE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_CATEGORIES;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_COPYRIGHT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_MODIFIED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_RELEASED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INTERNAL_RESOURCE_TYPE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IN_NAVIGATION;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IS_FOLDER;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_POSITION;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PERMISSIONS;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_SITE_PATH;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_SIZE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TITLE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TYPE_ICON;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_LOCKED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_MODIFIED;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.util.I_CmsItemSorter;
import org.opencms.util.CmsColorContrastCalculator;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Component;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CustomComponent;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.RowHeaderMode;
import com.vaadin.v7.ui.Table.TableDragMode;

/**
 * Generic table for displaying lists of resources.<p>
 */
@SuppressWarnings("deprecation")
public class CmsResourceTable extends CustomComponent {

    /**
     * Comparator used for sorting the categories column.
     */
    public static class CategoryComparator implements Comparator<String> {

        /** The collator used. */
        private final com.ibm.icu.text.Collator m_collator = com.ibm.icu.text.Collator.getInstance(
            com.ibm.icu.util.ULocale.ROOT);

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(String c1, String c2) {

            // We want empty strings to come last, but otherwise just use case insensitive string order
            if ("".equals(c1) && "".equals(c2)) {
                return 0;
            }
            if ("".equals(c1)) {
                // "" "foo"
                return 1;
            }
            if ("".equals(c2)) {
                // "foo" ""
                return -1;
            }
            return m_collator.compare(c1, c2);
        }

    }

    /**
     * Widget for displaying a resource's categories in a table column.
     *
     * <p>For user experience reasons, this widget only loads the category data when needed, which is either when it is attached,
     * or when other operations (sorting, filtering) need it.
     */
    public static class CategoryLabel extends Composite implements Comparable<CategoryLabel> {

        /**
         * Holds the data to display for a single category.
         */
        class CategoryItem {

            /** The background color. */
            private String m_background;

            /** The title to display. */
            private String m_title;

            /** The category path. */
            private String m_categoryPath;

            /**
             * Creates a new instance.
             *
             * @param title the title
             * @param background the background color
             */
            public CategoryItem(String categoryPath, String title, String background) {

                super();
                m_categoryPath = categoryPath;
                m_title = title;
                m_background = background;
            }

            /**
             * Gets the background color.
             *
             * @return the background color
             */
            public String getBackground() {

                return m_background;
            }

            public String getCategoryPath() {

                return m_categoryPath;
            }

            /**
             * Gets the title.
             *
             * @return the title
             */
            public String getTitle() {

                return m_title;
            }
        }

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** True if the widget has been initialized. */
        private boolean m_initialized;

        /** The label used to display the categories. */
        private Label m_label = new Label();

        /** The locale. */
        private Locale m_locale;

        /** The resource utility wrapper. */
        private CmsResourceUtil m_resUtil;

        /** The categories value as a string, for tooltips, sorting and filtering. */
        private String m_value = "";

        /**
         * Creates a new instance.
         *
         * @param resUtil the resource utility wrapper
         * @param locale the workplace locale
         */
        public CategoryLabel(CmsResourceUtil resUtil, Locale locale) {

            m_locale = locale;
            m_resUtil = resUtil;
            setCompositionRoot(m_label);
            addStyleName("o-category-label");
        }

        /**
         * @see com.vaadin.ui.AbstractComponent#attach()
         */
        @Override
        public void attach() {

            // Attach is only called when the user scrolls near the row in which this widget is located.
            init();
            super.attach();
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(CategoryLabel o) {

            // getValue() calls init()
            return CATEGORY_COMPARATOR.compare(getValue(), o.getValue());

        }

        /**
         * Gets the categories as a string (for tooltips, sorting and filtering).
         *
         * @return the category values
         */
        public String getValue() {

            init();
            return m_value;
        }

        /**
         * Needed for filtering.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            // getValue() calls init
            return getValue();
        }

        /**
         * Initializes the category data and the actual widget, unless it has already been initialized.
         */
        protected synchronized void init() {

            if (!m_initialized) {
                // We definitely don't want to repeatedly try to initialize the widget, since performance is the whole point.
                // Even failure should count as being initialized. So we might as well set m_initialized right here, at the start.
                m_initialized = true;
                try {
                    CmsObject cms = m_resUtil.getCms();
                    CmsCategoryService catService = CmsCategoryService.getInstance();
                    List<CmsCategory> categories = catService.readResourceCategories(cms, m_resUtil.getResource());
                    categories = catService.localizeCategories(cms, categories, m_locale);

                    Map<CmsPath, CmsCategory> categoriesByPath = categories.stream().collect(
                        Collectors.toMap(cat -> new CmsPath(cat.getPath()), cat -> cat, (a, b) -> b));
                    Set<CmsPath> parents = categories.stream().map(
                        cat -> CmsResource.getParentFolder(cat.getPath())).filter(path -> path != null).map(
                            path -> new CmsPath(path)).collect(Collectors.toSet());

                    boolean removeParents = OpenCms.getWorkplaceManager().isExplorerCategoriesLeavesOnly();
                    boolean fullPath = OpenCms.getWorkplaceManager().isExplorerCategoriesWithPath();
                    List<CmsCategory> categoriesToDisplay = new ArrayList<>(categories);
                    if (removeParents) {
                        categoriesToDisplay.removeIf(cat -> parents.contains(new CmsPath(cat.getPath())));
                    }

                    List<CategoryItem> items = categoriesToDisplay.stream().map(
                        cat -> new CategoryItem(
                            cat.getPath(),
                            fullPath ? getCompositeCategoryTitle(categoriesByPath, cat) : cat.getTitle(),
                            cat.getBackground())).collect(Collectors.toList());
                    Comparator<CategoryItem> comparator = (
                        a,
                        b) -> ComparisonChain.start().compare(a.getCategoryPath(), b.getCategoryPath()).result();
                    Collections.sort(items, comparator);
                    // Comma-separated list of titles, for tooltip, sorting and filtering
                    m_value = items.stream().map(item -> item.getTitle()).collect(Collectors.joining(", "));
                    m_label.setDescription(m_value);

                    // Assemble HTML based on the titles and fill the widget with it.
                    String html = items.stream().flatMap(item -> {
                        String colorStyle = "";
                        String bg = item.getBackground();
                        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(bg)) {
                            bg = bg.trim();
                            String fgSuffix = "";
                            try {
                                fgSuffix = " color: " + m_contrastCalculator.getForeground(bg) + " !important;";
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                            colorStyle = " style='background-color: " + bg + " !important; " + fgSuffix + "' ";
                        }
                        return Arrays.asList(
                            "<div class='o-category-label-category' ",
                            colorStyle,
                            ">",
                            CmsEncoder.escapeXml(item.getTitle()),
                            "</div>").stream();
                    }).collect(Collectors.joining(""));

                    m_label.setContentMode(ContentMode.HTML);
                    m_label.setValue(html);

                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Helper class for easily configuring a set of columns to display, together with their visibility / collapsed status.<p>
     */
    public class ColumnBuilder {

        /** The column entries configured so far. */
        private List<ColumnEntry> m_columnEntries = Lists.newArrayList();

        /**
         * Sets up the table and its container using the columns configured so far.<p>
         */
        public void buildColumns() {

            Set<CmsResourceTableProperty> visible = new LinkedHashSet<CmsResourceTableProperty>();
            Set<CmsResourceTableProperty> collapsed = new LinkedHashSet<CmsResourceTableProperty>();
            for (ColumnEntry entry : m_columnEntries) {
                CmsResourceTableProperty prop = entry.getColumn();
                m_container.addContainerProperty(prop, prop.getColumnType(), prop.getDefaultValue());
                if (entry.isCollapsed()) {
                    collapsed.add(entry.getColumn());
                }
                if (entry.isVisible()) {
                    visible.add(entry.getColumn());
                }
            }
            m_fileTable.setVisibleColumns(visible.toArray(new Object[0]));
            Object[] collapsedColumnsArray = collapsed.toArray(new Object[0]);
            setCollapsedColumns(collapsedColumnsArray);
            for (CmsResourceTableProperty visibleProp : visible) {
                String headerKey = visibleProp.getHeaderKey();
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(headerKey)) {
                    m_fileTable.setColumnHeader(visibleProp, CmsVaadinUtils.getMessageText(headerKey));
                } else {
                    m_fileTable.setColumnHeader(visibleProp, "");
                }
                m_fileTable.setColumnCollapsible(visibleProp, visibleProp.isCollapsible());
                if (visibleProp.getColumnWidth() > 0) {
                    m_fileTable.setColumnWidth(visibleProp, visibleProp.getColumnWidth());
                }
                if (visibleProp.getExpandRatio() > 0) {
                    m_fileTable.setColumnExpandRatio(visibleProp, visibleProp.getExpandRatio());
                }
                if (visibleProp.getConverter() != null) {
                    m_fileTable.setConverter(visibleProp, visibleProp.getConverter());
                }
            }
        }

        /**
         * Adds a new column.<p>
         *
         * @param prop the column
         *
         * @return this object
         */
        public ColumnBuilder column(CmsResourceTableProperty prop) {

            column(prop, 0);
            return this;
        }

        /**
         * Adds a new column.<p<
         *
         * @param prop the column
         * @param flags the flags for the column
         *
         * @return this object
         */
        public ColumnBuilder column(CmsResourceTableProperty prop, int flags) {

            ColumnEntry entry = new ColumnEntry();
            entry.setColumn(prop);
            entry.setFlags(flags);
            m_columnEntries.add(entry);
            return this;
        }
    }

    /**
     * Contains the data for the given column, along with some flags to control visibility/collapsed status.<p>
     *
     */
    public static class ColumnEntry {

        /** The column. */
        private CmsResourceTableProperty m_column;

        /** The flags. */
        private int m_flags;

        /**
         * Returns the column.<p>
         *
         * @return the column
         */
        public CmsResourceTableProperty getColumn() {

            return m_column;
        }

        /**
         * Returns the collapsed.<p>
         *
         * @return the collapsed
         */
        public boolean isCollapsed() {

            return (m_flags & COLLAPSED) != 0;
        }

        /**
         * Returns the visible.<p>
         *
         * @return the visible
         */
        public boolean isVisible() {

            return 0 == (m_flags & INVISIBLE);
        }

        /**
         * Sets the column.<p>
         *
         * @param column the column to set
         */
        public void setColumn(CmsResourceTableProperty column) {

            m_column = column;
        }

        /**
         * Sets the flags.<p>
         *
         * @param flags the flags to set
         */
        public void setFlags(int flags) {

            m_flags = flags;
        }

        @Override
        public String toString() {

            return "ColumnEntry[" + getColumn().getId() + "," + m_flags + "]";
        }

    }

    /**
     * Interfaces for getting notified of column visibility/sort setting changes.
     */
    public interface ColumnSettingChangeHandler {

        /**
         * Called when column visibility or sorting is changed by the user.
         */
        void onColumnSettingsChanged();
    }

    /**
     * Default description generator for table entries.
     */
    public static class DefaultItemDescriptionGenerator implements ItemDescriptionGenerator {

        /** Serial version id.*/
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.v7.ui.AbstractSelect.ItemDescriptionGenerator#generateDescription(com.vaadin.ui.Component, java.lang.Object, java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public String generateDescription(Component source, Object itemId, Object propertyId) {

            Table table = (Table)source;
            try {
                if ((propertyId != null) && (itemId != null)) {
                    Property prop = table.getContainerDataSource().getItem(itemId).getItemProperty(propertyId);
                    Converter<String, Object> converter = table.getConverter(propertyId);
                    if (CmsResourceTableProperty.PROPERTY_RESOURCE_NAME == propertyId) {
                        // when working with the explorer, tool tips constantly showing up when
                        // hovering over the file name accidentally seems more annoying than useful
                        return null;
                    } else if ((converter != null) && String.class.equals(converter.getPresentationType())) {
                        return converter.convertToPresentation(
                            prop.getValue(),
                            String.class,
                            A_CmsUI.get().getLocale());
                    } else if (String.class.equals(prop.getType()) || ClassUtils.isPrimitiveOrWrapper(prop.getType())) {
                        Object value = prop.getValue();
                        if (value != null) {
                            return CmsEncoder.escapeXml("" + value);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            return null;
        }
    }

    /**
     * Provides item property values for additional table columns.<p>
     */
    public static interface I_ResourcePropertyProvider {

        /**
         * Adds the property values to the given item.<p>
         *
         * @param resourceItem the resource item
         * @param cms the cms context
         * @param resource the resource
         * @param locale  the workplace locale
         */
        void addItemProperties(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale);
    }

    /**
     * Extending the indexed container to make the number of un-filtered items available.<p>
     */
    protected static class ItemContainer extends IndexedContainer {

        /** The serial version id. */
        private static final long serialVersionUID = -2033722658471550506L;

        /**
         * @see com.vaadin.v7.data.util.IndexedContainer#getSortableContainerPropertyIds()
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
         * Returns the number of items in the container, not considering any filters.<p>
         *
         * @return the number of items
         */
        protected int getItemCount() {

            return getAllItemIds().size();
        }
    }

    /** Flag to mark columns as initially collapsed.*/
    public static final int COLLAPSED = 1;

    /** Flag to mark columns as invisible. */
    public static final int INVISIBLE = 2;

    /** Static instance of the comparator used for categories. */
    private static final CategoryComparator CATEGORY_COMPARATOR = new CategoryComparator();

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTable.class);

    /** Used for calculating foreground colors for categories. */
    private static final CmsColorContrastCalculator m_contrastCalculator = new CmsColorContrastCalculator();

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The resource data container. */
    protected ItemContainer m_container = new ItemContainer();

    /** The table used to display the resource data. */
    protected Table m_fileTable = new Table() {

        /** If greater than 0, we are in a changeVariables call - which means that column changes probably are the direct result of user interaction with the table rather than automatic/programmatic changes. */
        private long m_changingVariables;

        /**
         * @see com.vaadin.v7.ui.Table#changeVariables(java.lang.Object, java.util.Map)
         */
        public void changeVariables(Object source, java.util.Map<String, Object> variables) {

            m_changingVariables += 1;
            try {
                super.changeVariables(source, variables);
            } finally {
                m_changingVariables -= 1;
            }
        }

        /**
         * @see com.vaadin.v7.ui.Table#setColumnCollapsed(java.lang.Object, boolean)
         */
        public void setColumnCollapsed(Object propertyId, boolean collapsed) throws IllegalStateException {

            super.setColumnCollapsed(propertyId, collapsed);
            if (m_changingVariables > 0) {
                if (m_columnSettingChangeHandler != null) {
                    m_columnSettingChangeHandler.onColumnSettingsChanged();
                }
            }

        };

        /**
         * @see com.vaadin.v7.ui.Table#sort(java.lang.Object[], boolean[])
         */
        public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {

            super.sort(propertyId, ascending);
            if (m_changingVariables > 0) {
                if (m_columnSettingChangeHandler != null) {
                    m_columnSettingChangeHandler.onColumnSettingsChanged();
                }
            }
        }
    };

    /** Property provider for additional columns. */
    protected List<I_ResourcePropertyProvider> m_propertyProviders;

    /** Handles column setting changes. */
    private ColumnSettingChangeHandler m_columnSettingChangeHandler;

    /**
     * Creates a new instance.<p>
     *
     * This constructor does *not* set up the columns of the table; use the ColumnBuilder inner class for this.
     */
    public CmsResourceTable() {

        m_propertyProviders = new ArrayList<I_ResourcePropertyProvider>();
        m_fileTable.setContainerDataSource(m_container);
        setCompositionRoot(m_fileTable);
        m_fileTable.setRowHeaderMode(RowHeaderMode.HIDDEN);
        m_fileTable.setItemDescriptionGenerator(new DefaultItemDescriptionGenerator());
    }

    /**
     * Static helper method to initialize the 'standard' properties of a data item from a given resource.<p>
     * @param resourceItem the resource item to fill
     * @param cms the CMS context
     * @param resource the resource
     * @param locale the locale
     */
    public static void fillItemDefault(Item resourceItem, CmsObject cms, CmsResource resource, Locale locale) {

        if (resource == null) {
            LOG.error("Error rendering item for 'null' resource");
            return;
        }

        if (resourceItem == null) {
            LOG.error("Error rendering 'null' item for resource " + resource.getRootPath());
            return;
        }
        if (cms == null) {
            cms = A_CmsUI.getCmsObject();
            LOG.warn("CmsObject was 'null', using thread local CmsObject");
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Map<String, CmsProperty> resourceProps = null;
        try {
            List<CmsProperty> props = cms.readPropertyObjects(resource, false);
            resourceProps = new HashMap<String, CmsProperty>();
            for (CmsProperty prop : props) {
                resourceProps.put(prop.getName(), prop);
            }
        } catch (CmsException e1) {
            LOG.debug("Unable to read properties for resource '" + resource.getRootPath() + "'.", e1);
        }
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        if (resourceItem.getItemProperty(PROPERTY_TYPE_ICON) != null) {
            resourceItem.getItemProperty(PROPERTY_TYPE_ICON).setValue(
                new CmsResourceIcon(resUtil, resource.getState(), true));
        }

        if (resourceItem.getItemProperty(PROPERTY_PROJECT) != null) {
            Label projectFlag = null;
            switch (resUtil.getProjectState().getMode()) {
                case 1:
                    projectFlag = new Label(
                        new CmsCssIcon(OpenCmsTheme.ICON_PROJECT_CURRENT).getHtml(resUtil.getLockedInProjectName()),
                        ContentMode.HTML);
                    break;
                case 2:
                    projectFlag = new Label(
                        new CmsCssIcon(OpenCmsTheme.ICON_PROJECT_OTHER).getHtml(resUtil.getLockedInProjectName()),
                        ContentMode.HTML);
                    break;
                case 5:
                    projectFlag = new Label(
                        new CmsCssIcon(OpenCmsTheme.ICON_PUBLISH).getHtml(resUtil.getLockedInProjectName()),
                        ContentMode.HTML);
                    break;
                default:
            }
            resourceItem.getItemProperty(PROPERTY_PROJECT).setValue(projectFlag);
        }

        if (resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT) != null) {
            resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).setValue(Boolean.valueOf(resUtil.isInsideProject()));
        }

        if (resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED) != null) {
            resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED).setValue(
                Boolean.valueOf(resUtil.isReleasedAndNotExpired()));
        }

        if (resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME) != null) {
            resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME).setValue(resource.getName());
        }

        if (resourceItem.getItemProperty(PROPERTY_SITE_PATH) != null) {
            resourceItem.getItemProperty(PROPERTY_SITE_PATH).setValue(cms.getSitePath(resource));
        }

        if ((resourceItem.getItemProperty(PROPERTY_TITLE) != null) && (resourceProps != null)) {
            resourceItem.getItemProperty(PROPERTY_TITLE).setValue(
                resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_TITLE)
                ? resourceProps.get(CmsPropertyDefinition.PROPERTY_TITLE).getValue()
                : "");
        }
        boolean inNavigation = false;
        if ((resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT) != null) && (resourceProps != null)) {
            resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT).setValue(
                resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT)
                ? resourceProps.get(CmsPropertyDefinition.PROPERTY_NAVTEXT).getValue()
                : "");
            inNavigation = resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT);
        }

        if ((resourceItem.getItemProperty(PROPERTY_NAVIGATION_POSITION) != null) && (resourceProps != null)) {
            try {
                Float navPos = resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_NAVPOS)
                ? Float.valueOf(resourceProps.get(CmsPropertyDefinition.PROPERTY_NAVPOS).getValue())
                : (inNavigation ? Float.valueOf(Float.MAX_VALUE) : null);
                resourceItem.getItemProperty(PROPERTY_NAVIGATION_POSITION).setValue(navPos);
                inNavigation = navPos != null;
            } catch (Exception e) {
                LOG.debug("Error evaluating navPos property", e);
            }
        }

        if (resourceItem.getItemProperty(PROPERTY_IN_NAVIGATION) != null) {
            if (inNavigation
                && (resourceProps != null)
                && resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_NAVINFO)
                && CmsClientSitemapEntry.HIDDEN_NAVIGATION_ENTRY.equals(
                    resourceProps.get(CmsPropertyDefinition.PROPERTY_NAVINFO).getValue())) {
                inNavigation = false;
            }
            resourceItem.getItemProperty(PROPERTY_IN_NAVIGATION).setValue(Boolean.valueOf(inNavigation));
        }

        if ((resourceItem.getItemProperty(PROPERTY_COPYRIGHT) != null) && (resourceProps != null)) {
            resourceItem.getItemProperty(PROPERTY_COPYRIGHT).setValue(
                resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_COPYRIGHT)
                ? resourceProps.get(CmsPropertyDefinition.PROPERTY_COPYRIGHT).getValue()
                : "");
        }

        if ((resourceItem.getItemProperty(PROPERTY_CACHE) != null) && (resourceProps != null)) {
            resourceItem.getItemProperty(PROPERTY_CACHE).setValue(
                resourceProps.containsKey(CmsPropertyDefinition.PROPERTY_CACHE)
                ? resourceProps.get(CmsPropertyDefinition.PROPERTY_CACHE).getValue()
                : "");
        }

        if (resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE) != null) {
            resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE).setValue(
                CmsWorkplaceMessages.getResourceTypeName(locale, type.getTypeName()));
        }

        if (resourceItem.getItemProperty(PROPERTY_INTERNAL_RESOURCE_TYPE) != null) {
            resourceItem.getItemProperty(PROPERTY_INTERNAL_RESOURCE_TYPE).setValue(type.getTypeName());
        }

        if (resourceItem.getItemProperty(PROPERTY_IS_FOLDER) != null) {
            resourceItem.getItemProperty(PROPERTY_IS_FOLDER).setValue(Boolean.valueOf(resource.isFolder()));
        }

        if (resourceItem.getItemProperty(PROPERTY_SIZE) != null) {
            if (resource.isFile()) {
                resourceItem.getItemProperty(PROPERTY_SIZE).setValue(Integer.valueOf(resource.getLength()));
            }
        }

        if (resourceItem.getItemProperty(PROPERTY_PERMISSIONS) != null) {
            resourceItem.getItemProperty(PROPERTY_PERMISSIONS).setValue(resUtil.getPermissionString());
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED) != null) {
            resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED).setValue(Long.valueOf(resource.getDateLastModified()));
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_MODIFIED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_MODIFIED).setValue(resUtil.getUserLastModified());
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_CREATED) != null) {
            resourceItem.getItemProperty(PROPERTY_DATE_CREATED).setValue(Long.valueOf(resource.getDateCreated()));
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_CREATED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_CREATED).setValue(resUtil.getUserCreated());
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_RELEASED) != null) {
            long release = resource.getDateReleased();
            if (release != CmsResource.DATE_RELEASED_DEFAULT) {
                resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(Long.valueOf(release));
            } else {
                resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(null);
            }
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED) != null) {
            long expire = resource.getDateExpired();
            if (expire != CmsResource.DATE_EXPIRED_DEFAULT) {
                resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(Long.valueOf(expire));
            } else {
                resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(null);
            }
        }

        if (resourceItem.getItemProperty(PROPERTY_STATE_NAME) != null) {
            resourceItem.getItemProperty(PROPERTY_STATE_NAME).setValue(resUtil.getStateName());
        }

        if (resourceItem.getItemProperty(PROPERTY_STATE) != null) {
            resourceItem.getItemProperty(PROPERTY_STATE).setValue(resource.getState());
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_LOCKED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_LOCKED).setValue(resUtil.getLockedByName());
        }

        if (resourceItem.getItemProperty(PROPERTY_CATEGORIES) != null) {
            CategoryLabel l = new CategoryLabel(resUtil, locale);
            resourceItem.getItemProperty(PROPERTY_CATEGORIES).setValue(l);
        }
    }

    /**
     * Gets the CSS style name for the given resource state.<p>
     *
     * @param state the resource state
     * @return the CSS style name
     */
    public static String getStateStyle(CmsResourceState state) {

        String stateStyle = "";
        if (state != null) {
            if (state.isDeleted()) {
                stateStyle = OpenCmsTheme.STATE_DELETED;
            } else if (state.isNew()) {
                stateStyle = OpenCmsTheme.STATE_NEW;
            } else if (state.isChanged()) {
                stateStyle = OpenCmsTheme.STATE_CHANGED;
            }
        }
        return stateStyle;
    }

    /**
     * Assembles the full title of a category from the title of its parents.
     *
     * @param categories the map of applicable categories by path
     * @param category the category for which to build the title
     *
     * @return the combined title
     */
    private static String getCompositeCategoryTitle(Map<CmsPath, CmsCategory> categories, CmsCategory category) {

        ArrayList<String> components = new ArrayList<>();
        CmsCategory currentCategory = category;
        while (currentCategory != null) {
            components.add(currentCategory.getTitle());
            CmsPath parentPath = new CmsPath(CmsResource.getParentFolder(currentCategory.getPath()));
            currentCategory = categories.get(parentPath);
        }
        // The while loop iterated "up" the category tree, we want the category titles in "down" direction
        Collections.reverse(components);
        return Joiner.on(" / ").join(components);
    }

    /**
     * Adds a property provider.<p>
     *
     * @param provider the property provider
     */
    public void addPropertyProvider(I_ResourcePropertyProvider provider) {

        m_propertyProviders.add(provider);
    }

    /**
     * Clears the value selection.<p>
     */
    public void clearSelection() {

        m_fileTable.setValue(Collections.emptySet());
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources) {

        fillTable(cms, resources, true);
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     * @param clearFilter <code>true</code> to clear the search filter
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources, boolean clearFilter) {

        fillTable(cms, resources, clearFilter, true);
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     * @param clearFilter <code>true</code> to clear the search filter
     * @param sort <code>true</code> to sort the table entries
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources, boolean clearFilter, boolean sort) {

        fillTable(cms, resources, clearFilter, true, false);
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     * @param clearFilter <code>true</code> to clear the search filter
     * @param sort <code>true</code> to sort the table entries
     * @param distinctResources whether to only show distinct resources
     */
    public void fillTable(
        CmsObject cms,
        List<CmsResource> resources,
        boolean clearFilter,
        boolean sort,
        boolean distinctResources) {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        m_container.removeAllItems();
        if (clearFilter) {
            m_container.removeAllContainerFilters();
        }
        if (distinctResources) {
            Map<String, String> ids = new HashMap<String, String>();
            for (CmsResource resource : resources) {
                String id = resource.getStructureId().getStringValue();
                if (!ids.containsKey(id)) {
                    fillItem(cms, resource, wpLocale);
                    ids.put(id, "");
                }
            }
        } else {
            for (CmsResource resource : resources) {
                fillItem(cms, resource, wpLocale);
            }
        }
        if (sort) {
            m_fileTable.sort();
        }
        clearSelection();
    }

    /**
     * Gets structure ids of resources for current folder in current sort order.<p>
     *
     * @return the structure ids of the current folder contents
     */
    @SuppressWarnings("unchecked")
    public List<CmsUUID> getAllIds() {

        return itemIdsToUUIDs((List<String>)m_fileTable.getContainerDataSource().getItemIds());
    }

    /**
     * Returns the number of currently visible items.<p>
     *
     * @return the number of currentliy visible items
     */
    public int getItemCount() {

        return m_container.getItemCount();
    }

    /**
     * Returns the structure id to the given string item id.<p>
     *
     * @param itemId the item id
     *
     * @return the structure id
     */
    public CmsUUID getUUIDFromItemID(String itemId) {

        return new CmsUUID(itemId);
    }

    /**
     * Returns if the column with the given property id is visible and not collapsed.<p>
     *
     * @param propertyId the property id
     *
     * @return <code>true</code> if the column is visible
     */
    public boolean isColumnVisible(CmsResourceTableProperty propertyId) {

        return Arrays.asList(m_fileTable.getVisibleColumns()).contains(propertyId)
            && !m_fileTable.isColumnCollapsed(propertyId);
    }

    /**
     * Removes a property provider.<p>
     *
     * @param provider the provider to remove
     */
    public void removePropertyProvider(I_ResourcePropertyProvider provider) {

        m_propertyProviders.remove(provider);
    }

    /**
     * Selects all resources.<p>
     */
    public void selectAll() {

        m_fileTable.setValue(m_fileTable.getItemIds());
    }

    /**
     * Sets the list of collapsed columns.<p>
     *
     * @param collapsedColumns the list of collapsed columns
     */
    public void setCollapsedColumns(Object... collapsedColumns) {

        Set<Object> collapsedSet = Sets.newHashSet();
        for (Object collapsed : collapsedColumns) {
            collapsedSet.add(collapsed);
        }
        for (Object key : m_fileTable.getVisibleColumns()) {
            boolean isCollapsed = collapsedSet.contains(key);
            internalSetColumnCollapsed(key, isCollapsed);
        }
    }

    /**
     * Sets the column setting change handler.
     * @param columnSettingChangeHandler the handler instance
     */
    public void setColumnSettingChangeHandler(ColumnSettingChangeHandler columnSettingChangeHandler) {

        m_columnSettingChangeHandler = columnSettingChangeHandler;
    }

    /**
     * Sets the table drag mode.<p>
     *
     * @param dragMode the drag mode
     */
    public void setDragMode(TableDragMode dragMode) {

        m_fileTable.setDragMode(dragMode);
    }

    /**
     * Sets the table drop handler.<p>
     *
     * @param handler the drop handler
     */
    public void setDropHandler(DropHandler handler) {

        m_fileTable.setDropHandler(handler);
    }

    /**
     * Selects an given object in table.<p>
     *
     * @param o object to be selected.
     */
    public void setValue(Set<String> o) {

        m_fileTable.setValue(o);
    }

    /**
     * Fills the file item data.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param locale the workplace locale
     */
    protected void fillItem(CmsObject cms, CmsResource resource, Locale locale) {

        Item resourceItem = m_container.getItem(resource.getStructureId().toString());
        if (resourceItem == null) {
            resourceItem = m_container.addItem(resource.getStructureId().toString());
        }
        fillItemDefault(resourceItem, cms, resource, locale);
        for (I_ResourcePropertyProvider provider : m_propertyProviders) {
            provider.addItemProperties(resourceItem, cms, resource, locale);
        }
    }

    protected void internalSetColumnCollapsed(Object key, boolean collapsed) {

        m_fileTable.setColumnCollapsed(key, collapsed);
    }

    /**
     * Transforms the given item ids into UUIDs.<p>
     *
     * @param itemIds the item ids
     *
     * @return the UUIDs
     */
    protected List<CmsUUID> itemIdsToUUIDs(Collection<String> itemIds) {

        List<CmsUUID> ids = new ArrayList<CmsUUID>();
        for (String itemId : itemIds) {
            if (itemId != null) {
                ids.add(getUUIDFromItemID(itemId));
            }
        }
        return ids;
    }
}
