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

/**
 * A simple query fragment which takes its SQL string and query parameters as
 * constructor arguments.<p>
 *
 * @since 8.0.0
 */
public class CmsSimpleQueryFragment implements I_CmsQueryFragment {

    /** The SQL fragment. */
    private String m_fragment;

    /** The query parameters. */
    private List<Object> m_params;

    /**
     * Creates a new instance.<p>
     *
     * @param fragment the SQL fragment
     * @param params the query parameters
     */
    public CmsSimpleQueryFragment(String fragment, List<Object> params) {

        m_fragment = fragment;
        m_params = params;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param fragment the SQL fragment
     * @param params the query parameters
     */
    public CmsSimpleQueryFragment(String fragment, Object... params) {

        m_fragment = fragment;
        m_params = Arrays.asList(params);
    }

    /**
     * @see org.opencms.db.I_CmsQueryFragment#visit(org.opencms.db.CmsStatementBuilder)
     */
    public void visit(CmsStatementBuilder builder) {

        builder.add(m_fragment, m_params);
    }

}
