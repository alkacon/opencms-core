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

package org.opencms.db.jpa.utils;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 * A query parameter of type 'int'.<p>
 *
 * @since 8.0.0
 */
public class CmsQueryIntParameter implements I_CmsQueryParameter {

    /** The value of the parameter. */
    private int m_param;

    /**
     * Creates a new parameter value.<p>
     *
     * @param param the value to use for this parameter
     */
    public CmsQueryIntParameter(int param) {

        m_param = param;
    }

    /**
     * @see org.opencms.db.jpa.utils.I_CmsQueryParameter#insertIntoQuery(javax.persistence.Query, int)
     */
    public void insertIntoQuery(Query q, int index) throws PersistenceException {

        q.setParameter(index, Integer.valueOf(m_param));
    }
}
