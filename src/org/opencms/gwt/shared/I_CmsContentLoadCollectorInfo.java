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

/**
 * Interface defining the data used to call a content collector.<p>
 */
public interface I_CmsContentLoadCollectorInfo {

    /**
     * Returns the collector name.<p>
     *
     * @return the collector name
     */
    public String getCollectorName();

    /**
     * Returns the collectorParams.<p>
     *
     * @return the collectorParams
     */
    public String getCollectorParams();

    /**
     * Sets the collectorName.<p>
     *
     * @param collectorName the collectorName to set
     */
    public void setCollectorName(String collectorName);

    /**
     * Sets the collectorParams.<p>
     *
     * @param collectorParams the collectorParams to set
     */
    public void setCollectorParams(String collectorParams);

    /** Returns the fully qualified class name of the used collector. It has to be specified only if the collector name is not set.
     * @return the fully qualified class name of the used collector.
     */
    String getCollectorClass();

    /**
     * Gets the id.<p>
     *
     * @return the id
     */
    String getId();

    /** Sets the class name to identify the collector implementation.
     * @param className the fully qualified class name.
     */
    void setCollectorClass(String className);

    /**
     * Sets the id.<p>
     *
     * @param id the id
     */
    void setId(String id);

}