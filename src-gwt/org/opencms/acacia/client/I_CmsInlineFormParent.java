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
import com.google.gwt.user.client.ui.IsWidget;

/**
 * An inline form parent widget.<p>
 */
public interface I_CmsInlineFormParent {

    /**
     * Adopts the given widget as a child widget.<p>
     * This assumes the widget element is already attached to the DOM and is a child or descendant of this widget element.<p>
     *
     * @param widget the widget to adopt
     */
    void adoptWidget(IsWidget widget);

    /**
     * The widget element.<p>
     *
     * @return the widget element
     */
    Element getElement();

    /**
     * Replaces the inner HTML of widget to reflect content data changes.<p>
     *
     * @param html the element HTML
     */
    void replaceHtml(String html);

}
