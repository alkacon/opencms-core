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

/**
 * Wrapper for {@link CmsSelectQuery} objects which adds SQL code for results paging.<p>
 *
 * The wrapper can either use the window function approach to paging or append a LIMIT/OFFSET clause.
 *
 * @see "http://troels.arvin.dk/db/rdbms/#select-limit-offset"
 *
 * @since 8.0.0
 */
public class CmsPagingQuery implements I_CmsQueryFragment {

    /** Flag which indicates whether subqueries should be named. */
    private boolean m_nameSubquery;

    /** The current page. */
    private int m_page;

    /** The page size. */
    private int m_pageSize;

    /** The wrapped query. */
    private CmsSelectQuery m_select;

    /** If true, use window functions, else use a LIMIT/OFFSET clause. */
    private boolean m_useWindowFunctions;

    /**
     * Creates a new instance.<p>
     *
     * @param select the wrapped query
     */
    public CmsPagingQuery(CmsSelectQuery select) {

        m_select = select;
    }

    /**
     * Enables or disables the naming of subqueries.<p>
     *
     * @param nameSubquery if true, enables naming of subqueries
     */
    public void setNameSubquery(boolean nameSubquery) {

        m_nameSubquery = nameSubquery;
    }

    /**
     * Sets both the page size and current page to use for the query.<p>
     *
     * @param pageSize the page size
     * @param page the current page (counting starts at 1)
     */
    public void setPaging(int pageSize, int page) {

        m_pageSize = pageSize;
        m_page = page;
    }

    /**
     * Enables the use of window functions.<p>
     *
     * @param useWindowFunctions if true, enables window functions
     */
    public void setUseWindowFunctions(boolean useWindowFunctions) {

        m_useWindowFunctions = useWindowFunctions;
    }

    /**
     * @see org.opencms.db.I_CmsQueryFragment#visit(org.opencms.db.CmsStatementBuilder)
     */
    public void visit(CmsStatementBuilder builder) {

        if (m_useWindowFunctions) {
            I_CmsQueryFragment order = m_select.getOrdering();
            assert order != null;
            CmsCompositeQueryFragment rownumFragment = new CmsCompositeQueryFragment();
            rownumFragment.add(new CmsSimpleQueryFragment("ROW_NUMBER() OVER (ORDER BY "));
            rownumFragment.add(order);
            rownumFragment.add(new CmsSimpleQueryFragment(") AS rownumber"));
            m_select.addColumn(rownumFragment);
            builder.add("SELECT * FROM ( ");
            m_select.visit(builder);
            int start = 1 + (m_pageSize * (m_page - 1));
            int end = (start + m_pageSize) - 1;
            builder.add(")");
            if (m_nameSubquery) {
                builder.add(" AS rnsq ");
            }
            builder.add(" WHERE rownumber BETWEEN " + start + " AND " + end);
        } else {
            m_select.visit(builder);
            int offset = (m_page - 1) * m_pageSize;
            builder.add("\nLIMIT " + m_pageSize + " OFFSET " + offset);
        }
    }
}
