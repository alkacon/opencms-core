/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.client.editablegroup;

import org.opencms.ui.components.editablegroup.CmsEditableGroupButtons;
import org.opencms.ui.shared.CmsEditableGroupButtonsState;
import org.opencms.ui.shared.rpc.I_CmsEditableGroupButtonsServerRpc;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for edit buttons for multivalued fields.<p>
 */
@Connect(CmsEditableGroupButtons.class)
public class CmsEditableGroupButtonsConnector extends AbstractComponentConnector {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     */
    public CmsEditableGroupButtonsConnector() {

        super();
    }

    /**
     * Gets the RPC proxy object.<p>
     *
     * @return the RPC proxy object
     */
    public I_CmsEditableGroupButtonsServerRpc getRpc() {

        return getRpcProxy(I_CmsEditableGroupButtonsServerRpc.class);
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsEditableGroupButtonsState getState() {

        return (CmsEditableGroupButtonsState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public CmsClientEditableGroupButtons getWidget() {

        return (CmsClientEditableGroupButtons)(super.getWidget());
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);
        getWidget().setFirst(getState().isFirst());
        getWidget().setLast(getState().isLast());
        getWidget().setHideAdd(getState().isAddOptionHidden());
        getWidget().setEditVisible(getState().isEditEnabled());

    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected CmsClientEditableGroupButtons createWidget() {

        return new CmsClientEditableGroupButtons(this);
    }

}
