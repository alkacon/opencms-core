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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * A class for generating SQL SELECT statements.<p>
 *
 * @since 8.0.0
 */
public class CmsSelectQuery implements I_CmsQueryFragment {

    /**
     * Helper class which wraps a table alias.<p>
     */
    public class TableAlias {

        /** The table alias. */
        String m_name;

        /**
         * Creates a new instance.<p>
         *
         * @param name the table alias
         */
        public TableAlias(String name) {

            m_name = name;
        }

        /**
         * Adds the table alias before a column name.<p>
         *
         * @param colName the column name
         *
         * @return the column name, qualified by the table alias
         */
        public String column(String colName) {

            return m_name + "." + colName;
        }

        /**
         * Returns the name of the table alias.<p>
         *
         * @return the name of the table alias
         */
        public String getName() {

            return m_name;
        }
    }

    /** The columns of the result. */
    private CmsCompositeQueryFragment m_columns = new CmsCompositeQueryFragment();

    /** The conditions for the WHERE clause. */
    private CmsCompositeQueryFragment m_conditions = new CmsCompositeQueryFragment();

    /** The result ordering. */
    private I_CmsQueryFragment m_ordering;

    /** SQL clauses which will be added after the other ones. */
    private CmsCompositeQueryFragment m_otherClauses = new CmsCompositeQueryFragment();

    /** The tables from which the data should be fetched. */
    private List<String> m_tables = Lists.newArrayList();

    /** The table aliases which have already been used. */
    private Set<String> m_usedAliases = new HashSet<String>();

    /**
     * Creates a new instance.<p>
     */
    public CmsSelectQuery() {

        // always use 1 = 1 as a condition so we don't have to worry about whether we need a "WHERE" keyword
        m_conditions.add(new CmsSimpleQueryFragment("1 = 1", Collections.<Object> emptyList()));
        m_conditions.setSeparator(" AND ");
        m_otherClauses.setSeparator("\n");
        m_columns.setSeparator(", ");
    }

    /**
     * Adds another clause to the query.<p>
     *
     * @param clause the clause to add
     */
    public void addClause(I_CmsQueryFragment clause) {

        m_otherClauses.add(clause);
    }

    /**
     * Adds an expression which should be added as a column in the result set.<p>
     *
     * @param node the expression which should be added as a column
     */
    public void addColumn(I_CmsQueryFragment node) {

        m_columns.add(node);
    }

    /**
     * Adds an expression which should be added as a column in the result set.<p>
     *
     * @param column the expression which should be added as a column
     */
    public void addColumn(String column) {

        m_columns.add(new CmsSimpleQueryFragment(column, Collections.<Object> emptyList()));
    }

    /**
     * Adds a new condition to the query.<p>
     *
     * @param node the condition to add to the query
     */
    public void addCondition(I_CmsQueryFragment node) {

        m_conditions.add(node);
    }

    /**
     * Adds a new condition to the query.<p>
     *
     * @param fragment the condition SQL
     * @param params the condition parameters
     */
    public void addCondition(String fragment, Object... params) {

        m_conditions.add(new CmsSimpleQueryFragment(fragment, params));
    }

    /**
     * Adds a table to the query's FROM clause.<p>
     *
     * @param table the table to add
     */
    public void addTable(String table) {

        m_tables.add(table);
    }

    /**
     * Adds a table the query's FROM clause.<p>
     *
     * @param table the table to add
     * @param aliasPrefix the prefix used to generate the alias
     *
     * @return an alias for the table
     */
    public TableAlias addTable(String table, String aliasPrefix) {

        String alias = makeAlias(aliasPrefix);
        m_tables.add(table + " " + alias);
        return new TableAlias(alias);

    }

    /**
     * Returns the fragment for the ORDER BY clause.<p>
     *
     * @return the fragment for the ORDER BY clause
     */
    public I_CmsQueryFragment getOrdering() {

        return m_ordering;
    }

    /**
     * Sets the SQL used for the ORDER BY clause.<p>
     *
     * @param ordering the SQL used for the ORDER BY clause
     */
    public void setOrdering(String ordering) {

        if (ordering != null) {
            m_ordering = new CmsSimpleQueryFragment(ordering, Collections.<Object> emptyList());
        } else {
            m_ordering = null;
        }
    }

    /**
     * @see org.opencms.db.I_CmsQueryFragment#visit(org.opencms.db.CmsStatementBuilder)
     */
    public void visit(CmsStatementBuilder builder) {

        builder.add("SELECT ");
        Joiner commaJoin = Joiner.on(", ");
        m_columns.visit(builder);
        builder.add("\nFROM ");
        builder.add(commaJoin.join(m_tables));
        builder.add("\nWHERE ");
        m_conditions.visit(builder);
        if (m_ordering != null) {
            builder.add("\nORDER BY ");
            m_ordering.visit(builder);
        }
        m_otherClauses.visit(builder);

    }

    /**
     * Helper method for generating an alias by taking a prefix and appending the first number to it for which
     * the resulting string is not already used as an alias.<p>
     *
     * @param prefix the alias prefix
     *
     * @return the table alias
     */
    private String makeAlias(String prefix) {

        int i = 0;
        String result;
        do {
            if (i == 0) {
                result = prefix;
            } else {
                result = prefix + i;
            }

        } while (m_usedAliases.contains(result));
        m_usedAliases.add(result);
        return result;

    }

}
