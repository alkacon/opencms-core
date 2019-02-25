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

import com.vaadin.ui.Component;
import com.vaadin.v7.ui.HorizontalLayout;

/**
 * Default implementation for row for multivalue field lists.<p>
 *
 * Wraps an input widget and a button bar for manipulating the row.<p>
 */
public class CmsEditableGroupRow extends HorizontalLayout implements I_CmsEditableGroupRow {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The text input field. */
    private Component m_component;

    /** Group of rows for a multivalue field list. */
    private CmsEditableGroup m_group;

    /** The button bar. */
    private CmsEditableGroupButtons m_buttonBar;

    /**
     * Creates a new instance.<p>
     *
     * @param group the multivalue widget group
     * @param component the wrapped input widget
     */
    public CmsEditableGroupRow(CmsEditableGroup group, Component component) {

        m_component = component;
        m_group = group;
        setWidth("100%");
        setSpacing(true);
        component.setWidth("100%");
        addComponent(component);
        setExpandRatio(component, 1f);
        m_buttonBar = new CmsEditableGroupButtons(new CmsDefaultActionHandler(group, this));
        addComponent(m_buttonBar);
    }

    /**
     * Gets the button bar.<p>
     *
     * @return the button bar
     */
    public CmsEditableGroupButtons getButtonBar() {

        return m_buttonBar;

    }

    /**
     * Returns the input field.<p>
     *
     * @return the input field
     */
    public Component getComponent() {

        return m_component;
    }

}
