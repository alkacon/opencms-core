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

package org.opencms.cmis;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import org.apache.chemistry.opencmis.commons.data.RenditionData;

/**
 * Rendition provider interface used to generate alternative renditions for resources.<p>
 */
public interface I_CmsCmisRenditionProvider {

    /**
     * Gets the rendition content stream for the resource.<p>
     *
     * @param cms the CMS context
     * @param resource the resource
     *
     * @return the content stream for the rendition of the resource
     */
    byte[] getContent(CmsObject cms, CmsResource resource);

    /**
     * Gets the rendition stream id.<p>
     *
     * @return the rendition stream id
     */
    String getId();

    /**
     * Gets the rendition kind.<p>
     *
     * @return the rendition kind
     */
    String getKind();

    /**
     * Gets the rendition mimetype.<p>
     *
     * @return the rendition mimetype
     */
    String getMimeType();

    /**
     * Gets the rendition data for a resource.<p>
     *
     * This method may return null to signal that a rendition can not be generated for the resource.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource
     *
     * @return the rendition data for the resource
     */
    RenditionData getRendition(CmsObject cms, CmsResource resource);

}
