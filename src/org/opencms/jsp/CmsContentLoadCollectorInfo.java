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

package org.opencms.jsp;

import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;

/**
 * Bean containing the data needed to call a collector.<p>
 */
public class CmsContentLoadCollectorInfo implements I_CmsContentLoadCollectorInfo {

    /** The collector name. */
    private String m_collectorName;

    /** The collector class. */
    private String m_collectorClass;

    /** The collector parameters. */
    private String m_collectorParams;

    /** The contentload id. */
    private String m_id;

    /**
     * Creates a new instance.<p>
     */
    public CmsContentLoadCollectorInfo() {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#getCollectorClass()
     */
    public String getCollectorClass() {

        return m_collectorClass;
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#getCollectorName()
     */
    @Override
    public String getCollectorName() {

        return m_collectorName;
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#getCollectorParams()
     */
    @Override
    public String getCollectorParams() {

        return m_collectorParams;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#setCollectorClass(java.lang.String)
     */
    public void setCollectorClass(final String className) {

        m_collectorClass = className;

    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#setCollectorName(java.lang.String)
     */
    @Override
    public void setCollectorName(String collectorName) {

        m_collectorName = collectorName;
    }

    /**
     * @see org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo#setCollectorParams(java.lang.String)
     */
    @Override
    public void setCollectorParams(String collectorParams) {

        m_collectorParams = collectorParams;
    }

    /**
     * Sets the id.<p>
     *
     * @param id the id to set
     */
    public void setId(String id) {

        m_id = id;
    }

}
