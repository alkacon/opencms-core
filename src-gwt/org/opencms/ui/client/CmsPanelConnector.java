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

import org.opencms.ui.components.CmsPanel;

import com.google.gwt.user.client.Timer;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.panel.PanelConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for the CmsPanel widget.<p>
 *
 * Contains a workaround for a glitch where the scroll position is sometimes reset after an RPC call.<p>
 */
@Connect(CmsPanel.class)
public class CmsPanelConnector extends PanelConnector {

    /** Version id. */
    private static final long serialVersionUID = 1L;

    /** Saved scrollTop value. */
    Integer m_scrollTop;

    /**
     * @see com.vaadin.client.ui.panel.PanelConnector#postLayout()
     */
    @Override
    public void postLayout() {

        super.postLayout();

        if (m_scrollTop != null) {
            final int newScrollTop = m_scrollTop.intValue();
            Timer timer = new Timer() {

                @Override
                public void run() {

                    getWidget().contentNode.setScrollTop(newScrollTop);
                    m_scrollTop = null;
                }
            };
            timer.schedule(50);
        }
    }

    /**
     * @see com.vaadin.client.ui.panel.PanelConnector#updateFromUIDL(com.vaadin.client.UIDL, com.vaadin.client.ApplicationConnection)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        super.updateFromUIDL(uidl, client);
        if (isRealUpdate(uidl)) {
            m_scrollTop = Integer.valueOf(getState().scrollTop);
        }
    }

}
