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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.Collections;
import java.util.Set;

/**
 * Dummy implementation of the I_CmsRelatedResourceProvider interface which doesn't actually add any related resources.<p>
 */
public class CmsDummyRelatedResourceProvider implements I_CmsPublishRelatedResourceProvider {

    /** Static instance of this class (we don't need more than one). **/
    public static final I_CmsPublishRelatedResourceProvider INSTANCE = new CmsDummyRelatedResourceProvider();

    /**
     * @see org.opencms.ade.publish.I_CmsPublishRelatedResourceProvider#getAdditionalRelatedResources(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Set<CmsResource> getAdditionalRelatedResources(CmsObject cms, CmsResource res) {

        return Collections.emptySet();

    }

}
