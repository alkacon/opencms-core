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

package org.opencms.workflow;

import org.opencms.ade.publish.CmsPublishRelationFinder.ResourceMap;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * 'Formats' a ResourceMap containing resources for publishing by creating a list of CmsPublishResource
 * beans with the appropriate status informations to display.
 */
public interface I_CmsPublishResourceFormatter {

    /**
     * Gets the publish resource beans created by this formatter.<p>
     *
     * @return the publish resource beans
     * @throws CmsException if something goes wrong
     */
    List<CmsPublishResource> getPublishResources() throws CmsException;

    /**
     * Initializes the formatter.<p>
     *
     * @param options the publish options
     * @param resources the publish resources
     *
     * @throws CmsException in case something goes wrong
     */
    void initialize(CmsPublishOptions options, ResourceMap resources) throws CmsException;
}
