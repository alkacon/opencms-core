/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.jpa.utils;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 * An interface which represents a parameter value which is going to be used in an SQL prepared statement.<p>
 *
 * @since 8.0.0
 */

public interface I_CmsQueryParameter {

    /**
     * Implementations of this interface should set the index-th parameter of the prepared statement.<p>
     *
     * @param q the query in which to set the value of a bind variable
     * @param index the index of a bind variable
     *
     * @throws PersistenceException if something goes wrong
     */
    void insertIntoQuery(Query q, int index) throws PersistenceException;

}
