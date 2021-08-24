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

package org.opencms.xml.xml2json.handler;

import org.opencms.xml.xml2json.CmsJsonResult;

/**
 * Interface for individual JSON handlers.
 *
 * <p>The CmsJsonResourceHandler delegates the actual work of producing the JSON to a number of sub-handlers,
 * e.g. one for XML contents, one for folders, etc. This is the base interface for these sub-handlers. Handlers are explicitly
 * sorted by their order, and then the first matching handler is selected.
 *
 */
public interface I_CmsJsonHandler {

    /**
     * Gets the sort order for this handler.<p>
     *
     * Handlers are sorted by ascending order, and the first matching handler is selected.
     *
     * @return the sort order
     */
    double getOrder();

    /**
     * Returns true if the handler matches the given context.
     *
     * @param context the context
     * @return true if the handler matches
     */
    boolean matches(CmsJsonHandlerContext context);

    /**
     * Renders the JSON.
     *
     * @param context the context (provides information about the path and resource)
     * @return the JSON result
     */
    CmsJsonResult renderJson(CmsJsonHandlerContext context);

}
