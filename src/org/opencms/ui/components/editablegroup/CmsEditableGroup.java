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

package org.opencms.ui.components.editablegroup;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * Manages a group of widgets used as a multivalue input.<p>
 *
 * This class is not itself a widget, it just coordinates the other widgets actually used to display the multivalue widget group.
 */
public class CmsEditableGroup implements I_CmsEditableGroup {

    /** The container in which to render the individual rows of the multivalue widget group. */
    private AbstractOrderedLayout m_container;

    /** Factory for creating new input fields. */
    private Supplier<Component> m_factory;

    /** Button to add a new row to an empty list. */
    private Button m_addButton;

    /**
     * Creates a new instance.<p>
     *
     * @param container the container in which to render the individual rows
     * @param componentFactory the factory used to create new input fields
     * @param addButtonCaption the caption for the button which is used to add a new row to an empty list
     */
    public CmsEditableGroup(
        AbstractOrderedLayout container,
        Supplier<Component> componentFactory,
        String addButtonCaption) {
        m_container = container;
        m_factory = componentFactory;
        m_addButton = new Button(addButtonCaption);
        m_addButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                addRow(m_factory.get());
            }
        });
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#addRow(com.vaadin.ui.Component)
     */
    public void addRow(Component component) {

        Component actualComponent = component == null ? m_factory.get() : component;
        CmsEditableGroupRow row = new CmsEditableGroupRow(this, actualComponent);
        m_container.addComponent(row);
        updateAddButton();
        updateButtonBars();
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#addRowAfter(org.opencms.ui.components.editablegroup.CmsEditableGroupRow)
     */
    public void addRowAfter(CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index >= 0) {
            Component component = m_factory.get();
            CmsEditableGroupRow newRow = new CmsEditableGroupRow(this, component);
            m_container.addComponent(newRow, index + 1);
        }
        updateAddButton();
        updateButtonBars();

    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#getRows()
     */
    public List<CmsEditableGroupRow> getRows() {

        List<CmsEditableGroupRow> result = Lists.newArrayList();
        for (Component component : m_container) {
            if (component instanceof CmsEditableGroupRow) {
                result.add((CmsEditableGroupRow)component);
            }
        }
        return result;
    }

    /**
     * Initializes the multivalue group.<p>
     */
    public void init() {

        m_container.removeAllComponents();
        m_container.addComponent(m_addButton);
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#moveDown(org.opencms.ui.components.editablegroup.CmsEditableGroupRow)
     */
    public void moveDown(CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if ((index >= 0) && (index < (m_container.getComponentCount() - 1))) {
            m_container.removeComponent(row);
            m_container.addComponent(row, index + 1);
        }
        updateButtonBars();
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#moveUp(org.opencms.ui.components.editablegroup.CmsEditableGroupRow)
     */
    public void moveUp(CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index > 0) {
            m_container.removeComponent(row);
            m_container.addComponent(row, index - 1);
        }
        updateButtonBars();
    }

    /**
     * @see org.opencms.ui.components.editablegroup.I_CmsEditableGroup#remove(org.opencms.ui.components.editablegroup.CmsEditableGroupRow)
     */
    public void remove(CmsEditableGroupRow row) {

        int index = m_container.getComponentIndex(row);
        if (index >= 0) {
            m_container.removeComponent(row);
            if (m_container.getComponentCount() == 0) {
                m_container.addComponent(getAddButton());
            }
        }
        updateAddButton();
        updateButtonBars();
    }

    /**
     * Gets the 'add' button.<p>
     *
     * @return the add button
     */
    private Component getAddButton() {

        return m_addButton;
    }

    /**
     * Updates the button visibility.<p>
     */
    private void updateAddButton() {

        if (getRows().size() == 0) {
            m_container.addComponent(m_addButton);
        } else {
            m_container.removeComponent(m_addButton);
        }

    }

    /**
     * Updates the button bars.<p>
     */
    private void updateButtonBars() {

        List<CmsEditableGroupRow> rows = getRows();
        int i = 0;
        for (CmsEditableGroupRow row : rows) {
            boolean first = i == 0;
            boolean last = i == (rows.size() - 1);
            row.getButtonBar().setFirstLast(first, last);
            i += 1;
        }
    }

}
