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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * A collector that generates list of {@link org.opencms.file.CmsResource} objects from the VFS.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsResourceCollector extends Comparable<I_CmsResourceCollector>, I_CmsCollectorPublishListProvider {

    /** The default limit for collector results. */
    int DEFAULT_LIMIT = 200;

    /**
     * Returns a list of all collector names (Strings) this collector implementation supports.<p>
     *
     * @return a list of all collector names this collector implementation supports
     */
    List<String> getCollectorNames();

    /**
     * Returns the link that must be executed when a user clicks on the direct edit
     * "new" button on a list created by the default collector.<p>
     *
     * If this method returns <code>null</code>,
     * it indicated that the selected collector implementation does not support a "create link",
     * and so no "new" button will should shown on lists generated with this collector.<p>
     *
     * @param cms the current CmsObject
     *
     * @return the link to execute after a "new" button was clicked
     *
     * @throws CmsException if something goes wrong
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     * @see #getCreateParam(CmsObject, String, String)
     *
     */
    String getCreateLink(CmsObject cms) throws CmsException, CmsDataAccessException;

    /**
     * Returns the link that must be executed when a user clicks on the direct edit
     * "new" button on a list created by the named collector.<p>
     *
     * If this method returns <code>null</code>,
     * it indicated that the selected collector implementation does not support a "create link",
     * and so no "new" button will should shown on lists generated with this collector.<p>
     *
     * @param cms the current CmsObject
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter
     *
     * @return the link to execute after a "new" button was clicked
     *
     * @throws CmsException if something goes wrong
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     * @see #getCreateParam(CmsObject, String, String)
     *
     */
    String getCreateLink(CmsObject cms, String collectorName, String param) throws CmsException, CmsDataAccessException;

    /**
     * Returns the default parameter that must be passed to the
     * {@link #getCreateLink(CmsObject, String, String)} method.<p>
     *
     * If this method returns <code>null</code>,
     * it indicates that the selected collector implementation does not support a "create link",
     * and so no "new" button will should shown on lists generated with this collector.<p>
     *
     * @param cms the current CmsObject
     *
     * @return the parameter that will be passed to the {@link #getCreateLink(CmsObject, String, String)} method, or null
     *
     * @throws CmsDataAccessException if the param attrib of the corresponding collector tag is invalid
     *
     * @see #getCreateLink(CmsObject, String, String)
     */
    String getCreateParam(CmsObject cms) throws CmsDataAccessException;

    /**
     * Returns the parameter that must be passed to the
     * {@link #getCreateLink(CmsObject, String, String)} method.<p>
     *
     * If this method returns <code>null</code>,
     * it indicates that the selected collector implementation does not support a "create link",
     * and so no "new" button will should shown on lists generated with this collector.<p>
     *
     * @param cms the current CmsObject
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter from the current page context
     *
     * @return the parameter that will be passed to the {@link #getCreateLink(CmsObject, String, String)} method, or null
     *
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     *
     * @see #getCreateLink(CmsObject, String, String)
     */
    String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException;

    /**
     * Returns the id of the resource type for new collector items.<p>
     * Returns -1 if creation of new items is not supported.<p>
     *
     * @param cms the current CmsObject
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter
     *
     * @return the resource type id
     *
     * @throws CmsException if something goes wrong
     */
    int getCreateTypeId(CmsObject cms, String collectorName, String param) throws CmsException;

    /**
     * Returns the default collector name to use for collecting resources.<p>
     *
     * @return the default collector name
     */
    String getDefaultCollectorName();

    /**
     * Returns the default collector parameter to use for collecting resources.<p>
     *
     * @return the default collector parameter
     */
    String getDefaultCollectorParam();

    /**
     * Returns the "order weight" of this collector.<p>
     *
     * The "order weight" is important because two collector classes may provide a collector with
     * the same name. If this is the case, the collector implementation with the higher
     * order number "overrules" the lower order number class.<p>
     *
     * @return the "order weight" of this collector
     */
    int getOrder();

    /**
     * Returns a list of {@link org.opencms.file.CmsResource} Objects that are
     * gathered in the VFS using the default collector name and parameter.<p>
     *
     * @param cms the current CmsObject
     *
     * @return a list of CmsXmlContent objects
     *
     * @throws CmsException if something goes wrong
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     */
    List<CmsResource> getResults(CmsObject cms) throws CmsDataAccessException, CmsException;

    /**
     * Returns a list of {@link org.opencms.file.CmsResource} Objects that are
     * gathered in the VFS using the named collector.<p>
     *
     * @param cms the current CmsObject
     * @param collectorName the name of the collector to use
     * @param param an optional collector parameter
     *
     * @return a list of CmsXmlContent objects
     *
     * @throws CmsException if something goes wrong
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     */
    List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException;

    /**
     * Returns a list of {@link org.opencms.file.CmsResource} Objects that are
     * gathered in the VFS using the named collector.<p>
     *
     * This method takes as a parameter the desired number of results. If this number is -1,
     * the number of results will only depend on the collector parameters. If it is positive, any given result
     * number in the collector parameter string will not be used.
     *
     * @param cms the current CmsObject
     * @param collectorName the name of the collector to use
     * @param params an optional collector parameter
     * @param numResults the desired number of results (overrides result number possibl
     *
     * @return a list of CmsXmlContent objects
     *
     * @throws CmsException if something goes wrong
     * @throws CmsDataAccessException if the parameter attribute of the corresponding collector tag is invalid
     */
    List<CmsResource> getResults(CmsObject cms, String collectorName, String params, int numResults)
    throws CmsException;

    /**
     * Sets the default collector name to use for collecting resources.<p>
     *
     * @param collectorName the default collector name
     */
    void setDefaultCollectorName(String collectorName);

    /**
     * Sets the default collector parameter to use for collecting resources.<p>
     *
     * @param param the default collector parameter
     */
    void setDefaultCollectorParam(String param);

    /**
     * Sets the "order weight" of this collector.<p>
     *
     * @param order the order weight to set
     *
     * @see #getOrder()
     */
    void setOrder(int order);
}