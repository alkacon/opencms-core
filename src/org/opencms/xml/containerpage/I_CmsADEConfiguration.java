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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletRequest;

/**
 * Configurable & expandable configuration.<p>
 *
 * @since 7.6
 */
public interface I_CmsADEConfiguration {

    /**
     * Creates a new element of a given type at the configured location.<p>
     *
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the type of the element to be created
     * @param locale the content locale
     *
     * @return the CmsResource representing the newly created element
     *
     * @throws CmsException if something goes wrong
     */
    CmsResource createNewElement(CmsObject cms, String cntPageUri, ServletRequest request, String type, Locale locale)
    throws CmsException;

    /**
     * Returns the list of creatable elements.<p>
     *
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     *
     * @return the list of creatable elements
     *
     * @throws CmsException if something goes wrong
     */
    Collection<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException;

    /**
     * Returns the maximal size of the favorite list.<p>
     *
     * @param cms the current opencms context
     *
     * @return the maximal size of the favorite list
     *
     * @throws CmsException if something goes wrong
     */
    int getFavoriteListMaxSize(CmsObject cms) throws CmsException;

    /**
     * Returns the formatter configuration for a given resource.<p>
     *
     * @param cms the OpenCms user context
     * @param containerPageRootPath the root path to the container page that includes the element resource
     * @param res the container page element resource
     *
     * @return the formatter configuration for a given resource
     *
     * @throws CmsException if something goes wrong
     */
    CmsFormatterConfiguration getFormattersForResource(CmsObject cms, String containerPageRootPath, CmsResource res)
    throws CmsException;

    /**
     * Returns the name of the next new file of the given type to be created.<p>
     *
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the resource type name
     *
     * @return the name of the next new file of the given type to be created
     *
     * @throws CmsException if something goes wrong
     */
    String getNextNewFileName(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException;

    /**
     * Returns the maximal size of the recent list.<p>
     *
     * @param cms the current opencms context
     *
     * @return the maximal size of the recent list
     *
     * @throws CmsException if something goes wrong
     */
    int getRecentListMaxSize(CmsObject cms) throws CmsException;

    /**
     * Returns the list of searchable resource types.<p>
     *
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     *
     * @return the list of searchable resource types, identified by a sample resource
     *
     * @throws CmsException if something goes wrong
     */
    Collection<CmsResource> getSearchableResourceTypes(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException;

    /**
     * Initializes the configuration.<p>
     *
     * @param cms the CMS object
     */
    void init(CmsObject cms);

    /**
     * Returns if the given type has a valid configuration to be created.<p>
     *
     * @param cms the CMS context
     * @param currentUri the current URI
     * @param typeName the resource type name
     *
     * @return <code>true</code> if the type can be created as new
     *
     * @throws CmsException if something goes wrong
     */
    boolean isCreatableType(CmsObject cms, String currentUri, String typeName) throws CmsException;

}
