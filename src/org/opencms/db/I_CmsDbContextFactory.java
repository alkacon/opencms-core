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

import org.opencms.file.CmsRequestContext;

/**
 * This interface defines a factory to create runtime info objects.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsDbContextFactory {

    /**
     * Initializes the runtime info factory with the OpenCms driver manager.<p>
     *
     * @param driverManager the initialized OpenCms driver manager
     */
    void initialize(CmsDriverManager driverManager);

    /**
     * Returns a new database context based on the given user request context.<p>
     *
     * @param context the user request context to initialize the database context with
     *
     * @return a new database context based on the given user request context
     */
    CmsDbContext getDbContext(CmsRequestContext context);

    /**
     * Returns a new database context.<p>
     *
     * @return a new database context
     */
    CmsDbContext getDbContext();
}
