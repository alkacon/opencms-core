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

package org.opencms.ui.client;

import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.ui.components.CmsGwtContextMenuButton;
import org.opencms.ui.shared.components.CmsGwtContextMenuButtonState;
import org.opencms.ui.shared.rpc.I_CmsGwtContextMenuServerRpc;
import org.opencms.util.CmsUUID;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for using the GWT based context menu buttons as Vaadin widgets.
 */
@Connect(CmsGwtContextMenuButton.class)
public class CmsGwtContextMenuButtonConnector extends AbstractComponentConnector {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsGwtContextMenuButtonState getState() {

        return (CmsGwtContextMenuButtonState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public CmsContextMenuButton getWidget() {

        return (CmsContextMenuButton)super.getWidget();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected CmsContextMenuButton createWidget() {

        CmsGwtContextMenuButtonState state = getState();
        final I_CmsGwtContextMenuServerRpc rpc = getRpcProxy(I_CmsGwtContextMenuServerRpc.class);
        CmsContextMenuButton result = new CmsContextMenuButton(
            new CmsUUID(state.getStructureId()),
            new CmsContextMenuHandler() {

                @Override
                public void refreshResource(CmsUUID structureId) {

                    rpc.refresh("" + structureId);
                }
            },
            AdeContext.resourceinfo);
        if (state.styles != null) {
            for (String s : state.styles) {
                // onStateChanged apparently isn't called for the initial state, so set the styles manually here.
                // There may be a better way to handle this, but I haven't found one.
                result.addStyleName(s);
            }
        }
        return result;
    }

}
