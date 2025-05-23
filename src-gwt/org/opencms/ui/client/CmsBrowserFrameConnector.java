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

package org.opencms.ui.client;

import org.opencms.ui.shared.CmsBrowserFrameState;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.AbstractEmbeddedState;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for the CmsBrowserFrame component.<p>
 */
@Connect(org.opencms.ui.components.CmsBrowserFrame.class)
public class CmsBrowserFrameConnector extends AbstractComponentConnector {

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsBrowserFrameState getState() {

        return (CmsBrowserFrameState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public CmsVBrowserFrame getWidget() {

        return (CmsVBrowserFrame)super.getWidget();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);

        getWidget().setAlternateText(getState().alternateText);
        String name = getState().getName();
        if (name == null) {
            name = getConnectorId();
        }
        getWidget().setName(name);
        getWidget().setSource(getResourceUrl(AbstractEmbeddedState.SOURCE_RESOURCE));

    }

}