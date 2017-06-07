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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * An abstract class for widgets which represent options for select boxes.<p>
 *
 * @since 8.0.0
 *
 */
public abstract class A_CmsSelectCell extends Composite {

    /**
     * Measures the required width for this cell.<p>
     *
     * @return the required width
     */
    public int getRequiredWidth() {

        Element clone = CmsDomUtil.clone(getElement());
        clone.getStyle().setPosition(Position.ABSOLUTE);
        RootPanel.getBodyElement().appendChild(clone);
        int result = clone.getOffsetWidth();
        clone.removeFromParent();
        return result;
    }

    /**
     * Returns the value of the select option as a string.<p>
     *
     * @return the value of the select option
     */
    public abstract String getValue();

    /**
     * Adds a new event handler to the widget.<p>
     *
     * This method is used because we want the select box to register some event handlers on this widget,
     * but we can't use {@link com.google.gwt.user.client.ui.Widget#addDomHandler} directly, since it's both protected
     * and final.
     *
     * @param <H> the event type
     * @param handler the new event handler
     * @param type the event type object
     *
     * @return the HandlerRegistration for removing the event handler
     */
    public <H extends EventHandler> HandlerRegistration registerDomHandler(final H handler, DomEvent.Type<H> type) {

        return addDomHandler(handler, type);
    }

}
