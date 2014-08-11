/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Inline form parent widget.<p>
 * Use to wrap any HTML element of the DOM as the parent of an inline form.<p>
 */
public class CmsFormParent extends ComplexPanel implements I_CmsInlineFormParent {

    /** The wrapped widget. This will be a @link com.google.gwt.user.client.RootPanel. */
    private Widget m_widget;

    /**
     * Constructor.<p>
     * 
     * @param rootPanel the root panel to wrap
     */
    public CmsFormParent(RootPanel rootPanel) {

        initWidget(rootPanel);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsInlineFormParent#adoptWidget(com.google.gwt.user.client.ui.IsWidget)
     */
    public void adoptWidget(IsWidget widget) {

        getChildren().add(widget.asWidget());
        adopt(widget.asWidget());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#isAttached()
     */
    @Override
    public boolean isAttached() {

        if (m_widget != null) {
            return m_widget.isAttached();
        }
        return false;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // Fire any handler added to the composite itself.
        super.onBrowserEvent(event);

        // Delegate events to the widget.
        m_widget.onBrowserEvent(event);
    }

    /**
     * @see org.opencms.acacia.client.I_CmsInlineFormParent#replaceHtml(java.lang.String)
     */
    public void replaceHtml(String html) {

        // detach all children first
        while (getChildren().size() > 0) {
            getChildren().get(getChildren().size() - 1).removeFromParent();
        }
        Element tempDiv = DOM.createDiv();
        tempDiv.setInnerHTML(html);
        getElement().setInnerHTML(tempDiv.getFirstChildElement().getInnerHTML());
    }

    /**
     * Provides subclasses access to the topmost widget that defines this
     * panel.
     * 
     * @return the widget
     */
    protected Widget getWidget() {

        return m_widget;
    }

    /**
     * Sets the widget to be wrapped by the composite. The wrapped widget must be
     * set before calling any {@link Widget} methods on this object, or adding it
     * to a panel. This method may only be called once for a given composite.
     * 
     * @param widget the widget to be wrapped
     */
    protected void initWidget(Widget widget) {

        // Validate. Make sure the widget is not being set twice.
        if (m_widget != null) {
            throw new IllegalStateException("Composite.initWidget() may only be " + "called once.");
        }

        // Use the contained widget's element as the composite's element,
        // effectively merging them within the DOM.
        setElement((Element)widget.getElement());

        adopt(widget);

        // Logical attach.
        m_widget = widget;
    }
}
