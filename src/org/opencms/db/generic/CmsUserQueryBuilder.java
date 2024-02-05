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

package org.opencms.db.generic;

import org.opencms.db.CmsCompositeQueryFragment;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsPagingQuery;
import org.opencms.db.CmsSelectQuery;
import org.opencms.db.CmsSelectQuery.TableAlias;
import org.opencms.db.CmsSimpleQueryFragment;
import org.opencms.db.CmsSqlBooleanClause;
import org.opencms.db.CmsStatementBuilder;
import org.opencms.db.I_CmsQueryFragment;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.file.CmsUserSearchParameters.SearchKey;
import org.opencms.file.CmsUserSearchParameters.SortKey;
import org.opencms.i18n.CmsEncoder;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * Default implementation of the user query builder.<p>
 *
 * @since 8.0.0
 */
public class CmsUserQueryBuilder {

    /**
     * Creates a query for searching users.<p>
     *
     * @param searchParams the user search criteria
     * @param countOnly if true, the query will only count the total number of results instead of returning them
     *
     * @return a pair consisting of the query string and its parameters
     */
    public CmsPair<String, List<Object>> createUserQuery(CmsUserSearchParameters searchParams, boolean countOnly) {

        CmsSelectQuery select = new CmsSelectQuery();
        TableAlias users = select.addTable(tabUsers(), "usr");
        if (countOnly) {
            select.addColumn("COUNT(" + users.column(colId()) + ")");
        } else {
            String[] columns = new String[] {
                colId(),
                colName(),
                colPassword(),
                colFirstName(),
                colLastName(),
                colEmail(),
                colLastLogin(),
                colFlags(),
                colOu(),
                colDateCreated()};
            for (String columnName : columns) {
                select.addColumn(users.column(columnName));
            }
        }
        CmsOrganizationalUnit orgUnit = searchParams.getOrganizationalUnit();
        boolean recursive = searchParams.recursiveOrgUnits();

        if (orgUnit != null) {
            addOrgUnitCondition(select, users, orgUnit, recursive);
        }
        if (searchParams.isFilterCore()) {
            select.addCondition(createCoreCondition(users));
        }
        addEmailCondition(select, users, searchParams.getEmail());
        addAllowedOuCondition(select, users, searchParams.getAllowedOus());
        addFlagCondition(select, users, searchParams.getFlags(), searchParams.keepCoreUsers());
        if (orgUnit != null) {
            addWebuserCondition(select, orgUnit, users);
        }
        addSearchFilterCondition(select, users, searchParams);
        addGroupCondition(select, users, searchParams);
        if (countOnly) {
            CmsStatementBuilder builder = new CmsStatementBuilder();
            select.visit(builder);
            return CmsPair.create(builder.getQuery(), builder.getParameters());
        } else {
            addSorting(select, users, searchParams);
            return makePaged(select, searchParams);
        }
    }

    /**
     * Adds OU conditions to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param allowedOus the allowed ous
     */
    protected void addAllowedOuCondition(
        CmsSelectQuery select,
        TableAlias users,
        List<CmsOrganizationalUnit> allowedOus) {

        if ((allowedOus != null) && !allowedOus.isEmpty()) {
            CmsCompositeQueryFragment ouCondition = new CmsCompositeQueryFragment();
            ouCondition.setPrefix("(");
            ouCondition.setSuffix(")");
            ouCondition.setSeparator(" OR ");
            for (CmsOrganizationalUnit ou : allowedOus) {
                String ouName = CmsStringUtil.joinPaths("/", ou.getName());
                ouCondition.add(new CmsSimpleQueryFragment(users.column(colOu()) + " = ? ", ouName));
            }
            select.addCondition(ouCondition);
        }
    }

    /**
     * Adds an equality test for the email address.
     *
     * If the email address is null, no test is added.
     *
     * @param select the select statement to add the test to
     * @param users the user table alias
     * @param email the email address to compare the column to
     */
    protected void addEmailCondition(CmsSelectQuery select, TableAlias users, String email) {

        if (email != null) {
            CmsSimpleQueryFragment condition = new CmsSimpleQueryFragment(users.column(colEmail()) + " = ? ", email);
            select.addCondition(condition);
        }
    }

    /**
     * Adds flag checking conditions to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param flags the flags
     * @param allowCore set to true if core users should not be filtered out
     */
    protected void addFlagCondition(CmsSelectQuery select, TableAlias users, int flags, boolean allowCore) {

        if (flags != 0) {
            I_CmsQueryFragment condition = createFlagCondition(users, flags);
            if (allowCore) {
                I_CmsQueryFragment coreCondition = createCoreCondition(users);
                select.addCondition(CmsSqlBooleanClause.makeOr(condition, coreCondition));
            } else {
                select.addCondition(condition);
            }
        }
    }

