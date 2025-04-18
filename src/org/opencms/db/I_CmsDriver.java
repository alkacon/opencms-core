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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * Generic base driver interface.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsDriver {

    /** Operator to concatenate or conditions. */
    String AND_CONDITION = " AND ";

    /** String to start a single condition. */
    String BEGIN_CONDITION = " (";

    /** Operator to concatenate exclude conditions. */
    String BEGIN_EXCLUDE_CONDITION = " AND NOT (";

    /** Operator to concatenate include conditions. */
    String BEGIN_INCLUDE_CONDITION = " AND (";

    /** String to end a single condition. */
    String END_CONDITION = ") ";

    /** Operator to concatenate or conditions. */
    String OR_CONDITION = " OR ";

    /**
     * Initializes the driver.<p>
     *
     * @param dbc the current database context
     * @param configurationManager the configuration manager
     * @param successiveDrivers a list of successive drivers to be initialized
     * @param driverManager the initialized OpenCms driver manager
     *
     * @throws CmsException if something goes wrong
     */
    void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List<String> successiveDrivers,
        CmsDriverManager driverManager) throws CmsException;

    /**
     * Returns information about the driver.<p>
     *
     * @return an information string
     */
    String toString();

}