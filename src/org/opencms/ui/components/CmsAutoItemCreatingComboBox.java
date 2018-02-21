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

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.ComboBox;

/**
 * A combo box which automatically creates a new option if setValue is called with an item id not already contained
 * in the data source.
 *
 * This class only supports IndexedContainer as a data source.
 *
 * TODO: Fix this so it works for the value automatically set by setPropertyDataSource()
 */
public class CmsAutoItemCreatingComboBox extends ComboBox {

    /**
     * Handles automatic creation of new values.<p>
     */
    public interface I_NewValueHandler {

        /**
         * Ensure that an item with the given id or a suitable replacement id exists.<p>
         *
         * @param cnt the container
         * @param id the id
         * @return the id actually used for the item
         */
        Object ensureItem(Container cnt, Object id);
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The caption id. */
    public static final String CAPTION_ID = "name";

    /** The handler for new values. */
    private I_NewValueHandler m_newValueHandler;

    /**
     * Creates a new instance.<p>
     */
    public CmsAutoItemCreatingComboBox() {

        super();
    }

    /**
     * @see com.vaadin.v7.ui.AbstractSelect#setContainerDataSource(com.vaadin.v7.data.Container)
     */
    @Override
    public void setContainerDataSource(Container newDataSource) {

        if (newDataSource instanceof IndexedContainer) {
            super.setContainerDataSource(newDataSource);
        } else {
            throw new IllegalArgumentException("only IndexedContainer supported as data source.");
        }
    }

    /**
     * Sets the new value handler.<p>
     *
     * @param handler the handler instance
     */
    public void setNewValueHandler(I_NewValueHandler handler) {

        m_newValueHandler = handler;
    }

    /**
     * @see com.vaadin.v7.ui.AbstractSelect#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object newValue) throws com.vaadin.v7.data.Property.ReadOnlyException {

        IndexedContainer container = (IndexedContainer)getContainerDataSource();
        if ((m_newValueHandler != null) && (newValue != null)) {
            newValue = m_newValueHandler.ensureItem(container, newValue);
        }
        super.setValue(newValue);
    }
}
