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

package org.opencms.gwt.client.property;

import org.opencms.gwt.client.ui.CmsScrollPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Wrapper around a scroll panel with an extra container for displaying a resource info box.
 *
 * <p>Used in the property dialog.
 */
public class CmsScrollPanelWrapper extends Composite {

    /** The wrapped scroll panel. */
    private CmsScrollPanel m_scrollPanel = GWT.create(CmsScrollPanel.class);

    /** The list info container. */
    private FlowPanel m_listInfoContainer = new FlowPanel();

    /** The root container of the widget. */
    private FlowPanel m_content = new FlowPanel();

    /**
     * Creates a new instance.
     */
    public CmsScrollPanelWrapper() {

        initWidget(m_content);
        m_content.add(m_listInfoContainer);
        m_content.add(m_scrollPanel);
        addStyleName("scrollPanelWrapper");
    }

    /**
     * Gets the list info container.
     *
     * @return the list info container
     */
    public FlowPanel getListInfoContainer() {

        return m_listInfoContainer;
    }

    /**
     * Gets the scroll panel.
     *
     * @return the scroll panel
     */
    public CmsScrollPanel getScrollPanel() {

        return m_scrollPanel;
    }

}
