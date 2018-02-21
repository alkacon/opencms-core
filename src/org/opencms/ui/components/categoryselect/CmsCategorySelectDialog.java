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

import org.opencms.relations.CmsCategory;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.fileselect.I_CmsSelectionHandler;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The category select dialog.<p>
 */
public class CmsCategorySelectDialog extends CmsBasicDialog {

    /** The serial version id. */
    private static final long serialVersionUID = 247205018045790127L;

    /** The category filter field. */
    private TextField m_filter;

    /** the OK button. */
    private Button m_okButton;

    /** The selection handlers. */
    private List<I_CmsSelectionHandler<Collection<CmsCategory>>> m_selectionHandlers;

    /** The category tree. */
    private CmsCategoryTree m_tree;

    /**
     * Constructor.<p>
     *
     * @param contextPath the context path to read the categories from
     */
    public CmsCategorySelectDialog(String contextPath) {
        m_selectionHandlers = new ArrayList<I_CmsSelectionHandler<Collection<CmsCategory>>>();
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_filter.setIcon(FontOpenCms.FILTER);
        m_filter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_filter.setWidth("200px");
        m_filter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                filterTree(event.getText());

            }
        });
        m_tree.loadCategories(A_CmsUI.getCmsObject(), contextPath);
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onOk();
            }
        });

    }

    /**
     * Adds a selection handler.<p>
     *
     * @param selectionHandler the selection handler to add
     */
    public void addSelectionHandler(I_CmsSelectionHandler<Collection<CmsCategory>> selectionHandler) {

        m_selectionHandlers.add(selectionHandler);
    }

    /**
     * Removes a selection handler.<p>
     *
     * @param selectionHandler the selection handler to remove
     */
    public void removeSelectionHandler(I_CmsSelectionHandler<Collection<CmsCategory>> selectionHandler) {

        m_selectionHandlers.remove(selectionHandler);
    }

    /**
     * Sets the selected categories.<p>
     *
     * @param categories the categories to select
     */
    public void setSelectedCategories(Collection<CmsCategory> categories) {

        m_tree.setSelectedCategories(categories);
    }

    /**
     * Adds a filter to the category tree container.<p>
     *
     * @param filter the filter to add
     */
    void filterTree(String filter) {

        HierarchicalContainer container = (HierarchicalContainer)m_tree.getContainerDataSource();
        container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            final String lowerCaseFilter = filter.toLowerCase();
            container.addContainerFilter(new Filter() {

                private static final long serialVersionUID = 1L;

                public boolean appliesToProperty(Object propertyId) {

                    return true;
                }

                public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

                    CmsCategory cat = (CmsCategory)itemId;

                    return cat.getPath().toLowerCase().contains(lowerCaseFilter)
                        || ((cat.getTitle() != null) && cat.getTitle().toLowerCase().contains(lowerCaseFilter));
                }
            });
        }
    }

    /**
     * On OK click.<p>
     */
    void onOk() {

        if (!m_selectionHandlers.isEmpty()) {
            Collection<CmsCategory> categories = m_tree.getSelectedCategories();
            for (I_CmsSelectionHandler<Collection<CmsCategory>> handler : m_selectionHandlers) {
                handler.onSelection(categories);
            }
        }
    }
}
