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

package org.opencms.jsp.search.result;

import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.search.CmsSearchResource;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @return
*/

/** Interface for a single search result, as wrapped JSP EL friendly. */
public interface I_CmsSearchResourceBean {

    /** Returns the map from field names to field values for date fields.
     * @return The map from field names to field values for date fields.
     */
    Map<String, Date> getDateFields();

    /** Returns the map from field names to field values for string fields.
     * @return The map from field names to field values for string fields.
     */
    Map<String, String> getFields();

    /** Returns the map from field names to field values for multi-valued (string) fields.
     * @return The map from field names to field values for multi-valued (string) fields.
     */
    Map<String, List<String>> getMultiValuedFields();

    /** Returns the wrapped CmsSearchResource (to access the CmsResource).
     * @return The wrapped CmsSearchResource.
     */
    CmsSearchResource getSearchResource();

    /** Reads the resource and allows content access in the current request language - in case of an XML content.
     * If the resource is not an XML content, it returns null.
     * @return The XML content or null.
     */
    CmsJspContentAccessBean getXmlContent();

    /** Reads the resource and allows content access in the specified language - in case of an XML content.
     * If the resource is not an XML content, it returns null.
     * @return The XML content or null.
     */
    Map<String, CmsJspContentAccessBean> getXmlContentInLocale();
}
