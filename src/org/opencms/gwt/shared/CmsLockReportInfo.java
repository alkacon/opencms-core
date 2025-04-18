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

package org.opencms.gwt.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean holding the lock report info.<p>
 *
 * @since 8.0.1
 */
public class CmsLockReportInfo implements IsSerializable {

    /** The locked child resources. */
    private List<CmsListInfoBean> m_lockedResourceInfos;

    /** The report resource info. */
    private CmsListInfoBean m_resourceInfo;

    /**
     * Constructor.<p>
     *
     * @param resourceInfo the report resource info
     * @param lockedResourceInfos the locked child resources
     */
    public CmsLockReportInfo(CmsListInfoBean resourceInfo, List<CmsListInfoBean> lockedResourceInfos) {

        m_resourceInfo = resourceInfo;
        m_lockedResourceInfos = lockedResourceInfos;
    }

    /**
     * Constructor. Serialization only.<p>
     */
    protected CmsLockReportInfo() {

        // nothing to do
    }

    /**
     * Returns the lockedResourceInfos.<p>
     *
     * @return the lockedResourceInfos
     */
    public List<CmsListInfoBean> getLockedResourceInfos() {

        return m_lockedResourceInfos;
    }

    /**
     * Returns the resourceInfo.<p>
     *
     * @return the resourceInfo
     */
    public CmsListInfoBean getResourceInfo() {

        return m_resourceInfo;
    }
}
