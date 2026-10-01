/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.I_CmsFormatterBean;

import javax.servlet.jsp.PageContext;

/**
 * Plug-in interface for emitting a marker into the page output around each element while the container
 * and display tags render a page.<p>
 *
 * The markers are written at the same positions as the ADE edit markers; an implementation may use
 * them for purposes such as building a content summary of the rendered page. The implementation is
 * optional and configured through the system configuration (the <code>edit-marker-handler</code>
 * element). When none is configured the no-op {@link CmsDefaultElementMarker} is used, so nothing is
 * emitted and normal rendering is unaffected. The configured instance is created once at startup and
 * reached through {@link org.opencms.main.OpenCms#getElementMarker()}.<p>
 *
 * Implementations are parameter handlers, so they can be configured with nested
 * <code>&lt;param&gt;</code> elements that are read from and written back to the configuration.<p>
 *
 * @since 22.0.0
 */
public interface I_CmsElementMarker extends I_CmsConfigurationParameterHandler {

    /**
     * Adds the end marker for an element to the page output.<p>
     *
     * @param context the page context
     * @param element the element
     */
    void addEndMarker(PageContext context, CmsContainerElementBean element);

    /**
     * Adds the start marker for an element to the page output.<p>
     *
     * @param context the page context
     * @param element the element
     * @param formatter the formatter the element is rendered with, or <code>null</code>
     */
    void addStartMarker(PageContext context, CmsContainerElementBean element, I_CmsFormatterBean formatter);
}
