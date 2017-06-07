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

package org.opencms.file.wrapper;

import org.opencms.file.types.CmsResourceTypeJsp;

/**
 * A resource type wrapper for jsp files, which adds the correct file extension "jsp"
 * to the resources.<p>
 *
 * @since 6.5.6
 */
public class CmsResourceExtensionWrapperJsp extends A_CmsResourceExtensionWrapper {

    /** The extension used for this resource type wrapper. */
    private static final String RESOURCE_TYPE_EXTENSION = "jsp";

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceExtensionWrapper#checkTypeId(int)
     */
    @Override
    protected boolean checkTypeId(int typeId) {

        return CmsResourceTypeJsp.isJspTypeId(typeId);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceExtensionWrapper#getExtension()
     */
    @Override
    protected String getExtension() {

        return RESOURCE_TYPE_EXTENSION;
    }

}