    /**
     * Adds group conditions to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param searchParams the search parameters
     */
    protected void addGroupCondition(CmsSelectQuery select, TableAlias users, CmsUserSearchParameters searchParams) {

        CmsGroup group = searchParams.getGroup();
        if (group != null) {
            CmsUUID groupId = group.getId();
            TableAlias groupUsers = select.addTable(tabGroupUsers(), "groupusrs");
            select.addCondition(
                new CmsSimpleQueryFragment(groupUsers.column(colGroupUserGroupId()) + " = ? ", groupId.toString()));
            select.addCondition(
                new CmsSimpleQueryFragment(groupUsers.column(colGroupUserUserId()) + " = " + users.column(colId())));
            if (searchParams.isFilterByGroupOu()) {
                select.addCondition(new CmsSimpleQueryFragment(users.column(colOu()) + " = ? ", group.getOuFqn()));
            }
        }
        CmsGroup notGroup = searchParams.getNotGroup();
        if (notGroup != null) {
            CmsSimpleQueryFragment notGroupCondition = new CmsSimpleQueryFragment(
                "NOT EXISTS (SELECT "
                    + getGroupUserSubqueryColumns()
                    + " FROM "
                    + tabGroupUsers()
                    + " GU WHERE GU."
                    + colGroupUserUserId()
                    + " = "
                    + users.column(colId())
                    + " AND GU."
                    + colGroupUserGroupId()
                    + " = ?)",
                notGroup.getId().toString());
            select.addCondition(notGroupCondition);
        }

        Collection<CmsGroup> anyGroups = searchParams.getAnyGroups();
        if ((anyGroups != null) && !anyGroups.isEmpty()) {
            CmsCompositeQueryFragment groupClause = new CmsCompositeQueryFragment();
            groupClause.setSeparator(" OR ");
            groupClause.setPrefix("(");
            groupClause.setSuffix(")");
            for (CmsGroup grp : anyGroups) {
                groupClause.add(
                    new CmsSimpleQueryFragment("GU." + colGroupUserGroupId() + " = ?", grp.getId().toString()));
            }
            CmsCompositeQueryFragment existsClause = new CmsCompositeQueryFragment();
            existsClause.add(
                new CmsSimpleQueryFragment(
                    "EXISTS (SELECT "
                        + getGroupUserSubqueryColumns()
                        + " FROM "
                        + tabGroupUsers()
                        + " GU WHERE GU."
                        + colGroupUserUserId()
                        + " = "
                        + users.column(colId())
                        + " AND "));
            existsClause.add(groupClause);
            existsClause.add(new CmsSimpleQueryFragment(" ) "));
            select.addCondition(existsClause);
        }
        Collection<CmsGroup> notAnyGroups = searchParams.getNotAnyGroups();
        if ((notAnyGroups != null) && (!notAnyGroups.isEmpty())) {
            CmsCompositeQueryFragment groupClause = new CmsCompositeQueryFragment();
            groupClause.setPrefix("(");
            groupClause.setSuffix(")");
            groupClause.setSeparator(" OR ");
            for (CmsGroup grp : notAnyGroups) {
                groupClause.add(
                    new CmsSimpleQueryFragment("GU." + colGroupUserGroupId() + " = ?", grp.getId().toString()));
            }
            CmsCompositeQueryFragment notExistsClause = new CmsCompositeQueryFragment();
            notExistsClause.add(
                new CmsSimpleQueryFragment(
                    "NOT EXISTS (SELECT "
                        + getGroupUserSubqueryColumns()
                        + " FROM "
                        + tabGroupUsers()
                        + " GU WHERE GU."
                        + colGroupUserUserId()
                        + " = "
                        + users.column(colId())
                        + " AND "));
            notExistsClause.add(groupClause);
            notExistsClause.add(new CmsSimpleQueryFragment(" ) "));
            select.addCondition(notExistsClause);
        }
    }

    /**
     * Adds a check for an OU to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param orgUnit the organizational unit
     * @param recursive if true, checks for sub-OUs too
     */
    protected void addOrgUnitCondition(
        CmsSelectQuery select,
        TableAlias users,
        CmsOrganizationalUnit orgUnit,
        boolean recursive) {

        String ouName = orgUnit.getName();
        String pattern = CmsOrganizationalUnit.SEPARATOR + ouName;
        if (recursive) {
            pattern += "%";
        }
        select.addCondition(CmsDbUtil.columnLike(users.column(colOu()), pattern));
    }

