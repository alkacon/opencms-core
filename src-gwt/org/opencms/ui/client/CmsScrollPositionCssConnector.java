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

import org.opencms.ui.components.extensions.CmsScrollPositionCss;
import org.opencms.ui.shared.components.CmsScrollPositionCssState;

import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * This connector will manipulate the CSS classes of the extended widget depending on the scroll position.<p>
 */
@Connect(CmsScrollPositionCss.class)
public class CmsScrollPositionCssConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -9079215265941920364L;

    /** The widget to enhance. */
    private Widget m_widget;

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsScrollPositionCssState getState() {

        return (CmsScrollPositionCssState)super.getState();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // Get the extended widget
        m_widget = ((ComponentConnector)target).getWidget();
        m_widget.addDomHandler(new ScrollHandler() {

            public void onScroll(ScrollEvent event) {

                handleScroll(event);

            }
        }, ScrollEvent.getType());
    }

    /**
     * Handles the scroll event.<p>
     *
     * @param event the scroll event
     */
    protected void handleScroll(ScrollEvent event) {

        String styleName = getState().getStyleName();
        int scrollBarrier = getState().getScrollBarrier();
        int barrierMargin = getState().getBarrierMargin();
        if ((m_widget != null) && (scrollBarrier > 0) && (styleName != null)) {
            if (m_widget.getElement().getScrollTop() > (scrollBarrier + barrierMargin)) {
                m_widget.addStyleDependentName(styleName);
                m_widget.addStyleName(styleName);
            } else if (m_widget.getElement().getScrollTop() < (scrollBarrier - barrierMargin)) {
                m_widget.removeStyleDependentName(styleName);
                m_widget.removeStyleName(styleName);
            }
        }
    }
}
