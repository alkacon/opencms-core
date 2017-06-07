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

package org.opencms.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget which wraps another widget, which will only be appended to the DOM if the wrapper's
 * <code>widget()</code> method is called.<p>
 *
 * @param <W> the type of the widget to wrap
 *
 * @since 8.0.0
 */
public class CmsLazyWidgetWrapper<W extends Widget> extends Composite {

    /** The wrapper panel. */
    private FlowPanel m_panel;

    /** The wrapped widget. */
    private W m_widget;

    /**
     * Creates a new instance.<p>
     *
     * @param widget the widget to wrap
     */
    public CmsLazyWidgetWrapper(W widget) {

        m_panel = new FlowPanel();
        initWidget(m_panel);
        m_widget = widget;
    }

    /**
     * Returns the wrapped widget and attaches it to the DOM if necessary.<p>
     *
     * @return the wrapped widget
     */
    public W widget() {

        if (m_widget.getParent() == null) {
            m_panel.add(m_widget);
        }
        return m_widget;
    }

}
