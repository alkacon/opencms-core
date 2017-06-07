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

package org.opencms.db;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A helper class used to accumulate SQL fragments together with the corresponding query parameters.<p>
 *
 * @since 8.0.0
 */
public class CmsStatementBuilder {

    /** The buffer containing the SQL. */
    private StringBuffer m_buffer = new StringBuffer();

    /** The list containing the query parameters. */
    private List<Object> m_params = Lists.newArrayList();

    /**
     * Adds an SQL fragment and zero or more query parameters.<p>
     *
     * @param fragment the SQL fragment
     * @param params the query parameters
     */
    public void add(String fragment, List<Object> params) {

        m_buffer.append(fragment);
        m_params.addAll(params);
    }

    /**
     * Adds an SQL fragment and zero or more query parameters.<p>
     *
     * @param fragment the SQL fragment
     * @param params the query parameters
     */
    public void add(String fragment, Object... params) {

        m_buffer.append(fragment);
        m_params.addAll(Arrays.asList(params));
    }

    /**
     * Returns the list of accumulated query parameters.<p>
     *
     * @return the list of accumulated query parameters
     */
    public List<Object> getParameters() {

        return m_params;
    }

    /**
     * Returns the accumulated query string.<p>
     *
     * @return the accumulated query string
     */
    public String getQuery() {

        return m_buffer.toString();
    }
}
