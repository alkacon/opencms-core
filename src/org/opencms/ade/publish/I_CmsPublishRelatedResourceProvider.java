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

package org.opencms.ade.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Set;

/**
 * Interface used to generate mode-specific 'related resources' for the publish dialog in addition to resources linked by normal relations.<p>
 */
public interface I_CmsPublishRelatedResourceProvider {

    /**
     * Gets the set of additional related resources.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the related resources should be returned
     *
     * @return the set of related resources
     *
     * @throws CmsException if something goes wrong
     */
    Set<CmsResource> getAdditionalRelatedResources(CmsObject cms, CmsResource res) throws CmsException;

}