    /**
     * Adds a search condition to a query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param searchParams the search criteria
     */
    protected void addSearchFilterCondition(
        CmsSelectQuery select,
        TableAlias users,
        CmsUserSearchParameters searchParams) {

        String searchFilter = searchParams.getSearchFilter();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(searchFilter)) {
            boolean caseInsensitive = !searchParams.isCaseSensitive();
            if (caseInsensitive) {
                searchFilter = searchFilter.toLowerCase();
            }
            CmsCompositeQueryFragment searchCondition = new CmsCompositeQueryFragment();
            searchCondition.setSeparator(" OR ");
            searchCondition.setPrefix("(");
            searchCondition.setSuffix(")");
            //use coalesce in case any of the name columns are null
            String patternExprTemplate = generateConcat(
                "COALESCE(%1$s, '')",
                "' '",
                "COALESCE(%2$s, '')",
                "' '",
                "COALESCE(%3$s, '')");
            patternExprTemplate = wrapLower(patternExprTemplate, caseInsensitive);

            String patternExpr = String.format(
                patternExprTemplate,
                users.column(colName()),
                users.column(colFirstName()),
                users.column(colLastName()));
            String like = " LIKE ? ESCAPE '!' ";
            String matchExpr = patternExpr + like;
            searchFilter = "%" + CmsEncoder.escapeSqlLikePattern(searchFilter, '!') + '%';
            searchCondition.add(new CmsSimpleQueryFragment(matchExpr, searchFilter));
            for (SearchKey key : searchParams.getSearchKeys()) {
                switch (key) {
                    case email:
                        searchCondition.add(
                            new CmsSimpleQueryFragment(
                                wrapLower(users.column(colEmail()), caseInsensitive) + like,
                                searchFilter));
                        break;
                    case orgUnit:
                        searchCondition.add(
                            new CmsSimpleQueryFragment(
                                wrapLower(users.column(colOu()), caseInsensitive) + like,
                                searchFilter));
                        break;
                    default:
                        break;
                }
            }
            select.addCondition(searchCondition);
        }
    }

    /**
     * Adds a sort order to an SQL query.<p>
     *
     * @param select the query
     * @param users the user table alias
     * @param searchParams the user search criteria
     */
    protected void addSorting(CmsSelectQuery select, TableAlias users, CmsUserSearchParameters searchParams) {

        boolean ascending = searchParams.isAscending();
        String ordering = getSortExpression(users, searchParams);
        if (ascending) {
            ordering += " ASC";
        } else {
            ordering += " DESC";
        }

        select.setOrdering(ordering);
    }

    /**
     * Adds a check for the web user condition to an SQL query.<p>
     *
     * @param select the query
     * @param orgUnit the organizational unit
     * @param users the user table alias
     */
    protected void addWebuserCondition(CmsSelectQuery select, CmsOrganizationalUnit orgUnit, TableAlias users) {

        String webuserConditionTemplate;
        if (orgUnit.hasFlagWebuser()) {
            webuserConditionTemplate = "( %1$s >= 32768 AND %1$s < 65536 )";
        } else {
            webuserConditionTemplate = "( %1$s < 32768 OR %1$s >= 65536 )";
        }
        String webuserCondition = String.format(webuserConditionTemplate, users.column(colFlags()));
        select.addCondition(webuserCondition);
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colDateCreated() {

        return "USER_DATECREATED";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colEmail() {

        return "USER_EMAIL";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colFirstName() {

        return "USER_FIRSTNAME";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colFlags() {

        return "USER_FLAGS";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colGroupUserGroupId() {

        return "GROUP_ID";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colGroupUserUserId() {

        return "USER_ID";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colId() {

        return "USER_ID";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colLastLogin() {

        return "USER_LASTLOGIN";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colLastName() {

        return "USER_LASTNAME";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colName() {

        return "USER_NAME";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colOu() {

        return "USER_OU";
    }

    /**
     * Column name accessor.<p>
     *
     * @return the name of the column
     */
    protected String colPassword() {

        return "USER_PASSWORD";
    }

    /**
     * Creates a core user check condition.<p>
     *
     * @param users the user table alias
     *
     * @return the resulting SQL expression
     */
    protected I_CmsQueryFragment createCoreCondition(TableAlias users) {

        return new CmsSimpleQueryFragment(users.column(colFlags()) + " <= " + I_CmsPrincipal.FLAG_CORE_LIMIT);
    }

    /**
     * Creates an SQL flag check condition.<p>
     *
     * @param users the user table alias
     * @param flags the flags to check
     *
     * @return the resulting SQL expression
     */
    protected I_CmsQueryFragment createFlagCondition(TableAlias users, int flags) {

        return new CmsSimpleQueryFragment(
            users.column(colFlags()) + " & ? = ? ",
            Integer.valueOf(flags),
            Integer.valueOf(flags));
    }

    /**
     * Generates an SQL expression for concatenating several other SQL expressions.<p>
     *
     * @param expressions the expressions to concatenate
     *
     * @return the concat expression
     */
    protected String generateConcat(String... expressions) {

        return "CONCAT(" + Joiner.on(", ").join(expressions) + ")";
    }

    /**
     * Generates an SQL expression for trimming whitespace from the beginning and end of a string.<p>
     *
     * @param expression the expression to wrap
     *
     * @return the expression for trimming the given expression
     */
    protected String generateTrim(String expression) {

        return "TRIM(" + expression + ")";
    }

    /**
     * Returns the columns that should be returned by  user subqueries.<p>
     *
     * @return the columns that should be returned by user subqueries
     */
    protected String getGroupUserSubqueryColumns() {

        return "*";
    }

    /**
     * Returns the expression used for sorting the results.<p>
     *
     * @param users the user table alias
     * @param searchParams the search parameters
     *
     * @return the sorting expressiong
     */
    protected String getSortExpression(TableAlias users, CmsUserSearchParameters searchParams) {

        SortKey sortKey = searchParams.getSortKey();
        String ordering = users.column(colId());
        if (sortKey != null) {
            switch (sortKey) {
                case email:
                    ordering = users.column(colEmail());
                    break;
                case loginName:
                    ordering = users.column(colName());
                    break;
                case fullName:
                    ordering = getUserFullNameExpression(users);
                    break;
                case lastLogin:
                    ordering = users.column(colLastLogin());
                    break;
                case orgUnit:
                    ordering = users.column(colOu());
                    break;
                case activated:
                    ordering = getUserActivatedExpression(users);
                    break;
                case flagStatus:
                    ordering = getUserFlagExpression(users, searchParams.getSortFlags());
                    break;
                default:
                    break;

            }
        }
        return ordering;
    }

    /**
     * Returns an expression for checking whether a user is activated.<p>
     *
     * @param users the user table alias
     *
     * @return the expression for checking whether the user is activated
     */
    protected String getUserActivatedExpression(TableAlias users) {

        return "MOD(" + users.column(colFlags()) + ", 2)";
    }

    /**
     * Returns a bitwise AND expression with a fixed second operand.<p>
     *
     * @param users the user table alias
     * @param flags the user flags
     * @return the resulting SQL expression
     */
    protected String getUserFlagExpression(TableAlias users, int flags) {

        return users.column(colFlags()) + " & " + flags;

    }

    /**
     * Returns the SQL expression for generating the user's full name in the format
     * 'firstname lastname (loginname)'.<p>
     *
     * @param users the user table alias
     *
     * @return the expression for generating the user's full name
     */
    protected String getUserFullNameExpression(TableAlias users) {

        //use coalesce in case any of the name columns are null
        String template = generateTrim(
            generateConcat("COALESCE(%1$s, '')", "' '", "COALESCE(%2$s, '')", "' ('", "%3$s", "')'"));
        return String.format(
            template,
            users.column(colFirstName()),
            users.column(colLastName()),
            users.column(colName()));
    }

    /**
     * Creates a query which uses paging from another query.<p>
     *
     * @param select the base query
     * @param params the query parameters
     *
     * @return the paged version of the query
     */
    protected CmsPair<String, List<Object>> makePaged(CmsSelectQuery select, CmsUserSearchParameters params) {

        CmsPagingQuery paging = new CmsPagingQuery(select);
        paging.setUseWindowFunctions(useWindowFunctionsForPaging());
        int page = params.getPage();
        int pageSize = params.getPageSize();
        paging.setNameSubquery(shouldNameSubqueries());
        paging.setPaging(pageSize, page);
        CmsStatementBuilder builder = new CmsStatementBuilder();
        paging.visit(builder);
        return CmsPair.create(builder.getQuery(), builder.getParameters());
    }

    /**
     * Should return true if subqueries in a FROM clause should be named.<p>
     *
     * @return true if subqueries in a FROM clause should be named
     */
    protected boolean shouldNameSubqueries() {

        return false;
    }

    /**
     * Table name accessor.<p>
     *
     * @return the name of a table
     */
    protected String tabGroups() {

        return "CMS_GROUPS";
    }

    /**
     * Table name accessor.<p>
     *
     * @return the name of a table
     */
    protected String tabGroupUsers() {

        return "CMS_GROUPUSERS";
    }

    /**
     * Table name accessor.<p>
     *
     * @return the name of a table
     */
    protected String tabUsers() {

        return "CMS_USERS";
    }

    /**
     * Returns true if window functions should be used for paging.<p>
     *
     * @return true if window functions should be used for paging
     */
    protected boolean useWindowFunctionsForPaging() {

        return false;
    }

    /**
     * Wraps an SQL expression in a "LOWER" call conditionally.<p>
     *
     * @param expr the expression to wrap
     * @param caseInsensitive if false, no wrapping should occur
     *
     * @return the resulting expression
     */
    protected String wrapLower(String expr, boolean caseInsensitive) {

        return caseInsensitive ? "LOWER(" + expr + ")" : expr;
    }

}
