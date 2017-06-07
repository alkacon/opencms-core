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

import java.util.ArrayList;
import java.util.List;

/**
 * Class for generating an SQL boolean expression.<p>
 *
 * @since 8.0.0
 */
public class CmsSqlBooleanClause implements I_CmsQueryFragment {

    /** The list of operands. */
    private List<I_CmsQueryFragment> m_fragments = new ArrayList<I_CmsQueryFragment>();

    /** The boolean operator. */
    private String m_operator;

    /**
     * Creates a new boolean clause.<p>
     *
     * @param operator the boolean operator
     */
    public CmsSqlBooleanClause(String operator) {

        m_operator = operator;
    }

    /**
     * Creates a boolean "AND" expression.<p>
     *
     * @param fragments the operands of the "AND"
     *
     * @return the combined expression
     */
    public static CmsSqlBooleanClause makeAnd(I_CmsQueryFragment... fragments) {

        CmsSqlBooleanClause result = new CmsSqlBooleanClause("AND");
        for (I_CmsQueryFragment fragment : fragments) {
            result.addCondition(fragment);
        }
        return result;

    }

    /**
     * Creates a boolean "OR" expression.<p>
     *
     * @param fragments the operands of the "OR"
     *
     * @return the combined expressiong
     */
    public static CmsSqlBooleanClause makeOr(I_CmsQueryFragment... fragments) {

        CmsSqlBooleanClause result = new CmsSqlBooleanClause("OR");
        for (I_CmsQueryFragment fragment : fragments) {
            result.addCondition(fragment);
        }
        return result;
    }

    /**
     * Adds an operand to the boolean expression.<p>
     *
     * @param fragment the operand
     *
     * @return this object instance
     */
    public CmsSqlBooleanClause addCondition(I_CmsQueryFragment fragment) {

        m_fragments.add(fragment);
        return this;
    }

    /**
     * @see org.opencms.db.I_CmsQueryFragment#visit(org.opencms.db.CmsStatementBuilder)
     */
    public void visit(CmsStatementBuilder builder) {

        String connector = " " + m_operator + " ";
        if (m_fragments.size() == 0) {
            throw new IllegalStateException();
        } else if (m_fragments.size() == 1) {
            m_fragments.get(0).visit(builder);
        } else {
            builder.add("(");
            for (int i = 0; i < m_fragments.size(); i++) {
                if (i != 0) {
                    builder.add(connector);
                }
                m_fragments.get(i).visit(builder);
            }
            builder.add(")");
        }
    }

}
