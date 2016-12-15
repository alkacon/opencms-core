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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Client side widget to display an OpenCms report.<p>
 */
public class CmsClientReportWidget extends FlowPanel {

    /** The widget containing the report content HTML. */
    private HTML m_reportContent = new HTML();

    /**
     * Creates a new instance.<p>
     */
    public CmsClientReportWidget() {
        add(m_reportContent);
        addStyleName("o-report");
        m_reportContent.addStyleName("o-report-content");
    }

    /**
     * Appends the given HTML to the report content.<p>
     *
     * Also scrolls to the bottom of the
     *
     * @param html the HTML to append
     */
    public void append(String html) {

        appendHtmlInternal(m_reportContent.getElement(), html);
        scrollToBottom(getElement(), m_reportContent.getElement());

    }

    /**
     * Appends HTML to the content.<p>
     *
     * @param element the report content element
     * @param html the HTML to append
     */
    private native void appendHtmlInternal(Element element, String html) /*-{
        element.insertAdjacentHTML('beforeend', html);
    }-*/;

    /**
     * Scrolls to the bottom.<p>
     *
     * @param element the report widget element
     * @param content the report content element
     */
    private native void scrollToBottom(Element element, Element content) /*-{

        element.scrollTop = content.offsetHeight;

    }-*/;
}
