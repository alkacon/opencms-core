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

package org.opencms.gwt.shared;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A collection of historical versions of a resource.<p>
 */
public class CmsHistoryResourceCollection implements IsSerializable {

    /** Info bean for the current content version. */
    private CmsListInfoBean m_contentInfo;

    /** The list of historical versions. */
    private List<CmsHistoryResourceBean> m_versions = Lists.newArrayList();

    /**
     * Creates a new instance.<p>
     */
    public CmsHistoryResourceCollection() {

        // do nothing

    }

    /**
     * Adds the information for a historical resource version.<p>
     *
     * @param historyBean bean representing a historical resource version
     */
    public void add(CmsHistoryResourceBean historyBean) {

        m_versions.add(historyBean);
    }

    /**
     * Gets the information bean for the current resource version.<p>
     *
     * @return the information bean for the current version
     */
    public CmsListInfoBean getContentInfo() {

        return m_contentInfo;
    }

    /**
     * Gets the list of historical versions.<p>
     *
     * @return the list of historical versions
     */
    public List<CmsHistoryResourceBean> getResources() {

        return m_versions;
    }

    /**
     * Returns true if there are no historical versions.<p>
     *
     * @return true if there are no historical versions
     */
    public boolean isEmpty() {

        return m_versions.isEmpty();
    }

    /**
     * Sets the content information for the current version.<p>
     * @param contentInfo the content information
     */
    public void setContentInfo(CmsListInfoBean contentInfo) {

        m_contentInfo = contentInfo;
    }

}
