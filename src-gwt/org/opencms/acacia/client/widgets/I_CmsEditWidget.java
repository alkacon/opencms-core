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

package org.opencms.acacia.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * The edit widget interface.<p>
 */
public interface I_CmsEditWidget extends HasValue<String>, HasFocusHandlers, IsWidget {

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler);

    /**
     * Returns if the widget is active.<p>
     *
     * @return <code>true</code> if the widget is active
     */
    boolean isActive();

    /**
     * This method is called when a widget is attached to the browser's document.<p>
     * It needs to call the {@link com.google.gwt.user.client.ui.Widget#onAttach()} method.<p>
     */
    @SuppressWarnings("javadoc")
    void onAttachWidget();

    /**
     * Returns true if the element should be logically counted as part of the widget for the purpose of determining whether a mouse click is "outside".
     *
     * For example, this is needed if the widget uses a popup.
     *
     * @param element the element to check
     *
     * @return true if the element counts as part of the widget
     */
    boolean owns(Element element);

    /**
     * Sets the widget active/inactive.<p>
     *
     * @param active <code>true</code> to activate the widget
     */
    void setActive(boolean active);

    /**
     * Sets the name of input fields.<p>
     *
     * @param name of the input field
     */
    void setName(String name);

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    void setValue(String value, boolean fireEvent);
}
