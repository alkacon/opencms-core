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

import org.opencms.acacia.client.widgets.I_EditWidget;
import org.opencms.acacia.client.widgets.I_FormEditWidget;

import com.google.gwt.dom.client.Element;

/**
 * Generates an editing widget with the given configuration.<p>
 */
public interface I_WidgetFactory {

    /**
     * Creates the from editing widget.<p>
     * 
     * @param configuration the widget configuration
     * 
     * @return the widget
     */
    I_FormEditWidget createFormWidget(String configuration);

    /**
     * Wraps an existing DOM element to create an inline editing widget.<p>
     * 
     * @param configuration the widget configuration
     * @param element the element to wrap
     * 
     * @return the widget
     */
    I_EditWidget createInlineWidget(String configuration, Element element);
}
