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

package org.opencms.db.jpa;

import org.opencms.db.CmsSelectQuery;
import org.opencms.db.CmsSelectQuery.TableAlias;
import org.opencms.db.CmsSimpleQueryFragment;
import org.opencms.db.CmsStatementBuilder;
import org.opencms.db.I_CmsQueryFragment;
import org.opencms.db.generic.CmsUserQueryBuilder;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * User query builder implementation for JPA.<p>
 *
 * @since 8.0.0
 */
public class CmsJpaUserQueryBuilder extends CmsUserQueryBuilder {

    /**
     * Creates a query for searching users.<p>
     *
     * @param searchParams the user search criteria
     * @param countOnly if true, the query will only count the total number of results instead of returning them
     *
     * @return a pair consisting of the query string and its parameters
     */
    @Override
    public CmsPair<String, List<Object>> createUserQuery(CmsUserSearchParameters searchParams, boolean countOnly) {

        CmsSelectQuery select = new CmsSelectQuery();
        TableAlias users = select.addTable("CmsDAOUsers", "user");
        if (countOnly) {
            select.addColumn("COUNT(user.m_userId)");
        } else {
            select.addColumn(users.getName());
        }
        CmsOrganizationalUnit orgUnit = searchParams.getOrganizationalUnit();
        boolean recursive = searchParams.recursiveOrgUnits();

        if (orgUnit != null) {
            addOrgUnitCondition(select, users, orgUnit, recursive);
        }
        if (searchParams.isFilterCore()) {
            select.addCondition(createCoreCondition(users));
        }
        addAllowedOuCondition(select, users, searchParams.getAllowedOus());
        addFlagCondition(select, users, searchParams.getFlags(), searchParams.keepCoreUsers());
        if (orgUnit != null) {
            addWebuserCondition(select, orgUnit, users);
        }
        addSearchFilterCondition(select, users, searchParams);
        addGroupCondition(select, users, searchParams);
        if (!countOnly) {
            addSorting(select, users, searchParams);
        }
        CmsStatementBuilder builder = new CmsStatementBuilder();
        select.visit(builder);
        return CmsPair.create(builder.getQuery(), builder.getParameters());
    }

    /**
     * Adds a sort order to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param searchParams the user search criteria
     */
    @Override
    protected void addSorting(CmsSelectQuery select, TableAlias users, CmsUserSearchParameters searchParams) {

        boolean ascending = searchParams.isAscending();
        String ordering = getSortExpression(users, searchParams);
        String direction;
        if (ascending) {
            direction = " ASC";
        } else {
            direction = " DESC";
        }
        select.addColumn(ordering + " as sortvalue");
        select.setOrdering("sortvalue " + direction);
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colDateCreated()
     */
    @Override
    protected String colDateCreated() {

        return "m_userDateCreated";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colEmail()
     */
    @Override
    protected String colEmail() {

        return "m_userEmail";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colFirstName()
     */
    @Override
    protected String colFirstName() {

        return "m_userFirstName";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colFlags()
     */
    @Override
    protected String colFlags() {

        return "m_userFlags";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colGroupUserGroupId()
     */
    @Override
    protected String colGroupUserGroupId() {

        return "m_groupId";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colGroupUserUserId()
     */
    @Override
    protected String colGroupUserUserId() {

        return "m_userId";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colId()
     */
    @Override
    protected String colId() {

        return "m_userId";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colLastLogin()
     */
    @Override
    protected String colLastLogin() {

        return "m_userLastLogin";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colLastName()
     */
    @Override
    protected String colLastName() {

        return "m_userLastName";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colName()
     */
    @Override
    protected String colName() {

        return "m_userName";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colOu()
     */
    @Override
    protected String colOu() {

        return "m_userOu";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#colPassword()
     */
    @Override
    protected String colPassword() {

        return "m_userPassword";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#createFlagCondition(org.opencms.db.CmsSelectQuery.TableAlias, int)
     */
    @Override
    protected I_CmsQueryFragment createFlagCondition(TableAlias users, int flags) {

        String conditionStr = internalCreateFlagCondition(users.column(colFlags()), flags);
        return new CmsSimpleQueryFragment(conditionStr);
    }

    /**
     * Creates the condition for matching a single flag.<p>
     *
     * @param col the column name
     * @param flag the flag
     * @return the single flag condition
     */
    protected String createSingleFlagCondition(String col, int flag) {

        // example with 6 bits:
        //
        // flags = 011001b
        // flag to check : 001000b (= 2^3)
        // 2^4 = 010000b
        // flags % (2^4) = 001001b, which is >= 001000b
        return "MOD(" + col + ", " + (2 * flag) + ") >= " + flag;
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#generateConcat(java.lang.String[])
     */
    @Override
    protected String generateConcat(String... expressions) {

        // CONCAT with more than 2 arguments may not be supported, so we use nested concat calls instead,
        // i.e. CONCAT(foo, CONCAT(bar, CONCAT(baz, qux)))

        if (expressions.length == 1) {
            return expressions[0];
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < (expressions.length - 1); i++) {
            buffer.append("CONCAT(" + expressions[i] + ", ");
        }
        buffer.append(expressions[expressions.length - 1]);
        for (int i = 0; i < (expressions.length - 1); i++) {
            buffer.append(")");
        }
        return buffer.toString();
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#getGroupUserSubqueryColumns()
     */
    @Override
    protected String getGroupUserSubqueryColumns() {

        return "GU";
    }

    /**
     * Helper method for creating a flag condition.<p>
     *
     * @param col the column which contains the flags
     * @param flags the flags to match
     *
     * @return the flag condition
     */
    protected String internalCreateFlagCondition(String col, int flags) {

        // No bitwise AND in JPQL, so we have to decompose the check into conditions for every single flag
        List<String> singleFlagConditions = new ArrayList<String>();
        for (Integer singleFlag : uncompressFlags(flags)) {
            singleFlagConditions.add(createSingleFlagCondition(col, singleFlag.intValue()));
        }

        return "(" + Joiner.on(" AND ").join(singleFlagConditions) + ")";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#tabGroups()
     */
    @Override
    protected String tabGroups() {

        return "CmsDAOGroups";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#tabGroupUsers()
     */
    @Override
    protected String tabGroupUsers() {

        return "CmsDAOGroupUsers";
    }

    /**
     * @see org.opencms.db.generic.CmsUserQueryBuilder#tabUsers()
     */
    @Override
    protected String tabUsers() {

        return "CmsDAOUsers";
    }

    /**
     * Uncompresses an integer used to store flags into its component flags.<p>
     *
     * @param flags the flags as an integer
     *
     * @return a list of integers which contain a single flag each
     */
    protected List<Integer> uncompressFlags(int flags) {

        List<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < 31; i++) { // only go up to 2^30 since 2^31 is an edge case, and not used anyway
            int v = 1 << i;
            if ((flags & v) != 0) {
                result.add(new Integer(v));
            }
        }
        return result;
    }

}
