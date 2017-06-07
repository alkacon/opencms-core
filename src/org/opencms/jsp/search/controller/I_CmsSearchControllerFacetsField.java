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

package org.opencms.jsp.search.controller;

import java.util.Collection;
import java.util.Map;

/** Interface the controller for all field facets must implement - just allow access to the single controllers. */
public interface I_CmsSearchControllerFacetsField extends I_CmsSearchController {

    /** Get access to the controllers by the field facet's names.
     * @return The map with all field facet controllers, where the facet's names are the keys and the facet's controllers are the values.
     */
    Map<String, I_CmsSearchControllerFacetField> getFieldFacetController();

    /** Get the collection of all field facet controllers.
     * @return The collection of all field facet controllers.
     */
    Collection<I_CmsSearchControllerFacetField> getFieldFacetControllers();
}
