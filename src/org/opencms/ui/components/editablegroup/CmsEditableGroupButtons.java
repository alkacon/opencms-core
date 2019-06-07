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

import org.opencms.ui.shared.CmsEditableGroupButtonsState;
import org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc;

import com.vaadin.ui.AbstractComponent;

/**
 * Button bar for manipulating rows in a multivalued field list.<p>
 *
 * Has buttons for moving a row up and down, deleting it, and adding a new row.
 */
public class CmsEditableGroupButtons extends AbstractComponent implements I_CmsEditableGroupButtonsServerRpc {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The action handler instance. */
    private I_CmsEditableGroupActionHandler m_handler;

    /**
     * Creates a new instance.<p>
     *
     * @param actionHandler handler which should be called for different button presses
     */
    public CmsEditableGroupButtons(I_CmsEditableGroupActionHandler actionHandler) {

        registerRpc(this, I_CmsEditableGroupButtonsServerRpc.class);
        m_handler = actionHandler;
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getState()
     */
    @Override
    public CmsEditableGroupButtonsState getState() {

        return (CmsEditableGroupButtonsState)super.getState();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc#onAdd()
     */
    public void onAdd() {

        m_handler.onAdd();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc#onDelete()
     */
    public void onDelete() {

        m_handler.onDelete();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc#onDown()
     */
    public void onDown() {

        m_handler.onDown();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc#onEdit()
     */
    public void onEdit() {

        m_handler.onEdit();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc#onUp()
     */
    public void onUp() {

        m_handler.onUp();
    }

    /**
     * Sets the 'first' and 'last' status of the button bar.<p>
     *
     * @param first true if this is the button bar of the first row
     * @param last true if this is the button bar of the last row
     * @param hideAdd true -> hide add option
     */
    public void setFirstLast(boolean first, boolean last, boolean hideAdd) {

        CmsEditableGroupButtonsState state = (CmsEditableGroupButtonsState)getState(false);
        if ((state.isFirst() != first) || (state.isLast() != last)) {
            state.setFirst(first);
            state.setLast(last);
            state.setAddOptionHidden(hideAdd);
            markAsDirty();
        }
    }

}
