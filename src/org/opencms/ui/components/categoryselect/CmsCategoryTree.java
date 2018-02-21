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

package org.opencms.ui.components.categoryselect;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TreeTable;

/**
 * The category tree.<p>
 */
public class CmsCategoryTree extends TreeTable {

    /** The checkbox column. */
    private static final String CHECKBOX = "checkbox";

    /** The path column. */
    private static final String PATH = "path";

    /** The serial version id. */
    private static final long serialVersionUID = 8046665824980274707L;

    /** The title column. */
    private static final String TITLE = "title";

    /** The check box map by category. */
    Map<CmsCategory, CheckBox> m_checkboxes;

    /** The data source container. */
    private HierarchicalContainer m_container;

    /** In case the display mode is set. */
    private boolean m_isDiplayOny;

    /**
     * Constructor.<p>
     */
    public CmsCategoryTree() {
        m_checkboxes = new HashMap<CmsCategory, CheckBox>();
        m_container = new HierarchicalContainer();
        setContainerDataSource(m_container);
        ColumnGenerator captionGenerator = new ColumnGenerator() {

            private static final long serialVersionUID = 1L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                Object result = null;
                if (TITLE.equals(columnId)) {
                    result = ((CmsCategory)itemId).getTitle();
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly((String)result)) {
                        result = ((CmsCategory)itemId).getName();
                    }
                } else if (PATH.equals(columnId)) {
                    result = ((CmsCategory)itemId).getPath();
                } else if (CHECKBOX.equals(columnId)) {
                    result = m_checkboxes.get(itemId);
                }
                return result;
            }
        };
        addGeneratedColumn(TITLE, captionGenerator);
        addGeneratedColumn(PATH, captionGenerator);
        addGeneratedColumn(CHECKBOX, captionGenerator);
        setVisibleColumns(CHECKBOX, TITLE, PATH);
        setColumnWidth(CHECKBOX, 40);
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        setItemCaptionPropertyId(TITLE);
    }

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param contextPath the context path
     */
    public CmsCategoryTree(CmsObject cms, String contextPath) {
        this();
        loadCategories(cms, contextPath);
    }

    /**
     * Returns the selected categories.<p>
     *
     * @return the selected categories
     */
    @SuppressWarnings("unchecked")
    public Collection<CmsCategory> getSelectedCategories() {

        Set<CmsCategory> result = new HashSet<CmsCategory>();
        if (m_isDiplayOny) {
            // in case of display only, all displayed categories are assume selected
            result.addAll((Collection<? extends CmsCategory>)getItemIds());
        } else {
            for (Entry<CmsCategory, CheckBox> entry : m_checkboxes.entrySet()) {
                if (entry.getValue().getValue().booleanValue()) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }

    /**
     * Fills the category tree.<p>
     *
     * @param categories the categories
     */
    public void setCategories(List<CmsCategory> categories) {

        for (int i = 0; i < categories.size(); i++) {
            CmsCategory cat = categories.get(i);
            m_container.addItem(cat);
            m_checkboxes.put(cat, new CheckBox());
            String parentPath = CmsResource.getParentFolder(cat.getPath());
            if (parentPath.length() > 1) {
                for (int j = i - 1; j >= 0; j--) {
                    if (categories.get(j).getPath().equals(parentPath)) {
                        m_container.setParent(cat, categories.get(j));
                        break;
                    }
                }
            }
        }
        // hide openers
        for (CmsCategory cat : categories) {
            if ((m_container.getChildren(cat) == null) || (m_container.getChildren(cat).size() == 0)) {
                m_container.setChildrenAllowed(cat, false);
            }
        }
    }

    /**
     * Sets the display mode.<p>
     *
     * @param displayOnly <code>true</code> to display categories only, not selectable with a hidden check box
     */
    public void setDisplayOnly(boolean displayOnly) {

        m_isDiplayOny = displayOnly;
        if (displayOnly) {
            setVisibleColumns(TITLE, PATH);
        } else {
            setVisibleColumns(CHECKBOX, TITLE, PATH);
        }
    }

    /**
     * Sets the selected categories.<p>
     *
     * @param categories the categories to select
     */
    public void setSelectedCategories(Collection<CmsCategory> categories) {

        for (Entry<CmsCategory, CheckBox> entry : m_checkboxes.entrySet()) {
            entry.getValue().setValue(Boolean.valueOf(categories.contains(entry.getKey())));
            CmsCategory parentCat = (CmsCategory)getParent(entry.getKey());
            if (parentCat != null) {
                setCollapsed(parentCat, false);
            }
        }
    }

    /**
     * Loads the categories for the given context path.<p>
     *
     * @param cms the cms context
     * @param contextPath the context path
     */
    void loadCategories(CmsObject cms, String contextPath) {

        m_checkboxes.clear();
        m_container.removeAllItems();

        List<CmsCategory> categories;
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsCategoryService catService = CmsCategoryService.getInstance();
        // get the categories
        try {
            categories = catService.readCategories(cms, "", true, contextPath);

            categories = catService.localizeCategories(cms, categories, wpLocale);
            setCategories(categories);
        } catch (CmsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
